package be.fsoffe.imaging.migrator.job;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import be.fsoffe.imaging.migrator.Environment;
import be.fsoffe.imaging.migrator.ImagingMigratorException;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.migrator.dao.ClientServerImageDao;
import be.fsoffe.imaging.migrator.dao.UewiDocumentDao;
import be.fsoffe.imaging.migrator.model.ImagingDocument;
import be.fsoffe.imaging.migrator.model.InDateFormat;
import be.fsoffe.imaging.migrator.service.AlfrescoRepositoryService;
import be.fsoffe.imaging.model.ImagingModel;

/**
 * Update migrated docs job.
 * 
 * @author jbourlet
 * 
 */
public class UpdateMigratedDocsJob implements Job {

	private static final Log LOGGER = LogFactory.getLog(UpdateMigratedDocsJob.class);

	private UewiDocumentDao uewiDocumentDao;
	private ClientServerImageDao csImageDao;
	
	private AlfrescoRepositoryService alfRepoService;
	private TransactionService transactionService;
	private RetryingTransactionHelper txnHelper;
	
	private Environment environment;
	
	/**
	 * KEEP A GLOBAL STATUS OF THE JOB.
	 */
	public static boolean jobRunning;
	
	
	@Override
	public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING Update migrated docs Job @@@");
		
