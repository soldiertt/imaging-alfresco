package be.fsoffe.imaging.migrator.model;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Represents an Imaging Document.
 * 
 * @author jbourlet
 *
 */
public class ImagingDocument {
	
	private boolean hasAtu;
	
	private String parentPath;
	
	private String docName;
	
	private String docSource;
	
	private String docType;
	
	private String docClass;
	
	private Calendar docInDate;
	
	private InDateFormat docInDateFormat;
	
	private String docLetter;
	
	private boolean docLinked;

	private String legacyDocId;
	
	private boolean legacyWip;
	
	private String workflowDossierNr;
	
	private String workflowAssignee;
	
	private int workflowDossierStatus;
	
	private List<ImagingNote> notes;
	
	private boolean addDossierNrInNote;
	
	private NodeRef docNodeRef;
	
	private NodeRef mainPdfNodeRef;
	
	private File mainPdfFile;
	
	/**
	 * Default constructor.
	 * @param uewiName old uewi name
	 * @param docName new document name
	 * @param hasAtu flag
	 * @param parentPathToParse parent path
	 * @param wipCount wip status
	 */
	public ImagingDocument(String uewiName, String docName, boolean hasAtu, String parentPathToParse, int wipCount) {
		this.legacyDocId = uewiName;
		this.docName = docName;
		this.hasAtu = hasAtu;
		setParentPath(parentPathToParse);
		this.legacyWip = (wipCount > 0) && (uewiName.startsWith("14") || uewiName.startsWith("15")); //Must be WIP and 2014 or 2015
	}

	public boolean isHasAtu() {
		return hasAtu;
	}

	public void setHasAtu(boolean hasAtu) {
		this.hasAtu = hasAtu;
	}

	public String getParentPath() {
		return parentPath;
	}

	/**
	 * Set and convert a string path to a path array.
	 * @param parentPathToParse path as a string
	 */
	public void setParentPath(String parentPathToParse) {
		String[] pathParts = parentPathToParse.split("\\\\");
		this.parentPath = pathParts[2];
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getDocSource() {
		return docSource;
	}

	public void setDocSource(String docSource) {
		this.docSource = docSource;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getDocClass() {
		return docClass;
	}

	public void setDocClass(String docClass) {
		this.docClass = docClass;
	}

	public Calendar getDocInDate() {
		return docInDate;
	}

	public void setDocInDate(Calendar docInDate) {
		this.docInDate = docInDate;
	}

	public InDateFormat getDocInDateFormat() {
		return docInDateFormat;
	}

	public void setDocInDateFormat(InDateFormat docInDateFormat) {
		this.docInDateFormat = docInDateFormat;
	}

	public String getDocLetter() {
		return docLetter;
	}

	public void setDocLetter(String docLetter) {
		this.docLetter = docLetter;
	}

	public boolean isDocLinked() {
		return docLinked;
	}

	public void setDocLinked(boolean docLinked) {
		this.docLinked = docLinked;
	}

	public String getLegacyDocId() {
		return legacyDocId;
	}

	public void setLegacyDocId(String legacyDocId) {
		this.legacyDocId = legacyDocId;
	}

	public boolean isLegacyWip() {
		return legacyWip;
	}

	public void setLegacyWip(boolean legacyWip) {
		this.legacyWip = legacyWip;
	}

	public String getWorkflowDossierNr() {
		return workflowDossierNr;
	}

	public void setWorkflowDossierNr(String workflowDossierNr) {
		this.workflowDossierNr = workflowDossierNr;
	}

	public String getWorkflowAssignee() {
		return workflowAssignee;
	}

	public void setWorkflowAssignee(String workflowAssignee) {
		this.workflowAssignee = workflowAssignee;
	}

	public int getWorkflowDossierStatus() {
		return workflowDossierStatus;
	}

	public void setWorkflowDossierStatus(int workflowDossierStatus) {
		this.workflowDossierStatus = workflowDossierStatus;
	}

	public List<ImagingNote> getNotes() {
		return notes;
	}

	public void setNotes(List<ImagingNote> notes) {
		this.notes = notes;
	}

	public boolean isAddDossierNrInNote() {
		return addDossierNrInNote;
	}

	public void setAddDossierNrInNote(boolean addDossierNrInNote) {
		this.addDossierNrInNote = addDossierNrInNote;
	}

	public NodeRef getDocNodeRef() {
		return docNodeRef;
	}

	public void setDocNodeRef(NodeRef docNodeRef) {
		this.docNodeRef = docNodeRef;
	}

	public NodeRef getMainPdfNodeRef() {
		return mainPdfNodeRef;
	}

	public void setMainPdfNodeRef(NodeRef mainPdfNodeRef) {
		this.mainPdfNodeRef = mainPdfNodeRef;
	}

	public File getMainPdfFile() {
		return mainPdfFile;
	}

	public void setMainPdfFile(File mainPdfFile) {
		this.mainPdfFile = mainPdfFile;
	}
	
}
