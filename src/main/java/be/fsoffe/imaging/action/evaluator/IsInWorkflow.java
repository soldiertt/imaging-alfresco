package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * Evaluator checking if the document is a workflow.
 * Document can be in a normal or mypersonal workflow.
 * 
 * @author jbourlet
 *
 */
public class IsInWorkflow extends RegisteredActionEvaluator {

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
	 * Set the authenticationService.
	 * @param authenticationService the service to set
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	
	@Override
	public boolean evaluate(NodeRef node) {
		
		boolean isinmypersonal = false;
		boolean haswfaspect = nodeService.hasAspect(node, ImagingModel.ASPECT_WORKFLOW);
		boolean hasmypersaspect = nodeService.hasAspect(node, ImagingModel.ASPECT_MYPERSONAL);
		
		if (hasmypersaspect) {
			String mypersAssignee = (String) nodeService.getProperty(node, ImagingModel.PROP_FDS_MYPERS_ASSIGNEE);
			isinmypersonal = authenticationService.getCurrentUserName().equals(mypersAssignee);
		}
		
		return haswfaspect || isinmypersonal;
	}

}
