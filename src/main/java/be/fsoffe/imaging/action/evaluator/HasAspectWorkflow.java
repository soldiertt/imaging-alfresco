package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * Evaluator checking if document has workflow aspect.
 * 
 * @author jbourlet
 *
 */
public class HasAspectWorkflow extends RegisteredActionEvaluator {

	private transient NodeService nodeService;
	
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

	@Override
	public boolean evaluate(NodeRef node) {
		return nodeService.hasAspect(node, ImagingModel.ASPECT_WORKFLOW);
	}

}
