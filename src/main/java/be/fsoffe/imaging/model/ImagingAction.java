package be.fsoffe.imaging.model;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents an Imaging action.
 * 
 * @author jbourlet
 *
 */
public class ImagingAction {
	
	private String name;
	
	private String url;

	private String urlTarget;
	
	private String type;
	
	private List<ImagingActionEvaluator> evaluators;
	
	private List<String> views;
	
	/**
	 * Default constructor with default values.
	 */
	public ImagingAction() {
		this.url = "";
		this.urlTarget = "";
		this.type = "javascript";
		this.evaluators = new ArrayList<ImagingActionEvaluator>();
		this.views = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlTarget() {
		return urlTarget;
	}

	public void setUrlTarget(String urlTarget) {
		this.urlTarget = urlTarget;
	}

	/**
	 * Associate an evaluator to this action.
	 * @param eval an evaluator
	 */
	public void addEvaluator(ImagingActionEvaluator eval) {
	    this.evaluators.add(eval);
	}
	
	/**
	 * Associate a view to this action.
	 * @param view to associate
	 */
	public void addView(String view) {
        this.views.add(view);
    }

    /**
     * @return the evaluators
     */
    public List<ImagingActionEvaluator> getEvaluators() {
        return evaluators;
    }

    /**
     * @return the views
     */
    public List<String> getViews() {
        return views;
    }
	
}
