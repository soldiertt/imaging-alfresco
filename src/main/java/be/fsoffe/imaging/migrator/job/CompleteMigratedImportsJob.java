package be.fsoffe.imaging.migrator.job;

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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import be.fsoffe.imaging.migrator.Environment;
import be.fsoffe.imaging.migrator.ImagingMigratorException;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.migrator.dao.UewiDocumentDao;
import be.fsoffe.imaging.migrator.model.ImagingDocument;
import be.fsoffe.imaging.migrator.service.AlfrescoRepositoryService;
import be.fsoffe.imaging.migrator.service.FileSystemService;

public class CompleteMigratedImportsJob implements Job {

	private static final Log LOGGER = LogFactory.getLog(CompleteMigratedImportsJob.class);

	private UewiDocumentDao uewiDocumentDao;
	
	private AlfrescoRepositoryService alfRepoService;
	private TransactionService transactionService;
	private FileSystemService fileSystemService;
	private RetryingTransactionHelper txnHelper;
	
	private Environment environment;
	
	/**
	 * KEEP A GLOBAL STATUS OF THE JOB.
	 */
	public static boolean jobRunning;
	
	@Override
	public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING Complete Migrated Imports Job @@@");
		
		if (!CompleteMigratedImportsJob.jobRunning) {
			
			try {
				CompleteMigratedImportsJob.jobRunning = true;
			
				initializeMembers(jobexecutioncontext);
				
				/************************************************* 
				** Filter documents to process
				** STATUS = 11 or 110
				***************************************************/
				List<ImagingDocument> documentsToCompleteImports = new ArrayList<ImagingDocument>();
				try {
					documentsToCompleteImports = uewiDocumentDao.getDocumentsToCompleteImports();
					LOGGER.info("find " + documentsToCompleteImports.size() + " documents to complete.");
				} catch (SQLException e) {
					LOGGER.error("Cannot retrieve documents to complete imports !", e);
					throw new ImagingMigratorException();
				}
				
				for (final ImagingDocument docRow : documentsToCompleteImports) {
					
					AuthenticationUtil.runAs(new RunAsWork<Void>() {
						@Override
						public Void doWork() {
							return txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
								public Void execute() throws Throwable {
									completeDocument(docRow);
									return null;
								}
							});
						}
					}, AuthenticationUtil.SYSTEM_USER_NAME);
						
				}
			} finally {
				CompleteMigratedImportsJob.jobRunning = false;
			}
		} else {
			LOGGER.info("Complete imports process still in progress ... do nothing.");
		}
		LOGGER.info("@@@ ENDING complete migrated imports Job @@@");
	}

	/**
	 * Initialize dao's and services.
	 *  
	 * @param jobexecutioncontext job execution context of the scheduled job
	 */
	private void initializeMembers(final JobExecutionContext jobexecutioncontext) {
		uewiDocumentDao = (UewiDocumentDao) jobexecutioncontext.getJobDetail().getJobDataMap().get("uewiDocumentDao");
		alfRepoService = (AlfrescoRepositoryService) jobexecutioncontext.getJobDetail().getJobDataMap().get("alfRepoService");
		transactionService = (TransactionService) jobexecutioncontext.getJobDetail().getJobDataMap().get("transactionService");
		fileSystemService = (FileSystemService) jobexecutioncontext.getJobDetail().getJobDataMap().get("fileSystemService");
		txnHelper = transactionService.getRetryingTransactionHelper();
		environment = Environment.valueOf(jobexecutioncontext.getJobDetail().getJobDataMap().getString("environment"));
	}
	
	/**
	 * Main method of the complete imports process.
	 * 
	 * @param imgDocument the imaging document
	 * @throws ImagingMigratorException if error occurs
	 */
	private void completeDocument(ImagingDocument imgDocument) throws ImagingMigratorException {
		
		List<File> ffeImports = null;
		List<File> atuImports = null;
		
		LOGGER.info("Completing document '" + imgDocument.getLegacyDocId() + "'");
			
		/*******************************************************
		 ** List FSO Imports
		 ********************************************************/
		try {
			ffeImports = fileSystemService.tridFolderAndFindValidFFEImports(imgDocument);
		} catch (Exception e) {
			if (!uewiDocumentDao.updateDocumentImportStatus(imgDocument, 17, environment)) {
				LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 17");
				throw new ImagingMigratorException();
			}
		}

		/*******************************************************
		 ** List ATU Imports
		 ********************************************************/
		if (imgDocument.isHasAtu()) {
			try {
				atuImports = fileSystemService.tridFolderAndFindValidATUImports(imgDocument);
			} catch (Exception e) {
				if (!uewiDocumentDao.updateDocumentImportStatus(imgDocument, 19, environment)) {
					LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 19");
					throw new ImagingMigratorException();
				}
			}
		}
		
		/*******************************************************
		 ** Check if any import to check 
		 ********************************************************/
		if (ffeImports.size() > 0 || (atuImports != null && atuImports.size() > 0)) {
			
			// Find document and set its nodeRef
			NodeRef nodeRef = alfRepoService.getNodeRef(imgDocument.getDocName());
			if (nodeRef != null) {
				imgDocument.setDocNodeRef(nodeRef);
				
				// INTERNAL IMPORTS
				processImports(imgDocument, ffeImports);
				
				if (atuImports != null) {
					// ATU IMPORTS
					processImports(imgDocument, atuImports);
				}
				
			} else {
				LOGGER.info("Updating '" +  imgDocument.getDocName() + "' with import_status " + MigratorUtil.STATUS_DOC_NOT_FOUND_IN_IMAGING);
				// Update export table
				if (!uewiDocumentDao.updateDocumentImportStatus(imgDocument, MigratorUtil.STATUS_DOC_NOT_FOUND_IN_IMAGING, environment)) {
					LOGGER.error("Document '" + imgDocument.getLegacyDocId() 
							+ "' cannot be marked with import_status " + MigratorUtil.STATUS_DOC_NOT_FOUND_IN_IMAGING);
					throw new ImagingMigratorException();
				}
			}
		}
		
		LOGGER.info("Updating '" +  imgDocument.getDocName() + "' with import_status " + MigratorUtil.STATUS_MIGRATED_AND_COMPLETED_IMPORTS);
		// Update export table
		if (!uewiDocumentDao.updateDocumentImportStatus(imgDocument, MigratorUtil.STATUS_MIGRATED_AND_COMPLETED_IMPORTS, environment)) {
			LOGGER.error("Document '" + imgDocument.getLegacyDocId() 
					+ "' cannot be marked with import_status " + MigratorUtil.STATUS_MIGRATED_AND_COMPLETED_IMPORTS);
			throw new ImagingMigratorException();
		}
		
	}
	
	private void processImports(ImagingDocument imgDocument, List<File> imports) throws ImagingMigratorException {
		if (imports.size() > 0) {
			try {
				alfRepoService.completeImports(imgDocument.getDocNodeRef(), imports);
			} catch (Exception e) {
				if (!uewiDocumentDao.updateDocumentImportStatus(imgDocument, 18, environment)) {
					LOGGER.error("Document '" + imgDocument.getLegacyDocId() + "' cannot be marked with status 18");
					throw new ImagingMigratorException();
				}
			}
		}
	}
}
