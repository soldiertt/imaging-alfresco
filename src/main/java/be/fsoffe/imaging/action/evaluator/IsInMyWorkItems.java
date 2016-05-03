package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * Evaluator checking if document is in my workitem.
 * 
 * @author jbourlet
 *
 */
public class IsInMyWorkItems extends HasAspectWorkItem {

	private transient AuthenticationService authenticationService;
	
	@Override
	public boolean evaluate(NodeRef node) {
		if (super.evaluate(node)) { //HAS ASPECT WORK ITEM
			String workItemOwner = (String) getNodeService().getProperty(node, ImagingModel.PROP_FDS_ITEM_OWNER);
			return authenticationService.getCurrentUserName().equals(workItemOwner);
		}
		return false;
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
