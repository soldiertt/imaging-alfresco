package be.fsoffe.imaging.action.evaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry class containing all declared evaluators.
 * 
 * @author jbourlet
 *
 */
public class EvaluatorRegistry {

	Map<String, ActionEvaluator> evaluators;
	
	/**
	 * Create an empty evaluator list.
	 */
	public void init() {
		this.evaluators = new HashMap<String, ActionEvaluator>();
	}
	
	/**
	 * Register a new evaluator.
	 * @param eval the evaluator to register
	 */
	public void registerEvaluator(final ActionEvaluator eval) {
		this.evaluators.put(eval.getBeanName(), eval);
	}
	
	/**
	 * Retrieve an evaluator based on its bean name.
	 * 
	 * @param beanName the bean name
	 * @return the evaluator
	 */
	public ActionEvaluator getEvaluatorByBeanName(final String beanName) {
		if (this.evaluators.containsKey(beanName)) {
			return this.evaluators.get(beanName);
		} else {
			return null;
		}
	}
}
