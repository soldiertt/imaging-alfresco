package be.fsoffe.imaging.scheduled;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Manage incoming documents from Upload (Out_Scan from Abbyy) and move them to DropZone2.
 * 
 * @author jbourlet
 *
 */
public class MoveToDropZone2Job implements Job {

	private static final Log LOGGER = LogFactory.getLog(MoveToDropZone2Job.class);
	
	@Override
	public void execute(JobExecutionContext jobexecutioncontect) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING MoveToDropZone2Job @@@");
		
		final NodeService nodeService = (NodeService) jobexecutioncontect.getJobDetail().getJobDataMap().get("nodeService");
		final FileFolderService fileFolderService = (FileFolderService) jobexecutioncontect.getJobDetail().getJobDataMap().get("fileFolderService");
		final ContentService contentService = (ContentService) jobexecutioncontect.getJobDetail().getJobDataMap().get("contentService");
		final String pathToImaging = (String) jobexecutioncontect.getJobDetail().getJobDataMap().get("pathToImaging");
		final TransactionService transactionService = (TransactionService) jobexecutioncontect.getJobDetail().
				getJobDataMap().get("transactionService");
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final RetryingTransactionCallback<Void> callbackInit = new RetryingTransactionCallback<Void>() {

			@Override
			public Void execute() throws Throwable {
				
				List<File> filesToMove = new ArrayList<File>();
				File outscanFolder = new File(pathToImaging);
				File[] listedFiles = outscanFolder.listFiles();
				if (listedFiles == null) {
					LOGGER.error("Cannot list folder " + pathToImaging);
				} else {
					for (int i = 0; i < listedFiles.length; i++) {
						filesToMove.add(listedFiles[i]);
					}
				}
				LOGGER.info("found " + filesToMove.size() + " to move.");
				
				NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home");
				List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(rootNode, ContentModel.ASSOC_CHILDREN, qname);
				if (assocRefs.size() == 1) { //CompanyHome
					
					NodeRef companyHome = assocRefs.get(0).getChildRef();
					NodeRef dropZone2 = fileFolderService.searchSimple(companyHome, "DropZone2");
					if (dropZone2 == null) {
						LOGGER.error("Unable to find DropZone2 folder !");
						throw new JobExecutionException();
					}
					
					for (File fileToMove : filesToMove) {
						/******************************************************* 
						** Create PDF document
						********************************************************/
						QName pdfName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileToMove.getName());
						Map<QName, Serializable> pdfProperties = new HashMap<QName, Serializable>(2);
						pdfProperties.put(ContentModel.PROP_NAME, fileToMove.getName());
						NodeRef movedPDF = nodeService.createNode(dropZone2, ContentModel.ASSOC_CONTAINS, pdfName, 
								ContentModel.TYPE_CONTENT, pdfProperties).getChildRef();
					    ContentWriter writer = contentService.getWriter(movedPDF, ContentModel.PROP_CONTENT, true);
					    writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
					    writer.setEncoding("UTF-8");
					    writer.putContent(new FileInputStream(fileToMove));
					    fileToMove.delete();
					}
					
				}
				return null;
			}
			
		};
		
		AuthenticationUtil.runAs(new RunAsWork<Void>() {
			@Override
			public Void doWork() {
				return txnHelper.doInTransaction(callbackInit);
			}
		}, AuthenticationUtil.SYSTEM_USER_NAME);
		
		LOGGER.info("@@@ ENDING MoveToDropZone2Job @@@");
	}
	
}
