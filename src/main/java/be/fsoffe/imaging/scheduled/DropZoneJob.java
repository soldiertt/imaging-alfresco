package be.fsoffe.imaging.scheduled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
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

import be.fsoffe.imaging.SignatureUtil;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.model.ImagingModel;

/**
 * Manage incoming documents from Kofax (source is either scanner or mail).
 * 
 * @author jbourlet
 *
 */
public class DropZoneJob implements Job {

	private static final Log LOGGER = LogFactory.getLog(DropZoneJob.class);
	
	@Override
	public void execute(JobExecutionContext jobexecutioncontect) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING DropZoneJob @@@");
		
		final String minutesToWait = (String) jobexecutioncontect.getJobDetail().getJobDataMap().get("minutestowait");
		final NodeService nodeService = (NodeService) jobexecutioncontect.getJobDetail().getJobDataMap().get("nodeService");
		final FileFolderService fileFolderService = (FileFolderService) jobexecutioncontect.getJobDetail().getJobDataMap().get("fileFolderService");
		final ContentService contentService = (ContentService) jobexecutioncontect.getJobDetail().getJobDataMap().get("contentService");
		final String pathToArchive = (String) jobexecutioncontect.getJobDetail().getJobDataMap().get("pathToArchive");
		final String pathToCertificate = (String) jobexecutioncontect.getJobDetail().getJobDataMap().get("pathToCertificate");
		final TransactionService transactionService = (TransactionService) jobexecutioncontect.getJobDetail().
				getJobDataMap().get("transactionService");
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final RetryingTransactionCallback<Void> callbackInit = new RetryingTransactionCallback<Void>() {

			@Override
			public Void execute() throws Throwable {
				Calendar xMinutesAgo = Calendar.getInstance();
				xMinutesAgo.add(Calendar.MINUTE, -(Integer.parseInt(minutesToWait)));
				
				NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home");
				List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(rootNode, ContentModel.ASSOC_CHILDREN, qname);
				if (assocRefs.size() == 1) { //CompanyHome
					LOGGER.debug("found companyhome");
					NodeRef companyHome = assocRefs.get(0).getChildRef();
					NodeRef dropZone = fileFolderService.searchSimple(companyHome, "DropZone");
					NodeRef entryFolder = fileFolderService.searchSimple(companyHome, "Entry");
					NodeRef failureFolder = fileFolderService.searchSimple(companyHome, "Failure");
					if (dropZone == null) {
						LOGGER.error("Unable to find DropZone folder !");
						throw new JobExecutionException();
					}
					LOGGER.debug("found dropzone : " + dropZone.toString());
					Set<QName> documentChildTypes = new HashSet<QName>();
					documentChildTypes.add(ImagingModel.TYPE_FDS_DOCUMENT);
					Set<QName> contentChildTypes = new HashSet<QName>();
					contentChildTypes.add(ImagingModel.TYPE_FDS_CONTENT);
					contentChildTypes.add(ContentModel.TYPE_CONTENT);
					List<ChildAssociationRef> documents = nodeService.getChildAssocs(dropZone, documentChildTypes);
					LOGGER.info("number of documents found : " + documents.size());
					
					NodeRef document; //fds:document dropped in folder
					String docName; //Original document name (Kofax id)
					Date docCreateDate; //Document create date
					String docSource; //Document source - fds:docSource
					Date docInDate; //Document in date - fds:docInDate
					String docType; //Document type
					SimpleDateFormat concatFormat = new SimpleDateFormat("yyyyMMddHHmmss");
					String formattedInDate; //Formatted inDate
					String docSourceAbb = ""; //Abbreviation of source
					String suffix = ""; //Last characters of name
					String finalDocName; //Final document name
					List<ChildAssociationRef> docChildren = null;
					
					for (ChildAssociationRef documentAssoc : documents) {
						document = documentAssoc.getChildRef();
						docName = (String) nodeService.getProperty(document, ContentModel.PROP_NAME);
						docCreateDate = (Date) nodeService.getProperty(document, ContentModel.PROP_CREATED);
						if (docCreateDate.before(xMinutesAgo.getTime())) {
							//OK TO TREAT THIS DOCUMENT
							docSource = (String) nodeService.getProperty(document, ImagingModel.PROP_FDS_DOC_SOURCE);
							docInDate = (Date) nodeService.getProperty(document, ImagingModel.PROP_FDS_DOC_INDATE);
							docType = (String) nodeService.getProperty(document, ImagingModel.PROP_FDS_DOC_TYPE);
							
							formattedInDate = concatFormat.format(docInDate);
							switch (docSource) {
								case "upload": docSourceAbb = "UPD"; break;
								case "scanner": docSourceAbb = "SCN"; break;
								case "mail": docSourceAbb = "EML"; break;
								case "printer": docSourceAbb = "PRN"; break;
								case "fax": docSourceAbb = "FAX"; break;
								default : docSourceAbb = "";
							}
							suffix = "000";
							if (docName.length() > 3) {
								suffix = docName.substring(docName.length() - 3);
							} else {
								suffix = ("00" + docName).substring(docName.length() - 1);
							}
							
							finalDocName = formattedInDate + docSourceAbb + suffix; //Node will be renamed when moved
							LOGGER.info("final name : " + finalDocName);
							
							// PROCESS CHILDREN
							docChildren = nodeService.getChildAssocs(document, contentChildTypes);
							LOGGER.debug("number of children found : " + docChildren.size());
							
							List<NodeRef> importsToKeep = new ArrayList<NodeRef>();
							NodeRef docChild;
							String childName;
							int lastDotIndex;
							String childExt = "";
							boolean hasMainDocumentArchived = false;
							
							for (ChildAssociationRef docChildAssoc : docChildren) {
								docChild = docChildAssoc.getChildRef();
								childName = (String) nodeService.getProperty(docChild, ContentModel.PROP_NAME);
								lastDotIndex = childName.lastIndexOf('.');
								childExt = "";
								if (lastDotIndex != -1 && lastDotIndex != childName.length() - 1) {
									childExt = childName.substring(lastDotIndex + 1).toLowerCase();
								} 
								LOGGER.debug("child extension :  " + childExt);
								if ((docName + ".pdf").equalsIgnoreCase(childName)) { // This is the main document
									ContentReader cReader = contentService.getReader(docChild, ContentModel.PROP_CONTENT);
									InputStream pdfStream = cReader.getContentInputStream();
									if (SignatureUtil.signAndArchivePDF(pdfStream, finalDocName, pathToArchive, pathToCertificate)) {
										hasMainDocumentArchived = true;
										nodeService.setProperty(docChild, ContentModel.PROP_NAME, finalDocName + ".pdf");
										if (Arrays.asList(ImagingModel.DOCTYPES_WITH_KEYWORDS_ASPECT).contains(docType)) {
											nodeService.addAspect(document, ImagingModel.ASPECT_KEYWORDS, null);
										}
										if (Arrays.asList(MigratorUtil.DOCTYPES_TO_INDEX_CONTENT).contains(docType)) {
											nodeService.setProperty(docChild, ContentModel.PROP_IS_CONTENT_INDEXED, true);
											forceReindex(contentService, docChild);
										}
									}
								} else { //This is an import
									if (childExt.equals("xls") || childExt.equals("xlsx") || childExt.equals("doc") || childExt.equals("docx") || childExt.equals("pdf")) {
										importsToKeep.add(docChild); //Keep xl, pdf, and doc files
									} else {
										nodeService.deleteNode(docChild); // Remove other imports
										LOGGER.debug("deleting " + childName + " ...");
									}
								}
								
							}
							
							LOGGER.debug("number of imports to keep : " + importsToKeep.size());
							if (importsToKeep.size() > 0) {
								QName importsFolderName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "imports");
								Map<QName, Serializable> importFolderProps = new HashMap<QName, Serializable>(1);
								importFolderProps.put(ContentModel.PROP_NAME, "imports"); 
								ChildAssociationRef importsFolder = nodeService.createNode(document, ContentModel.ASSOC_CONTAINS, 
										importsFolderName, ImagingModel.TYPE_FDS_FOLDER, importFolderProps);
								for (NodeRef importRef : importsToKeep) {
									fileFolderService.move(importRef, importsFolder.getChildRef(), null);
								}
							}
							if (hasMainDocumentArchived) {
								Map<QName, Serializable> archiveProps = new HashMap<QName, Serializable>(1);
								archiveProps.put(ImagingModel.PROP_FDS_ARCHIVED_DATE, Calendar.getInstance().getTime());
								nodeService.addAspect(document, ImagingModel.ASPECT_ARCHIVED, archiveProps);
								fileFolderService.move(document, entryFolder, finalDocName);
							} else {
								LOGGER.error("Document '" + docName + "' has no main document, moved to Failure");
								fileFolderService.move(document, failureFolder, finalDocName);
							}
						} else {
							LOGGER.info("Document is too young : " + docName);
						}
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
		
		LOGGER.info("@@@ ENDING DropZoneJob @@@");
	}
	
	/**
	 * When a specific document type is detected the document need to be indexed.
	 * @param contentService utility service
	 * @param document document reference
	 * @throws IOException if error occurs
	 */
	private void forceReindex(ContentService contentService, NodeRef document) throws IOException {
		//READ FILE CONTENT
		ContentReader reader = contentService.getReader(document, ContentModel.PROP_CONTENT);
	    InputStream originalInputStream = reader.getContentInputStream();
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    final int bufSize = 1 << 8; //1KiB buffer
	    byte[] buffer = new byte[bufSize];
	    int bytesRead = -1;
	    while ((bytesRead = originalInputStream.read(buffer)) > -1) {
	    	outputStream.write(buffer, 0, bytesRead);
	    }
	    originalInputStream.close();
	    byte[] binaryData = outputStream.toByteArray();
	    // WRITE FILE WITH SAME CONTENT TO FORCE REINDEX
	    ContentWriter writer = contentService.getWriter(document, ContentModel.PROP_CONTENT, true);
	    writer.putContent(new ByteArrayInputStream(binaryData));
	}

}
