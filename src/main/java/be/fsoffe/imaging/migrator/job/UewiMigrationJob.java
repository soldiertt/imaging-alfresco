package be.fsoffe.imaging.migrator.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import be.fsoffe.imaging.migrator.Environment;
import be.fsoffe.imaging.migrator.ImagingMigratorException;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.migrator.dao.ClientServerImageDao;
import be.fsoffe.imaging.migrator.dao.UewiDocumentDao;
import be.fsoffe.imaging.migrator.mapper.ImagingDocumentMapper;
import be.fsoffe.imaging.migrator.model.ImagingDocument;
import be.fsoffe.imaging.migrator.model.ImagingNote;
import be.fsoffe.imaging.migrator.service.AlfrescoRepositoryService;
import be.fsoffe.imaging.migrator.service.AnnotationService;
import be.fsoffe.imaging.migrator.service.FileSystemService;

/**
 * Uewi migration job.
 * 
 * @author jbourlet
 * 
 */
public class UewiMigrationJob implements Job {

	private static final Log LOGGER = LogFactory.getLog(UewiMigrationJob.class);

	private UewiDocumentDao uewiDocumentDao;
	private ClientServerImageDao clientServerImageDao;

	private AlfrescoRepositoryService alfRepoService;
	private FileSystemService fileSystemService;
	private AnnotationService annotationService;
	private NodeRef companyHome;
	private TransactionService transactionService;
	private RetryingTransactionHelper txnHelper;

	private Environment environment;
	private boolean wipMode;

	/**
	 * KEEP A GLOBAL STATUS OF THE MIGRATION.
	 */
	public static boolean migrationPending;

	@Override
	public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING Uewi Migration Job @@@");

