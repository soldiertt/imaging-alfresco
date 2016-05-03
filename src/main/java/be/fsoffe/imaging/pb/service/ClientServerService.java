package be.fsoffe.imaging.pb.service;

import java.util.List;

import be.fsoffe.imaging.pb.dao.ClientServerDaoJdbc;
import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.model.Keyword;
import be.fsoffe.imaging.pb.model.LinkContext;

/**
 * Service to manage c/s interactions.
 * 
 * @author jbourlet
 *
 */
public class ClientServerService {
	
	ClientServerDaoJdbc csDao;

	public void setCsDao(ClientServerDaoJdbc csDao) {
		this.csDao = csDao;
	}

	/**
	 * Read the link context stored in the C/S database.
	 * @param userName the actual logged in user
	 * @return all valid contexts
	 * @throws ImagingPBException if error occurs
	 */
	public List<LinkContext> readContextForUser(String userName) throws ImagingPBException {
		return csDao.readContextForUser(userName);
	}
	
	/**
	 * Check if the document type is allowed to be linked based on the screen the user was on.
	 * 
	 * @param refScreen screen reference
	 * @param documentType the document type in imaging
	 * @return is linkable status
	 * @throws ImagingPBException if error occurs
	 */
	public boolean isLinkable(Long refScreen, String documentType) throws ImagingPBException {
		List<String> docTypesByScreen = csDao.getDocTypesByScreenId(refScreen);
		return (docTypesByScreen.contains(documentType));
	}
	
	/**
	 * Find a specific context based on the session id.
	 * 
	 * @param sessionId the PB session id
	 * @param userName the currently logged in users
	 * @return a unique context
	 * @throws ImagingPBException if error occurs
	 */
	public LinkContext getContextBySessionId(Long sessionId, String userName) throws ImagingPBException {
		return csDao.getContextBySessionId(sessionId, userName);
	}
	
	/**
	 * Link an Image to a C/S Dossier or do nothing if already linked.
	 * 
	 * @param documentName the document name
	 * @param documentType the document type
	 * @param context the link details
	 * @return true if ok, false if document is already linked with this context
	 * @throws ImagingPBException if error occurs
	 */
	public boolean linkImage(String documentName, String documentType, LinkContext context) throws ImagingPBException {
		Long docTypeId = csDao.getDocTypeIdByName(documentType);
		
		boolean alreadyLinkedSameContext = csDao.checkAlreadyLinked(documentName, docTypeId, context);
		if (alreadyLinkedSameContext) {
			return false;
		}
		
		csDao.linkImageToDossier(documentName, docTypeId, context);
		return true;
	}

	/**
	 * Find images based on a given context.
	 * 
	 * @param linkCtx the context
	 * @param maxItems the maximum items to return
	 * @return all references to images in string list
	 * @throws ImagingPBException if error occurs
	 */
	public List<String> findImagesByContext(LinkContext linkCtx, int maxItems) throws ImagingPBException {
		return csDao.findImagesByContext(linkCtx, maxItems);
	}

	/**
	 * Update GroupAnswer or Vrgl table depending the docType.
	 * Only for document with UPLOAD source coming from DropZone2.
	 * @param docTypeInt
	 * @param docName
	 * @throws ImagingPBException 
	 */
	public void updateGroupAnswerOrVrgl(Integer docTypeInt, String docName, Long questionId) throws ImagingPBException {
		if (docName != null && !"".equals(docName.trim()) && questionId > 0) {
			if (docTypeInt == 3 || docTypeInt == 12) {
				csDao.updateGroupAnswer(docName, questionId);
			} else if (docTypeInt == 5) {
				csDao.updateVrgl(docName, questionId);
			}
		}
	}

	/**
	 * List available keywords.
	 * 
	 * @return
	 * @throws ImagingPBException 
	 */
	public List<Keyword> findKeywords() throws ImagingPBException {
		return csDao.findKeywords();
	}
	
}
