package be.fsoffe.imaging.migrator.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import be.fsoffe.imaging.migrator.ImagingMigratorException;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.migrator.model.ImagingDocument;
import be.fsoffe.imaging.migrator.model.ImagingNote;
import be.fsoffe.imaging.model.ImagingModel;
/**
 * Service to interact with Alfresco repository, getting and saving objects.
 * 
 * @author jbourlet
 *
 */
public class AlfrescoRepositoryService {
	
	private static final Log LOGGER = LogFactory.getLog(AlfrescoRepositoryService.class);
	
	private NodeService nodeService;
	private SearchService searchService;
	private ContentService contentService;
	private FileFolderService fileFolderService;
	private MimetypeService mimetypeService;

	/**
	 * Get the alfresco root folder.
	 * @return companyHome nodeRef
	 */
	public NodeRef getCompanyHome() {
		NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home");
		List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(rootNode, ContentModel.ASSOC_CHILDREN, qname);
		if (assocRefs.size() == 1) {
			LOGGER.debug("found companyhome");
			return assocRefs.get(0).getChildRef();
		}
		LOGGER.error("cannot find companyhome !!");
		return null;
	}
	
	/**
	 * Remove an existing imaging document from the repository.
	 * @param documentName the actual document name
	 * @return success status
	 */
	public boolean removeImagingDocument(String documentName) {
		boolean removeOK = false;
		String query = "EXACTTYPE:\"fds:document\" AND @cm\\:name:\"" + documentName + "\"";
		List<NodeRef> docToRemove = executeQuery(query);
		if (docToRemove.size() == 1) {
			nodeService.deleteNode(docToRemove.get(0));
			removeOK = true;
		}
		return removeOK;
	}
	
	/**
	 * Rename an existing imaging document.
	 * @param originalDocumentName the actual document name
	 * @param newDocumentName the new document name
	 * @return success status
	 */
	public boolean renameImagingDocument(String originalDocumentName, String newDocumentName) {
		boolean renameOK = false;
		// Check whether the newname already exists
		String query = "EXACTTYPE:\"fds:document\" AND @cm\\:name:\"" + newDocumentName + "\"";
		List<NodeRef> docToRename = executeQuery(query);
		if (docToRename.size() == 1) {
			// The name already exists. Change it to avoid collision
			int lower = 100;
			int upper = 999;
			int r=100;
			boolean nameOk = false;
			while (!nameOk) {
				r = (int) (Math.random() * (upper - lower)) + lower;
				if ((newDocumentName != newDocumentName.substring(0,17) + r)) {
					newDocumentName = newDocumentName.substring(0,17) + r;
					nameOk = true;
				}
			}
		}
		query = "EXACTTYPE:\"fds:document\" AND @cm\\:name:\"" + originalDocumentName + "\"";
		docToRename = executeQuery(query);
		if (docToRename.size() == 1) {
			nodeService.setProperty(docToRename.get(0), ContentModel.PROP_NAME, newDocumentName);
			renameOK = true;
		}
		return renameOK;
	}
	
	/**
	 * Build the main parts of the imaging document with all its aspects and properties.
	 * 
	 * @param companyHome this will be the temporary parent of the imaging document
	 * @param imgDocument the imaging document
	 * @return the newly created document reference
	 */
	@SuppressWarnings("unchecked")
	public NodeRef buildImagingDocument(NodeRef companyHome, ImagingDocument imgDocument) {
		
		// A. GENERAL DOCUMENT PROPERTIES
		Map<QName, Serializable> documentProps = new HashMap<QName, Serializable>(7);
		QName imagingDocumentName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, imgDocument.getDocName());
		documentProps.put(ContentModel.PROP_NAME, imgDocument.getDocName());
		documentProps.put(ImagingModel.PROP_FDS_DOC_SOURCE, imgDocument.getDocSource());
		documentProps.put(ImagingModel.PROP_FDS_DOC_TYPE, imgDocument.getDocType());
		documentProps.put(ImagingModel.PROP_FDS_DOC_CLASS, imgDocument.getDocClass());
		documentProps.put(ImagingModel.PROP_FDS_DOC_INDATE, imgDocument.getDocInDate());
		documentProps.put(ImagingModel.PROP_FDS_DOC_LETTER, imgDocument.getDocLetter());
		documentProps.put(ImagingModel.PROP_FDS_DOC_LINKED, imgDocument.isDocLinked());
		
