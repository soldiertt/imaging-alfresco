package be.fsoffe.imaging.action.evaluator;

import org.springframework.beans.factory.BeanNameAware;

/**
 * Abstract class to automatically register an evaluator implementing this class.
 *  
 * @author jbourlet
 *
 */
public abstract class RegisteredActionEvaluator implements BeanNameAware, ActionEvaluator {
	
	private EvaluatorRegistry evaluatorRegistry;
	private String beanName;
	
	/**
	 * Import the evaluator registry.
	 * @param evaluatorRegistry the evaluator registry
	 */
	public void setEvaluatorRegistry(EvaluatorRegistry evaluatorRegistry) {
		this.evaluatorRegistry = evaluatorRegistry;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
		this.evaluatorRegistry.registerEvaluator(this);
	}

	/**
	 * @return Return the definition bean name.
	 */
	public String getBeanName() {
		return beanName;
	}
	
}
