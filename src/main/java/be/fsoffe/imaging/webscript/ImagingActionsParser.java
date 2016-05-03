/*
 * Copyright (c) Smals
 */
package be.fsoffe.imaging.webscript;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import be.fsoffe.imaging.model.ImagingAction;
import be.fsoffe.imaging.model.ImagingActionEvaluator;


/**
 * Parse imaging-fdsdocument-actions.xml file to map with object model (list of ImagingAction).
 * 
 * @author jlb
 * 
 * @since 
 * 
 */
public final class ImagingActionsParser {

    private static List<ImagingAction> imagingActions;
    
    private static Log logger = LogFactory.getLog(ImagingActionsParser.class);  
    
    /**
     * Utility class good practice.
     */
    private ImagingActionsParser() {
		// private constructor.
	}

	/**
     * @return the imagingActions
     */
    public static synchronized List<ImagingAction> getImagingActions() {
        if (imagingActions == null) {
            try {
                parseActions();
            } catch (DataConversionException e) {
                logger.error("Cannot parse imaging actions xml !");
                return Collections.emptyList();
            }
        }
        return imagingActions;
    }
    
    /**
     * Find appropriate action in list.
     * 
     * @param actionName name of the action
     * @return null if action is not found.
     */
    public static ImagingAction getActionByName(String actionName) {
        for (ImagingAction imagingAction : getImagingActions()) {
            if (imagingAction.getName().equals(actionName)) {
                return imagingAction;
            }
        }
        return null;
    }
    
    /**
     * Parse the xml to convert into Imaging actions.
     * @throws DataConversionException if error occurs
     */
    @SuppressWarnings("unchecked")
    private static void parseActions() throws DataConversionException {
    	
    	logger.info("Parsing actions !");
    	
        imagingActions = new ArrayList<ImagingAction>();
        
        SAXBuilder sxb = new SAXBuilder();
        Document document = null;
        try {
            String url = "imaging-fdsdocument-actions.xml";
            URI uri = ImagingActionsParser.class.getClassLoader().getResource(url).toURI();
            File actionsFileDefinition = new File(uri);
            document = sxb.build(actionsFileDefinition);
        } catch (Exception e) {
            logger.error("Cannot parse imaging actions xml !");
        }

        Element racine = document.getRootElement();
        List<Element> actions = (List<Element>) racine.getChildren("action");
        Iterator<Element> actionsIterator = actions.iterator();
        while (actionsIterator.hasNext()) {
            Element currentAction = actionsIterator.next();
            
            //Constructor put default values
            ImagingAction imagingAction = new ImagingAction();
            //ACTION NAME
            imagingAction.setName(currentAction.getChild("name").getText());
            //ACTION TYPE
            Attribute attrType = currentAction.getAttribute("type");
            if (attrType != null) {
                imagingAction.setType(attrType.getValue());
                if (attrType.getValue().equals("url")) {
                	//ACTION URL TARGET
                	Attribute attrTarget = currentAction.getAttribute("target");
                	imagingAction.setUrlTarget(attrTarget.getValue());
                }
            }
            //ACTION URL
            Element urlElement = currentAction.getChild("url");
            if (urlElement != null) {
                imagingAction.setUrl(urlElement.getText());
            }
            //ACTION EVALUATOR
            Element evaluatorsElement = currentAction.getChild("evaluators");
            if (evaluatorsElement != null) {
	            List<Element> evaluators = (List<Element>) evaluatorsElement.getChildren("evaluator");
	            Iterator<Element> evalIterator = evaluators.iterator();
	            while (evalIterator.hasNext()) {
	                Element evaluator = evalIterator.next();
	                Attribute negateAttribute = evaluator.getAttribute("negate");
	                boolean negate = false;
	                if (negateAttribute != null && negateAttribute.getBooleanValue()) {
	                    negate = true;
	                }
	                imagingAction.addEvaluator(new ImagingActionEvaluator(evaluator.getText(), negate));
	            }
            }
            //ACTION DISPLAY VIEWS
            Element displayElement = currentAction.getChild("display");
            if (displayElement != null) {
	            List<Element> views = (List<Element>) displayElement.getChildren("view");
	            Iterator<Element> viewIterator = views.iterator();
	            while (viewIterator.hasNext()) {
	                Element view = viewIterator.next();
	                imagingAction.addView(view.getText());
	            }
            }
            imagingActions.add(imagingAction);
        }
    }
}