		NodeRef imagingDocumentRef = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, imagingDocumentName, 
				ImagingModel.TYPE_FDS_DOCUMENT, documentProps).getChildRef();
		
		// B. ARCHIVE ASPECT
		Map<QName, Serializable> archiveProps = new HashMap<QName, Serializable>(1);
		archiveProps.put(ImagingModel.PROP_FDS_ARCHIVED_DATE, Calendar.getInstance().getTime());
		nodeService.addAspect(imagingDocumentRef, ImagingModel.ASPECT_ARCHIVED, archiveProps);
		
		// C. LEGACY ASPECT
		Map<QName, Serializable> legacyProps = new HashMap<QName, Serializable>(2);
		legacyProps.put(ImagingModel.PROP_FDS_LEGACY_DOCID, imgDocument.getLegacyDocId());
		legacyProps.put(ImagingModel.PROP_FDS_LEGACY_WIP, imgDocument.isLegacyWip());
		nodeService.addAspect(imagingDocumentRef, ImagingModel.ASPECT_LEGACY_DOC, legacyProps);
		
		// E. EXTRA JSON FOR WORKFLOW
		if (imgDocument.isLegacyWip()) {
			JSONObject jsonPropertiesObj = new JSONObject();
			if (MigratorUtil.isNumeric(imgDocument.getWorkflowDossierNr())) {
				jsonPropertiesObj.put("dossiernr", imgDocument.getWorkflowDossierNr());
			}
			jsonPropertiesObj.put("dossierstatus", imgDocument.getWorkflowDossierStatus());
			QName jsonPropertiesName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, MigratorUtil.JSON_OUTPUT_FILENAME);
			Map<QName, Serializable> jsonProps = new HashMap<QName, Serializable>(1);
			jsonProps.put(ContentModel.PROP_NAME, MigratorUtil.JSON_OUTPUT_FILENAME);
			NodeRef jsonProperties = nodeService.createNode(imagingDocumentRef, ContentModel.ASSOC_CONTAINS, jsonPropertiesName, 
					ContentModel.TYPE_CONTENT, jsonProps).getChildRef();
	        ContentWriter writer = contentService.getWriter(jsonProperties, ContentModel.PROP_CONTENT, true);
	        writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
	        writer.setEncoding("UTF-8");
	        writer.putContent(jsonPropertiesObj.toJSONString());
	        //imgDocument.getWorkflowAssignee() => ignored
		}
		
		return imagingDocumentRef;
	}

	/**
	 * Build the pdf file and its properties inside the imaging document.
	 * 
	 * @param imgDocument the imaging document
	 * @param inputStream inputStream of the pdf file to create
	 * @return the newly created pdf file reference
	 */
	public NodeRef buildMainPDF(ImagingDocument imgDocument, ByteArrayInputStream inputStream) {
		/******************************************************* 
		** Attach main PDF document
		********************************************************/
		QName imagingDocumentName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, imgDocument.getDocName() + ".pdf");
		Map<QName, Serializable> mainPdfContentProperties = new HashMap<QName, Serializable>(2);
		mainPdfContentProperties.put(ContentModel.PROP_NAME, imgDocument.getDocName() + ".pdf");
		/******************************************************* 
		** Manage indexControl aspect
		********************************************************/
		mainPdfContentProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Arrays.asList(MigratorUtil.DOCTYPES_TO_INDEX_CONTENT)
				.contains(imgDocument.getDocType()));
		NodeRef mainPDF = nodeService.createNode(imgDocument.getDocNodeRef(), ContentModel.ASSOC_CONTAINS, imagingDocumentName, 
				ImagingModel.TYPE_FDS_CONTENT, mainPdfContentProperties).getChildRef();
	    ContentWriter writer = contentService.getWriter(mainPDF, ContentModel.PROP_CONTENT, true);
	    writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
	    writer.setEncoding("UTF-8");
	    writer.putContent(inputStream);
	    return mainPDF;
	}
	
	/**
	 * Generate the annotation text file to attach to the main pdf document.
	 * @param imgDocument the imaging document
	 * @param annotationFileText the content of the annotation file as text
	 */
	public void addAnnotations(ImagingDocument imgDocument, String annotationFileText) {
		String annotationFileName = imgDocument.getDocName() + ".pdf.ant";
		QName annotationFileQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, annotationFileName);
		Map<QName, Serializable> annotationFileProps = new HashMap<QName, Serializable>(1);
		annotationFileProps.put(ContentModel.PROP_NAME, annotationFileName);
		NodeRef annotationFile = nodeService.createNode(imgDocument.getMainPdfNodeRef(), MigratorUtil.ASSOC_ANNOT_CONTAINS, annotationFileQName, 
				ContentModel.TYPE_CONTENT, annotationFileProps).getChildRef();
        ContentWriter writer = contentService.getWriter(annotationFile, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(annotationFileText);
	}
	
	/**
	 * Create the imports files below 'imports' folder based on a list of files.
	 * @param documentRef parent document reference
	 * @param imports the list of imports
	 * @throws ImagingMigratorException 
	 */
	public void createImports(NodeRef documentRef, List<File> imports) throws ImagingMigratorException {
		LOGGER.info("Number of imports : " + imports.size());
		// The folder may already exists if doc contains ATU + FFE Imports
		NodeRef importsFolder = fileFolderService.searchSimple(documentRef, "imports");
		if (importsFolder == null) {
			importsFolder = fileFolderService.create(documentRef, "imports", ImagingModel.TYPE_FDS_FOLDER).getNodeRef();
		}
		for (File validImport : imports) {
			//CREATE IMPORT
			NodeRef importNode = fileFolderService.create(importsFolder, validImport.getName(), ImagingModel.TYPE_FDS_CONTENT).getNodeRef();
			// WRITE FILE CONTENT
		    ContentWriter writer = contentService.getWriter(importNode, ContentModel.PROP_CONTENT, true);
		    writer.setMimetype(MigratorUtil.getMimeTypeForFileName(mimetypeService, validImport.getName()));
		    writer.setEncoding("UTF-8");
		    try {
				writer.putContent(new ByteArrayInputStream(FileUtils.readFileToByteArray(validImport)));
			} catch (ContentIOException e) {
				LOGGER.error("ERROR creating import !", e);
				throw new ImagingMigratorException();
			} catch (IOException e) {
				LOGGER.error("ERROR creating import !", e);
				throw new ImagingMigratorException();
			}
		}
	}
	
	/**
	 * Complete the imports files below 'imports' folder based on a list of files.
	 * @param documentRef parent document reference
	 * @param imports the list of imports
	 * @throws ImagingMigratorException 
	 */
	public void completeImports(NodeRef documentRef, List<File> imports) throws ImagingMigratorException {
		LOGGER.info("Number of imports : " + imports.size());
		// The folder may already exists if doc contains ATU + FFE Imports
		NodeRef importsFolder = fileFolderService.searchSimple(documentRef, "imports");
		if (importsFolder == null) {
			importsFolder = fileFolderService.create(documentRef, "imports", ImagingModel.TYPE_FDS_FOLDER).getNodeRef();
		}
		for (File validImport : imports) {
			NodeRef importDocument = fileFolderService.searchSimple(importsFolder, validImport.getName());
			if (importDocument == null) {
				LOGGER.info("Creating new import : '" + validImport.getName() + "'");
				//CREATE IMPORT
				NodeRef importNode = fileFolderService.create(importsFolder, validImport.getName(), ImagingModel.TYPE_FDS_CONTENT).getNodeRef();
				// WRITE FILE CONTENT
			    ContentWriter writer = contentService.getWriter(importNode, ContentModel.PROP_CONTENT, true);
			    writer.setMimetype(MigratorUtil.getMimeTypeForFileName(mimetypeService, validImport.getName()));
			    writer.setEncoding("UTF-8");
			    try {
					writer.putContent(new ByteArrayInputStream(FileUtils.readFileToByteArray(validImport)));
				} catch (ContentIOException e) {
					LOGGER.error("ERROR creating import !", e);
					throw new ImagingMigratorException();
				} catch (IOException e) {
					LOGGER.error("ERROR creating import !", e);
					throw new ImagingMigratorException();
				}
			}
		}
	}
	
	/**
	 * Create the forum and notes/comments attached to the main pdf document.
	 * @param imgDocument the imaging document
	 * @param notesToProcess all ImagingNote to add
	 */
	public void createNotes(ImagingDocument imgDocument, List<ImagingNote> notesToProcess) {
		QName forumQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Discussion " + imgDocument.getDocName());
		NodeRef forum = nodeService.createNode(imgDocument.getMainPdfNodeRef(), ForumModel.ASSOC_DISCUSSION, 
				forumQName, ForumModel.TYPE_FORUM).getChildRef();
		NodeRef topic = fileFolderService.create(forum, "Comments", ForumModel.TYPE_TOPIC).getNodeRef();
		
		int postId = 1;
		if (imgDocument.isAddDossierNrInNote()) {
			NodeRef post = fileFolderService.create(topic, "comment-" + postId++, ForumModel.TYPE_POST).getNodeRef();
			ContentWriter writer = contentService.getWriter(post, ContentModel.PROP_CONTENT, true);
			writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
	        writer.setEncoding("UTF-8");
	        String noteBody = "<p><b>Dossier number :</b> " + imgDocument.getWorkflowDossierNr() + "</p>";
	        writer.putContent(noteBody);
		}
		for (ImagingNote imagingNote : notesToProcess) {
			NodeRef post = fileFolderService.create(topic, "comment-" + postId++, ForumModel.TYPE_POST).getNodeRef();
			ContentWriter writer = contentService.getWriter(post, ContentModel.PROP_CONTENT, true);
			writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
	        writer.setEncoding("UTF-8");
	        String noteBody = "<p><u>" + imagingNote.getAuthor() + ":</u></p><p>" + imagingNote.getText().replaceAll("~~", "<br/>") + "</p>";
	        writer.putContent(noteBody);
		}
	}
	
	/**
	 * Move the document to the imaging workflow entry point.
	 * @param companyHome root reference
	 * @param imgDocument imaging document
	 * @throws ImagingMigratorException if error occurs
	 */
	public void moveToWorkflow(NodeRef companyHome, ImagingDocument imgDocument) throws ImagingMigratorException {
		NodeRef entryFolder = fileFolderService.searchSimple(companyHome, MigratorUtil.WORKFLOW_ENTRY_POINT_FOLDER);
		try {
			fileFolderService.move(imgDocument.getDocNodeRef(), entryFolder, null);
		} catch (FileExistsException e) {
			LOGGER.error("Document already exists at destination !", e);
			throw new ImagingMigratorException();
		} catch (FileNotFoundException e) {
			LOGGER.error("Cannot find document to move !", e);
			throw new ImagingMigratorException();
		}
	}
	
	/**
	 * Move the document to the imaging repository.
	 * @param companyHome root reference
	 * @param imgDocument imaging document
	 * @throws ImagingMigratorException if error occurs
	 */
	public void moveToRepository(NodeRef companyHome, ImagingDocument imgDocument) throws ImagingMigratorException {
		NodeRef repoDestination = MigratorUtil.createDestinationPath(fileFolderService, companyHome, imgDocument.getDocInDate());
		try {
			fileFolderService.move(imgDocument.getDocNodeRef(), repoDestination, null);
		} catch (FileExistsException e) {
			LOGGER.error("Document already exists at destination !", e);
			// Skip exception, happens when document name is the same a previous generated one.
			// Will try again later with another suffix, should pass.
			throw new ImagingMigratorException(MigratorUtil.SKIP_EXCEPTION);
		} catch (FileNotFoundException e) {
			LOGGER.error("Cannot find document to move !", e);
			throw new ImagingMigratorException();
		}
	}
	
	/** 
	 * Update a single field.
	 * 
	 * @param documentName
	 * @param propertyQName
	 * @param propertyValue
	 * @return
	 */
	public boolean updateSingleField(String documentName, QName propertyQName, String propertyValue) {
		boolean updateOK = false;
		String query = "EXACTTYPE:\"fds:document\" AND @cm\\:name:\"" + documentName + "\"";
		List<NodeRef> docToUpdate = executeQuery(query);
		if (docToUpdate.size() == 1) {
			nodeService.setProperty(docToUpdate.get(0), propertyQName, propertyValue);
			updateOK = true;
		}
		return updateOK;
		
	}
	
	public NodeRef getNodeRef(String documentName) {
		String query = "EXACTTYPE:\"fds:document\" AND @cm\\:name:\"" + documentName + "\"";
		List<NodeRef> findDoc = executeQuery(query);
		if (findDoc.size() == 1) {
			return findDoc.get(0);
		}
		return null;
	}
	
	public boolean annotUpdatedSinceProd(String documentName, NodeRef docNodeRef) {
		Calendar prodDate = Calendar.getInstance();
		prodDate.set(2015, 4, 26, 0, 0); //26-05-2015 00:00
		NodeRef pdfFile = fileFolderService.searchSimple(docNodeRef, documentName + ".pdf");
		if (pdfFile != null) {
			Date modifyDate = (Date) nodeService.getProperty(pdfFile, ContentModel.PROP_MODIFIED);
			LOGGER.info("MODIFIED DATE: " + modifyDate);
			LOGGER.info("PROD DATE: " + prodDate.getTime());
			return modifyDate.after(prodDate.getTime());
		}
		return false;
	}
	
	/**
	 * Execute a lucene search query.
	 * @param query the lucene query as a string
	 * @return the resulting nodes
	 */
	private List<NodeRef> executeQuery(String query) {
		
		SearchParameters sp = new SearchParameters();
		org.alfresco.service.cmr.search.ResultSet searchResults = null;
		List<NodeRef> results = new ArrayList<NodeRef>();
		
		try {
			sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
	        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
			sp.setQuery(query);
			searchResults = searchService.query(sp);
			results = searchResults.getNodeRefs();
			
		} finally {
		    if (searchResults != null) {
		    	searchResults.close();
		    }
		}
		
		return results;
		
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

}
