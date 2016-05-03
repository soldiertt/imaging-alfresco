package be.fsoffe.imaging.pb.model;

/**
 * Link helper object containing all the info needed to establish link betwee Imaging image and Client Server.
 * 
 * @author jbourlet
 *
 */
public class LinkContext {

	private Long sessionId;
	private Long refScreen;
	private String userName;
	private Long refDossier;
	private String map;
	private Long refEmployer;
	private String employerName;
	private Long refWorker;
	private String workerName;
	private Long refPerson;
	private String personName;
	private Long refKeyword1;
	private String keyword1Name;
	private Long refKeyword2;
	private String keyword2Name;
	private Long logicalOp;
	private Long refDocType;
	private Boolean specialjp;
	private Long refGajur;
		

	public Long getRefGajur() {
		return refGajur;
	}

	public void setRefGajur(Long refGajur) {
		this.refGajur = refGajur;
	}

	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public Long getRefScreen() {
		return refScreen;
	}

	public void setRefScreen(Long refScreen) {
		this.refScreen = refScreen;
	}

	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public Long getRefDossier() {
		return refDossier;
	}
	
	public void setRefDossier(Long refDossier) {
		this.refDossier = refDossier;
	}
	
	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}

	public Long getRefEmployer() {
		return refEmployer;
	}
	
	public void setRefEmployer(Long refEmployer) {
		this.refEmployer = refEmployer;
	}
	
	public String getEmployerName() {
		return employerName;
	}

	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}

	public Long getRefWorker() {
		return refWorker;
	}
	
	public void setRefWorker(Long refWorker) {
		this.refWorker = refWorker;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public Long getRefPerson() {
		return refPerson;
	}

	public void setRefPerson(Long refPerson) {
		this.refPerson = refPerson;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public Long getRefKeyword1() {
		return refKeyword1;
	}

	public void setRefKeyword1(Long refKeyword1) {
		this.refKeyword1 = refKeyword1;
	}

	public String getKeyword1Name() {
		return keyword1Name;
	}

	public void setKeyword1Name(String keyword1Name) {
		this.keyword1Name = keyword1Name;
	}

	public Long getRefKeyword2() {
		return refKeyword2;
	}

	public void setRefKeyword2(Long refKeyword2) {
		this.refKeyword2 = refKeyword2;
	}

	public String getKeyword2Name() {
		return keyword2Name;
	}

	public void setKeyword2Name(String keyword2Name) {
		this.keyword2Name = keyword2Name;
	}

	public Long getLogicalOp() {
		return logicalOp;
	}
	
	public void setLogicalOp(Long logicalOp) {
		this.logicalOp = logicalOp;
	}
	
	public Long getRefDocType() {
		return refDocType;
	}

	public void setRefDocType(Long refDocType) {
		this.refDocType = refDocType;
	}

	public Boolean getSpecialjp() {
		return specialjp;
	}

	public void setSpecialjp(Boolean specialjp) {
		this.specialjp = specialjp;
	}
	
}
