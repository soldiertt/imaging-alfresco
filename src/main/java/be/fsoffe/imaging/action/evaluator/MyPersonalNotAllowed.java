package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * Evaluator checking if document is in a my personal workflow but i cannot send it for any reason.
 * @author jbourlet
 *
 */
public class MyPersonalNotAllowed extends RegisteredActionEvaluator {

	private transient NodeService nodeService;
	
	private transient AuthenticationService authenticationService;
	
	/**
	 * Set the nodeService.
	 * @param nodeService the service to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Get the nodeService.
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Set the authenticationService.
	 * @param authenticationService the service to set
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * Get the authenticationService.
	 * @return authenticationService
	 */
	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}
	
	@Override
	public boolean evaluate(NodeRef node) {
		
		if (nodeService.hasAspect(node, ImagingModel.ASPECT_MYPERSONAL)) {
			String mypersAssignee = (String) nodeService.getProperty(node, ImagingModel.PROP_FDS_MYPERS_ASSIGNEE);
			if (!mypersAssignee.equals(authenticationService.getCurrentUserName())) {
				return true; // I'm not the assignee of the mypersonal workflow, document found via search, i cannot send again the document.
			}
		}
		return false;
	}

}
