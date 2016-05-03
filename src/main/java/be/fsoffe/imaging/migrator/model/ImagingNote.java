package be.fsoffe.imaging.migrator.model;
/**
 * Represents an imaging comment or note.
 * @author jbourlet
 *
 */
public class ImagingNote {
	
	private String text;
	private String author;
	
	/**
	 * Default constructor.
	 * @param text the comment content
	 * @param author the comment author (username)
	 */
	public ImagingNote(String text, String author) {
		this.text = text;
		this.author = author;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
}
