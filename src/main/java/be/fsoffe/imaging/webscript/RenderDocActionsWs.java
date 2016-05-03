package be.fsoffe.imaging.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.action.evaluator.ActionEvaluator;
import be.fsoffe.imaging.action.evaluator.EvaluatorRegistry;
import be.fsoffe.imaging.model.ImagingAction;
import be.fsoffe.imaging.model.ImagingActionEvaluator;

/**
 * 
 * 
 * @author jlbourlet
 * 
 */
public class RenderDocActionsWs extends DeclarativeWebScript {

	private static final Log LOGGER = LogFactory.getLog(RenderDocActionsWs.class);
	
	private EvaluatorRegistry evaluatorRegistry;
	
	/**
	 * Implement webscript.
	 * @param req request
	 * @param status status
	 * @param cache cache
	 * @return model
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,	Status status, Cache cache) {

		Map<String, Object> model = new HashMap<String, Object>();
		List<ImagingAction> displayedActions = new ArrayList<ImagingAction>();
		// VALIDATE INPUT
		String nodeRef = req.getParameter("nodeRef");
		String documentView = req.getParameter("view");
		
		if (nodeRef != null && documentView != null) {
			NodeRef evalNode = new NodeRef(nodeRef);
			
			if (evalNode != null) {
			    
			    List<ImagingAction> imagingActions = ImagingActionsParser.getImagingActions();
				
				for (ImagingAction imagingAction : imagingActions) {
					
					//ACTION EVALUATOR
					boolean displayAction = true;
					for (ImagingActionEvaluator imagingEvaluator : imagingAction.getEvaluators()) {
						displayAction = displayAction && evaluateAction(imagingEvaluator, evalNode);
					}
					//ACTION DISPLAY VIEWS
                    boolean viewMatch = false;
                    for (String view : imagingAction.getViews()) {
                        if (view.equals(documentView)) {
                            viewMatch = true;
                        }
                    }
                    
					if (displayAction && viewMatch) {
						displayedActions.add(imagingAction);
					}
				}
			}
		}
		model.put("actions", displayedActions);
		return model;
	}

	/**
	 * Check if action is displayed or hidden.
	 * @param evaluator the evaluator
	 * @param evalNode the node reference
	 * @return evaluation result
	 */
	private boolean evaluateAction(ImagingActionEvaluator evaluator, NodeRef evalNode) {
		
		boolean display = true;
	
		ActionEvaluator actionEvaluator = evaluatorRegistry.getEvaluatorByBeanName(evaluator.getName());
		if (actionEvaluator != null) {
			if (evaluator.isNegate()) {
				display = !actionEvaluator.evaluate(evalNode);
			} else {
				display = actionEvaluator.evaluate(evalNode);
			}
		} else {
			LOGGER.error("EVALUATOR not found : " + evaluator.getName());
		}
		
		return display;
	}
	
	public void setEvaluatorRegistry(EvaluatorRegistry evaluatorRegistry) {
		this.evaluatorRegistry = evaluatorRegistry;
	}

}