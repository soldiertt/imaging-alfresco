package be.fsoffe.imaging.migrator.model.annotation;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * @author jbourlet
 *
 *[TEXT]
X = 815
Y = 808
WIDTH = 946
HEIGHT = 145
                       FONTTYPE = Arial
                       FONTHEIGHT = 65
SEMITRANSPARENT = 0
BORDER = 0
                       TEXT = jbourlet - April 28, 2014, 09:52:23, CEST
PAGE = 1
PAGEURL = http://vm-imgtest-repo/share/proxy/alfresco/api/node/content/workspace/SpacesStore/c8164a40-3034-40ea-b507-10e389b0b78b/331.PDF
                       COLOR = 255, 0, 0
TRANSPARENT = 1
LABEL = stamp1
PAGESIZE = 1921, 2724
						SECURITYMODEL = 2
						READ = 1
						MODIFY = 0
						EXECUTE = 0
						PRINT = 1
						DELETE = 0
						MODIFYSECURITY = 0
						OWNER = admin
CREATEDATE = 28 AVR. 2014, 09:52:27, CEST
MODIFIEDDATE = 28 avr. 2014, 09:52:32, CEST
CREATEDID = jbourlet
MODIFIEDID = jbourlet

 */
public class ImagingAnnotation {
	
	private static final Log LOGGER = LogFactory.getLog(ImagingAnnotation.class);
	
	private String type;
	
	@AnnotationLabel(value = "X")
	private long posX;
	@AnnotationLabel(value = "Y")
	private long posY;
	@AnnotationLabel(value = "WIDTH")
	private long width;
	@AnnotationLabel(value = "HEIGHT")
	private long height;
	@AnnotationLabel(value = "PAGE")
	private long page;
	@AnnotationLabel(value = "RESOURCE", types = { "STAMP" })
	private String resource;
	@AnnotationLabel(value = "BORDER", types = { "TEXT" })
	private long border;
	@AnnotationLabel(value = "FILLCOLOR", types = { "TEXT", "HIGHLIGHT" })
	private String backgroundColor;
	@AnnotationLabel(value = "SEMITRANSPARENT", types = { "TEXT" })
	private long semiTransparent;
	@AnnotationLabel(value = "TRANSPARENT", types = { "TEXT", "HIGHLIGHT" })
	private long transparent;
	@AnnotationLabel(value = "LABEL")
	private String label;
	@AnnotationLabel(value = "CREATEDATE")
	private String createdDate;
	@AnnotationLabel(value = "MODIFIEDDATE")
	private String modifiedDate;
	@AnnotationLabel(value = "CREATEDID")
	private String creator;
	@AnnotationLabel(value = "MODIFIEDID")
	private String modifier;
	
	private ImagingAnnotationText annotationText;
	private ImagingAnnotationSecurity annotationSecurity;
	
	/**
	 * Default constructor.
	 * @param owner the annotation author/owner.
	 */
	public ImagingAnnotation(String owner) {
		this.annotationText = new ImagingAnnotationText();
		this.annotationSecurity = new ImagingAnnotationSecurity(owner);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getPosX() {
		return posX;
	}

	public void setPosX(long posX) {
		this.posX = posX;
	}

	public long getPosY() {
		return posY;
	}

	public void setPosY(long posY) {
		this.posY = posY;
	}

	public long getWidth() {
		return width;
	}

	public void setWidth(long width) {
		this.width = width;
	}

	public long getHeight() {
		return height;
	}

	public void setHeight(long height) {
		this.height = height;
	}

	public long getPage() {
		return page;
	}

	public void setPage(long page) {
		this.page = page;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public long getBorder() {
		return border;
	}

	public void setBorder(long border) {
		this.border = border;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public long getTransparent() {
		return transparent;
	}

	public void setTransparent(long transparent) {
		this.transparent = transparent;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getSemiTransparent() {
		return semiTransparent;
	}

	public void setSemiTransparent(long semiTransparent) {
		this.semiTransparent = semiTransparent;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public ImagingAnnotationText getAnnotationText() {
		return annotationText;
	}

	public ImagingAnnotationSecurity getAnnotationSecurity() {
		return annotationSecurity;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("[" + this.type + "]\n");
		try {
			output.append(objectToAnnotationLabels(this, this.type));
			output.append(objectToAnnotationLabels(this.getAnnotationText(), this.type));
			output.append(objectToAnnotationLabels(this.getAnnotationSecurity(), this.type));
		} catch (IllegalAccessException e) {
			LOGGER.error("Unable to read field value !", e);
		}
		
		return output.toString();
	}
	
	/**
	 * Return a key/value text serializing all object fields marked with AnnotationLabel annotation.
	 * @param object the object to serialize
	 * @param annotType type of annotation
	 * @return text
	 * @throws IllegalAccessException if error occurs
	 */
	private String objectToAnnotationLabels(Object object, String annotType) throws IllegalAccessException   {
		StringBuilder output = new StringBuilder("");
		Object returnValue;
		for (Field f: object.getClass().getDeclaredFields()) {
		   AnnotationLabel labelAnnot = f.getAnnotation(AnnotationLabel.class);
		   if (labelAnnot != null) {
			   f.setAccessible(true);
			   returnValue = (Object) f.get(object);
			   f.setAccessible(false);
			   if (returnValue != null && Arrays.asList(labelAnnot.types()).contains(annotType)) {
				   output.append(labelAnnot.value()).append(" = ").append(returnValue).append("\n");
			   }
		   }
		}
		return output.toString();
	}
}
