package be.fsoffe.imaging.pb.model;

/**
 * Keyword object, from table "keyw".
 * 
 * @author jbourlet
 *
 */
public class Keyword {
	
	private Long id;
	
	private String nameNl;
	
	private String nameFr;
	
	public Keyword(Long id, String nameNl, String nameFr) {
		super();
		this.id = id;
		this.nameNl = nameNl;
		this.nameFr = nameFr;
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNameNl() {
		return nameNl;
	}

	public void setNameNl(String nameNl) {
		this.nameNl = nameNl;
	}
	
	public String getNameFr() {
		return nameFr;
	}

	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}
	
}
