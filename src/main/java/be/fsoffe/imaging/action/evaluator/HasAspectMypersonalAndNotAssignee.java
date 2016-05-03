package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * Evaluator checking if the document has MyPersonal aspect and the current user is not the assignee.
 * 
 * @author jbourlet
 *
 */
public class HasAspectMypersonalAndNotAssignee extends RegisteredActionEvaluator {

	private transient AuthenticationService authenticationService;
	
	private transient NodeService nodeService;
	
	@Override
	public boolean evaluate(final NodeRef node) {
		boolean hasAspectMyPersonal = nodeService.hasAspect(node, ImagingModel.ASPECT_MYPERSONAL);
		String mypersAssignee = (String) getNodeService().getProperty(node, ImagingModel.PROP_FDS_MYPERS_ASSIGNEE);
		return hasAspectMyPersonal && !authenticationService.getCurrentUserName().equals(mypersAssignee);
	}

	/**
	 * Set the nodeService.
	 * @param nodeService the service to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Get the nodeService.
	 * @return nodeService
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
	
}
