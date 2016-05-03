package be.fsoffe.imaging.model;
/**
 * Represents a functional box in imaging application.
 * 
 * @author jbourlet
 *
 */
public class ImagingBox {

	private String nodeRef;
	
	private String name;

	private int docCount;
	
	public String getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDocCount() {
		return docCount;
	}

	public void setDocCount(int docCount) {
		this.docCount = docCount;
	}

}
