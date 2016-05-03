/*
 * Copyright (c) Smals
 */
package be.fsoffe.imaging.model;


/**
 * Action Evaluator.
 * 
 * @author jlb
 * 
 * @since 
 * 
 */
public class ImagingActionEvaluator {
    
    private String name;
    
    private boolean negate;


    /**
     * Default constructor.
     * @param name evaluator name
     * @param negate status
     */
    public ImagingActionEvaluator(String name, boolean negate) {
        this.name = name;
        this.negate = negate;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the negate
     */
    public boolean isNegate() {
        return negate;
    }

    /**
     * @param negate the negate to set
     */
    public void setNegate(boolean negate) {
        this.negate = negate;
    }
    
}