		if (!UpdateMigratedDocsJob.jobRunning) {
			
			try {
				UpdateMigratedDocsJob.jobRunning = true;
			
				initializeMembers(jobexecutioncontext);
				
				/************************************************* 
				** Filter documents to update 
				** STATUS = 11
				** NOT Wrong DocType (NOTTOKEEP*)
				***************************************************/
				List<ImagingDocument> documentsToUpdate = new ArrayList<ImagingDocument>();
				try {
					documentsToUpdate = uewiDocumentDao.getDocumentsToUpdate();
				} catch (SQLException e) {
					LOGGER.error("Cannot retrieve documents to update !", e);
					throw new ImagingMigratorException();
				}
				
				for (final ImagingDocument docRow : documentsToUpdate) {
					
					AuthenticationUtil.runAs(new RunAsWork<Void>() {
						@Override
						public Void doWork() {
							return txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
								public Void execute() throws Throwable {
									updateDocument(docRow);
									return null;
								}
							});
						}
					}, AuthenticationUtil.SYSTEM_USER_NAME);
						
				}
			} finally {
				UpdateMigratedDocsJob.jobRunning = false;
			}
		} else {
			LOGGER.info("Update process still in progress ... do nothing.");
		}
		LOGGER.info("@@@ ENDING Update migrated docs Job @@@");
	}

	/**
	 * Initialize dao's and services.
	 *  
	 * @param jobexecutioncontext job execution context of the scheduled job
	 */
	private void initializeMembers(final JobExecutionContext jobexecutioncontext) {
		uewiDocumentDao = (UewiDocumentDao) jobexecutioncontext.getJobDetail().getJobDataMap().get("uewiDocumentDao");
		csImageDao = (ClientServerImageDao) jobexecutioncontext.getJobDetail().getJobDataMap().get("csImageDao");
		alfRepoService = (AlfrescoRepositoryService) jobexecutioncontext.getJobDetail().getJobDataMap().get("alfRepoService");
		transactionService = (TransactionService) jobexecutioncontext.getJobDetail().getJobDataMap().get("transactionService");
		txnHelper = transactionService.getRetryingTransactionHelper();
		environment = Environment.valueOf(jobexecutioncontext.getJobDetail().getJobDataMap().getString("environment"));
	}
	
	/**
	 * Main method of the update process.
	 * 
	 * @param imgDocument the imaging document
	 * @throws ImagingMigratorException if error occurs
	 */
	private void updateDocument(ImagingDocument imgDocument) throws ImagingMigratorException {
		
		boolean docNotFound = false;
		
		// 1. UPDATE DOCUMENT DOSSIER NUMBER
		if (imgDocument.getWorkflowDossierNr() != null
				&& MigratorUtil.isNumeric(imgDocument.getWorkflowDossierNr())
				&& Long.parseLong(imgDocument.getWorkflowDossierNr()) > 0) {
			if (!alfRepoService.updateSingleField(imgDocument.getDocName(), ImagingModel.PROP_FDS_DOC_DOSSIER_NR, imgDocument.getWorkflowDossierNr())) {
				LOGGER.error("Cannot find document '" + imgDocument.getDocName() + "' in Imaging repository !");
				docNotFound = true;
			} else {
				LOGGER.info("Updated '" +  imgDocument.getDocName() + "' with new dossier number : " + imgDocument.getWorkflowDossierNr());
			}
		}
		
		// 2. UPDATE PROCESSED BY FIELD
		try {
			String processedBy = csImageDao.getProcessedBy(imgDocument.getLegacyDocId());
			if (processedBy != null) {
				if (!alfRepoService.updateSingleField(imgDocument.getDocName(), ImagingModel.PROP_FDS_DOC_PROCESSED_BY, processedBy)) {
					LOGGER.error("Cannot find document '" + imgDocument.getDocName() + "' in Imaging repository !");
					docNotFound = true;
				} else {
					LOGGER.info("Updated '" +  imgDocument.getDocName() + "' with processedBy : " + processedBy);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Cannot retrieve processedBy user !", e);
			throw new ImagingMigratorException();
		}
		
		String newDocSourceAbb = null;
		String newDocSource = null;
		
		// 3. UPDATE SOURCE IF NEEDED
		if (imgDocument.getLegacyDocId().indexOf("FSO") != -1) {
			
			if ("MAIL".equals(imgDocument.getDocClass())) {
				newDocSourceAbb = "EML";
				newDocSource = "mail";
			} else if (InDateFormat.SHORT.equals(imgDocument.getDocInDateFormat()))  {
				newDocSourceAbb = "UPD";
				newDocSource = "upload";
			}
		}
		
		if (newDocSourceAbb != null) {
			// Need to rename the document and update the export table.
			// Locate OLD SOURCE
			String oldDocSourceAbb = imgDocument.getDocName().substring(14, 17);
			String newDocName = imgDocument.getDocName().replaceFirst(oldDocSourceAbb, newDocSourceAbb);
			// SET THE NEW SOURCE AND RENAME THE DOCUMENT
			if (!alfRepoService.updateSingleField(imgDocument.getDocName(), ImagingModel.PROP_FDS_DOC_SOURCE, newDocSource)) {
				LOGGER.error("Cannot find document '" + imgDocument.getDocName() + "' in Imaging repository !");
				docNotFound = true;
			} else {
				LOGGER.info("Updated '" +  imgDocument.getDocName() + "' with source : " + newDocSource);
				imgDocument.setDocSource(newDocSource);
				if (alfRepoService.renameImagingDocument(imgDocument.getDocName(), newDocName)) {
					LOGGER.info("Updated '" +  imgDocument.getDocName() + "' with new name : " + newDocName);
					imgDocument.setDocName(newDocName);
				} else {
					LOGGER.error("Cannot find document '" + imgDocument.getDocName() + "' in Imaging repository !");
					docNotFound = true;
				}
			}
		}
		
		if (!docNotFound) {
			LOGGER.info("Updating '" +  imgDocument.getDocName() + "' with status " + MigratorUtil.STATUS_MIGRATED_AND_UPDATED);
			// Update export table
			if (!uewiDocumentDao.updateDocumentStatus(imgDocument, MigratorUtil.STATUS_MIGRATED_AND_UPDATED, imgDocument.getDocName(), environment)) {
				LOGGER.error("Document '" + imgDocument.getLegacyDocId() 
						+ "' cannot be marked with status " + MigratorUtil.STATUS_MIGRATED_AND_UPDATED);
				throw new ImagingMigratorException();
			}
		} else {
			LOGGER.info("Updating '" +  imgDocument.getDocName() + "' with status " + MigratorUtil.STATUS_DOC_NOT_FOUND_IN_IMAGING);
			// Update export table
			if (!uewiDocumentDao.updateDocumentStatus(imgDocument, MigratorUtil.STATUS_DOC_NOT_FOUND_IN_IMAGING, imgDocument.getDocName(), environment)) {
				LOGGER.error("Document '" + imgDocument.getLegacyDocId() 
						+ "' cannot be marked with status " + MigratorUtil.STATUS_DOC_NOT_FOUND_IN_IMAGING);
				throw new ImagingMigratorException();
			}
		}
	}
}
