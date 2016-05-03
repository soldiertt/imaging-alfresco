package be.fsoffe.imaging.migrator.model.annotation;

/**
 * 
 * @author jbourlet
 *
SECURITYMODEL = 2
READ = 1
MODIFY = 0
EXECUTE = 0
PRINT = 1
DELETE = 0
MODIFYSECURITY = 0
OWNER = admin

 */
public class ImagingAnnotationSecurity {
	
	@AnnotationLabel(value = "SECURITYMODEL")
	private int securityModel;
	@AnnotationLabel(value = "READ")
	private int read;
	@AnnotationLabel(value = "MODIFY")
	private int modify;
	@AnnotationLabel(value = "EXECUTE")
	private int execute;
	@AnnotationLabel(value = "PRINT")
	private int print;
	@AnnotationLabel(value = "DELETE")
	private int delete;
	@AnnotationLabel(value = "MODIFYSECURITY")
	private int modifysecurity;
	@AnnotationLabel(value = "OWNER")
	private String owner;
	
	/**
	 * Default constructor with default values for security.
	 * @param owner the owner of the annotation
	 */
	public ImagingAnnotationSecurity(String owner) {
		this.owner = owner;
		this.securityModel = 2;
		this.read = 1;
		this.modify = 0;
		this.execute = 0;
		this.print = 1;
		this.delete = 0;
		this.modifysecurity = 0;
	}

	public int getSecurityModel() {
		return securityModel;
	}
	
	public void setSecurityModel(int securityModel) {
		this.securityModel = securityModel;
	}
	
	public int getRead() {
		return read;
	}
	
	public void setRead(int read) {
		this.read = read;
	}
	
	public int getModify() {
		return modify;
	}
	
	public void setModify(int modify) {
		this.modify = modify;
	}
	
	public int getExecute() {
		return execute;
	}
	
	public void setExecute(int execute) {
		this.execute = execute;
	}
	
	public int getPrint() {
		return print;
	}
	
	public void setPrint(int print) {
		this.print = print;
	}
	
	public int getDelete() {
		return delete;
	}
	
	public void setDelete(int delete) {
		this.delete = delete;
	}
	
	public int getModifysecurity() {
		return modifysecurity;
	}
	
	public void setModifysecurity(int modifysecurity) {
		this.modifysecurity = modifysecurity;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
}
