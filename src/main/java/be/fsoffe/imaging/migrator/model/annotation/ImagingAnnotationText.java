package be.fsoffe.imaging.migrator.model.annotation;

/**
 * All annotation labels related to the text contained in an annotation.
 * 
 * @author jbourlet
 *
FONTTYPE = Arial
FONTHEIGHT = 65
TEXT = jbourlet - April 28, 2014, 09:52:23, CEST
COLOR = 255, 0, 0

 */
public class ImagingAnnotationText {
	
	@AnnotationLabel(value = "FONTTYPE", types = { "TEXT" })
	private String fontFamily;
	@AnnotationLabel(value = "FONTHEIGHT", types = { "TEXT" })
	private long fontHeight;
	@AnnotationLabel(value = "TEXT", types = { "TEXT" })
	private String text;
	@AnnotationLabel(value = "COLOR", types = { "TEXT" })
	private String color;
	
	public String getFontFamily() {
		return fontFamily;
	}
	
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}
	
	public long getFontHeight() {
		return fontHeight;
	}
	
	public void setFontHeight(long fontHeight) {
		this.fontHeight = fontHeight;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
}
