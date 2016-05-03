package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Provide an interface to identify Action evaluators.
 * 
 * @author jbourlet
 *
 */
public interface ActionEvaluator {

	/**
	 * Main method to identify if the action is shown or hidden.
	 * 
	 * @param node the alfresco node reference of the regarding document the action will execute on.
	 * 
	 * @return boolean
	 */
	boolean evaluate(NodeRef node);
	
	/**
	 * Return the bean definitation name of the evaluator.
	 * @return bean name
	 */
	String getBeanName();
}