		if (!UewiMigrationJob.migrationPending) {

			try {
				UewiMigrationJob.migrationPending = true;

				initializeMembers(jobexecutioncontext);

				companyHome = alfRepoService.getCompanyHome();

				/*************************************************
				 ** Filter documents to process - PASS 1 
				 ** NON-WIP documents 
				 ** NOT Wrong DocType (NOTTOKEEP*)
				 ***************************************************/
				List<ImagingDocument> documentsToProcess = new ArrayList<ImagingDocument>();
				try {
					documentsToProcess = uewiDocumentDao.getDocumentsToProcess(wipMode);
				} catch (SQLException e) {
					LOGGER.error("Cannot retrieve documents to process !", e);
					throw new ImagingMigratorException();
				}

				for (final ImagingDocument docRow : documentsToProcess) {

					LOGGER.info("Processing document '" + docRow.getLegacyDocId() + "'");

					/*******************************************************
					 ** Filter documents to process - PASS 2 
					 ** Need to be linked in fso:image table in client-server
					 ********************************************************/
					boolean docIsLinked = false;

					try {
						docIsLinked = clientServerImageDao.documentIsLinked(docRow.getLegacyDocId());
					} catch (SQLException e) {
						LOGGER.error("Unable to check if document is linked !", e);
						throw new ImagingMigratorException();
					}

					if ((docIsLinked && !wipMode) || wipMode) {
						if (wipMode && docIsLinked) {
							docRow.setLegacyWip(false); // WIP linked docs will go to the repository
						}
						AuthenticationUtil.runAs(new RunAsWork<Void>() {
							@Override
							public Void doWork() {
								return txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
									public Void execute() throws Throwable {
										migrateDocument(docRow);
										return null;
									}
								});
							}
						}, AuthenticationUtil.SYSTEM_USER_NAME);

					} else {

						/*******************************************************
						 ** Also remove document from file system
						 ********************************************************/
						//fileSystemService.removeMainPDF(docRow);

						LOGGER.info("Document '" + docRow.getLegacyDocId() + "' is not linked in fso:image table !");
						if (!uewiDocumentDao.updateDocumentStatus(docRow, MigratorUtil.STATUS_NOT_LINKED, null, environment)) {
							LOGGER.error("Document '" + docRow.getLegacyDocId() + "' cannot be marked with status "
									+ MigratorUtil.STATUS_NOT_LINKED);
							throw new ImagingMigratorException();
						}
					}
				}
			} finally {
				UewiMigrationJob.migrationPending = false;
			}
		} else {
			LOGGER.info("Migration still in progress ... do nothing.");
		}
		LOGGER.info("@@@ ENDING Uewi Migration Job @@@");
	}

	/**
	 * Initialize dao's and services.
	 * 
	 * @param jobexecutioncontext job execution context of the scheduled job
	 */
	private void initializeMembers(final JobExecutionContext jobexecutioncontext) {
		uewiDocumentDao = (UewiDocumentDao) jobexecutioncontext.getJobDetail().getJobDataMap().get("uewiDocumentDao");
		clientServerImageDao = (ClientServerImageDao) jobexecutioncontext.getJobDetail().getJobDataMap().get("clientServerImageDao");
		alfRepoService = (AlfrescoRepositoryService) jobexecutioncontext.getJobDetail().getJobDataMap().get("alfRepoService");
		fileSystemService = (FileSystemService) jobexecutioncontext.getJobDetail().getJobDataMap().get("fileSystemService");
		annotationService = (AnnotationService) jobexecutioncontext.getJobDetail().getJobDataMap().get("annotationService");
		transactionService = (TransactionService) jobexecutioncontext.getJobDetail().getJobDataMap().get("transactionService");
		txnHelper = transactionService.getRetryingTransactionHelper();
		environment = Environment.valueOf(jobexecutioncontext.getJobDetail().getJobDataMap().getString("environment"));
		if (jobexecutioncontext.getJobDetail().getJobDataMap().get("wipmode") != null) {
			wipMode = Boolean.parseBoolean(jobexecutioncontext.getJobDetail().getJobDataMap().getString("wipmode"));
		} else {
			wipMode = false;
		}
	}

	/**
	 * Main method of the migrator, migrate a single document.
	 * 
	 * @param imgDocument the imaging document
	 * @throws ImagingMigratorException if error occurs
	 */
	private void migrateDocument(ImagingDocument imgDocument) throws ImagingMigratorException {

		JSONObject jsonObject = null;

		try {
			jsonObject = fileSystemService.getDocumentPropertiesAsJson(imgDocument);
		} catch (ImagingMigratorException e) {
			if (e.getErrorCode() > 0) { // Managed errors
				LOGGER.error("Error when reading input json properties file, mark record with error code " + e.getErrorCode());
				// Mark with special status in export database
				if (!uewiDocumentDao.updateDocumentStatus(imgDocument, e.getErrorCode(), null, environment)) {
					LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status " + e.getErrorCode());
					throw new ImagingMigratorException();
				}
			} else { // Non-managed errors
				throw e;
			}
		}

		if (jsonObject != null) {

			mapBasicMetadata(imgDocument, jsonObject);

			// NOTES
			JSONArray notes = (JSONArray) jsonObject.get("notes");
			List<ImagingNote> notesList = new ArrayList<ImagingNote>();
			if (notes != null) {
				for (int i = 0; i < notes.size(); i++) {
					JSONObject noteObject = (JSONObject) notes.get(i);
					notesList.add(new ImagingNote((String) noteObject.get("Text"), (String) noteObject.get("Author")));
				}
				imgDocument.setNotes(notesList);
			}

			/*******************************************************
			 ** Document previously migrated ?
			 ********************************************************/
			boolean removeOK = true;
			boolean skip = false;
			if (imgDocument.getDocName() != null) {
				// EXTRA CHECK 29/05/2015 - NOT UPDATED SINCE 26/05
				NodeRef existing = alfRepoService.getNodeRef(imgDocument.getDocName());
				if (existing != null) {
					if (!alfRepoService.annotUpdatedSinceProd(imgDocument.getDocName(), existing)) {
						// ALREADY MIGRATED DOCUMENT, need to remove existing document
						removeOK = alfRepoService.removeImagingDocument(imgDocument.getDocName());
						LOGGER.info("THIS DOCUMENT HAS BEEN REMOVED " + imgDocument.getDocName());
					} else {
						
						/*******************************************************
						 ** Flag document migrated
						 ********************************************************/
						LOGGER.info("Mark document '" + imgDocument.getDocName() + "(" + imgDocument.getLegacyDocId() + ")' as migrated");
						if (!uewiDocumentDao.updateDocumentStatus(imgDocument, MigratorUtil.STATUS_MIGRATED, imgDocument.getDocName(), environment)) {
							LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status " 
									+ MigratorUtil.STATUS_MIGRATED);
							throw new ImagingMigratorException();
						}
						
						skip = true;
					}
				}
			} else {
				// SET NEW NAME AND ARCHIVE only if not yet migrated document
				/*******************************************************
				 ** Generate new name
				 ********************************************************/
				// DOCUMENT NAME
				imgDocument.setDocName(ImagingDocumentMapper.mapDocumentName(imgDocument));

				/*******************************************************
				 ** Archive document
				 ********************************************************/

				try {
					archiveDocument(imgDocument);
				} catch (ImagingMigratorException e) {
					skip = true;
					if (e.getErrorCode() > 0) { // Managed errors
						LOGGER.error("Error when archiving document, mark record with error code " + e.getErrorCode());
						// Mark with special status in export database
						if (!uewiDocumentDao.updateDocumentStatus(imgDocument, e.getErrorCode(), null, environment)) {
							LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status " 
									+ e.getErrorCode());
							throw new ImagingMigratorException();
						}
					} else { // Non-managed errors
						throw e;
					}
				}
			}

			if (!skip) { // Skip all if archiving was not OK

				if (removeOK) {

					/*******************************************************
					 ** Create Document in Alfresco
					 ********************************************************/
					imgDocument.setDocNodeRef(alfRepoService.buildImagingDocument(companyHome, imgDocument));
					ByteArrayInputStream inputStream = null;
					try {
						inputStream = fileSystemService.getMainPdfAsStream(imgDocument);
					} catch (ImagingMigratorException e) {
						if (!uewiDocumentDao.updateDocumentStatus(imgDocument, 222, null, environment)) {
							LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 222");
							throw new ImagingMigratorException();
						}
					}

					imgDocument.setMainPdfNodeRef(alfRepoService.buildMainPDF(imgDocument, inputStream));
					/*******************************************************
					 ** Manage Notes/Comments
					 ********************************************************/
					if (imgDocument.getNotes() != null) {
						List<ImagingNote> notesToProcess = imgDocument.getNotes();
						alfRepoService.createNotes(imgDocument, notesToProcess);
					}
					/*******************************************************
					 ** Manage annotations
					 ********************************************************/
					String annotationFileText = annotationService.generateAnnotationFileText((JSONObject) jsonObject.get("annotations"));
					if (annotationFileText != null) {
						alfRepoService.addAnnotations(imgDocument, annotationFileText);
					}

					/*******************************************************
					 ** Manage ATU Imports
					 ********************************************************/
					List<File> atuImports = null;
					try {
						atuImports = fileSystemService.findValidATUImports(imgDocument);
					} catch (Exception e) {
						if (!uewiDocumentDao.updateDocumentStatus(imgDocument, 19, null, environment)) {
							LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 19");
							throw new ImagingMigratorException();
						}
					}

					if (atuImports.size() > 0) {
						try {
							alfRepoService.createImports(imgDocument.getDocNodeRef(), atuImports);
						} catch (Exception e) {
							if (!uewiDocumentDao.updateDocumentStatus(imgDocument, 19, null, environment)) {
								LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 19");
								throw new ImagingMigratorException();
							}
						}
					}

					/*******************************************************
					 ** Manage FSO Imports
					 ********************************************************/
					List<File> ffeImports = null;
					try {
						ffeImports = fileSystemService.findValidFFEImports(imgDocument);
					} catch (Exception e) {
						if (!uewiDocumentDao.updateDocumentStatus(imgDocument, 17, null, environment)) {
							LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 17");
							throw new ImagingMigratorException();
						}
					}

					if (ffeImports.size() > 0) {
						try {
							alfRepoService.createImports(imgDocument.getDocNodeRef(), ffeImports);
						} catch (Exception e) {
							if (!uewiDocumentDao.updateDocumentStatus(imgDocument, 17, null, environment)) {
								LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 17");
								throw new ImagingMigratorException();
							}
						}
					}

					/*******************************************************
					 ** Move document to repository or workflow
					 ********************************************************/

					if (imgDocument.isLegacyWip()) {
						alfRepoService.moveToWorkflow(companyHome, imgDocument);
					} else {
						try {
							alfRepoService.moveToRepository(companyHome, imgDocument);
						} catch (ImagingMigratorException e) {
							// Silently failed if needed
							if (e.getErrorCode() != MigratorUtil.SKIP_EXCEPTION) {
								throw e;
							} else {
								skip = true;
							}
						}
					}

					/*******************************************************
					 ** Flag document migrated
					 ********************************************************/
					if (!skip) {
						LOGGER.info("Mark document '" + imgDocument.getDocName() + "(" + imgDocument.getLegacyDocId() + ")' as migrated");
						if (!uewiDocumentDao.updateDocumentStatus(imgDocument, MigratorUtil.STATUS_MIGRATED, imgDocument.getDocName(), environment)) {
							LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status " 
									+ MigratorUtil.STATUS_MIGRATED);
							throw new ImagingMigratorException();
						}
					}

				} else {
					LOGGER.error("Cannot remove existing document '" + imgDocument.getDocName() + "(" + imgDocument.getLegacyDocId() + ")'");
					if (!uewiDocumentDao.updateDocumentStatus(imgDocument, MigratorUtil.STATUS_CANNOT_DELETE_PREVIOUS_DOCUMENT, null, environment)) {
						LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status "
								+ MigratorUtil.STATUS_CANNOT_DELETE_PREVIOUS_DOCUMENT);
						throw new ImagingMigratorException();
					}
				}

				/*******************************************************
				 ** Remove document from file system
				 ********************************************************/
				if (!skip) {
					//fileSystemService.removeMainPDF(imgDocument);
				}
			}
		}
	}

	/**
	 * Basic metadata mapping.
	 * 
	 * @param imgDocument the imaging document
	 * @param jsonObject the input json properties
	 */
	private void mapBasicMetadata(ImagingDocument imgDocument, JSONObject jsonObject) {

		/*******************************************************
		 ** MetaData mapping
		 ********************************************************/

		// Get original values from Json.
		String docInDateStr = (String) jsonObject.get("scandate");
		String docLinked = (String) jsonObject.get("linkstatus");
		String wfDossierNr = (String) jsonObject.get("dossiernr");
		String docLetter = (String) jsonObject.get("letter");
		String wfAssignee = (String) jsonObject.get("person");
		String docType = (String) jsonObject.get("doctype");
		String wfDossierStatus = (String) jsonObject.get("statusDossier");
		String docClass = (String) jsonObject.get("objectclass");

		// **** Will be mapped later on ****
		imgDocument.setDocType(docType);
		imgDocument.setWorkflowDossierStatus(ImagingDocumentMapper.mapStringToValue(wfDossierStatus));
		imgDocument.setDocClass(docClass);
		// *********************************

		// SOURCE
		// DEPENDENCIES :
		// -- LegacyDocId need to be set : OK
		// -- docType : need to be set : must be the ORIGINAL ONE (before mapping)
		// -- wfDossierStatus : need to be set : must be the ORIGINAL ONE (before mapping)
		// -- docClass : need to be set : must be the ORIGINAL ONE (before mapping)
		imgDocument.setDocSource(ImagingDocumentMapper.mapDocSource(imgDocument));

		// WF Dossier Status
		// No dependency
		imgDocument.setWorkflowDossierStatus(ImagingDocumentMapper.mapDossierStatus(wfDossierStatus));

		// DOCUMENT TYPE
		imgDocument.setDocType(ImagingDocumentMapper.mapDocType(docType));

		// SCAN DATE - IN DATE
		imgDocument.setDocInDate(ImagingDocumentMapper.mapDocInDate(docInDateStr));

		// LINK STATUS
		imgDocument.setDocLinked(ImagingDocumentMapper.mapLinkStatus(docLinked));

		// DOCUMENT CLASS
		imgDocument.setDocClass(ImagingDocumentMapper.mapDocClass(docClass));

		// WF DOSSIER NR
		if (!MigratorUtil.isNumeric(wfDossierNr)) {
			imgDocument.setAddDossierNrInNote(true);
		}
		imgDocument.setWorkflowDossierNr(wfDossierNr);

		// DOCUMENT LETTER
		imgDocument.setDocLetter(docLetter);

		// WF ASSIGNEE
		imgDocument.setWorkflowAssignee(wfAssignee);
	}

	/**
	 * Sign and send the document to archiving system.
	 * @param imgDocument the imaging document
	 * @throws ImagingMigratorException if error occurs
	 */
	private void archiveDocument(final ImagingDocument imgDocument) throws ImagingMigratorException {
		fileSystemService.signAndArchivePDF(imgDocument);
	}
}
