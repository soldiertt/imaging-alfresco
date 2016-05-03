package be.fsoffe.imaging.model;

import org.alfresco.service.namespace.QName;

/**
 * Store static Imaging model variables.
 * 
 * @author jbourlet
 *
 */
public final class ImagingModel {

	/***/
	private static final String DOCTYPE_BRIEF_LETTRE_GAAJ = "BRIEF/LETTRE GAAJ";
	/***/
	private static final String IMAGING_MODEL_1_0_URI = "http://fsoffe.rva.fgov.be/model/fsoffeModel/1.0";
	
	/***/
	public static final QName TYPE_FDS_DOCUMENT = QName.createQName(IMAGING_MODEL_1_0_URI, "document");
	/***/
	public static final QName TYPE_FDS_CONTENT = QName.createQName(IMAGING_MODEL_1_0_URI, "content");
	/***/
	public static final QName TYPE_FDS_FOLDER = QName.createQName(IMAGING_MODEL_1_0_URI, "folder");
	/***/
	public static final QName ASPECT_ARCHIVED = QName.createQName(IMAGING_MODEL_1_0_URI, "archived");
	/***/
	public static final QName ASPECT_WORKITEM = QName.createQName(IMAGING_MODEL_1_0_URI, "workitem");
	/***/
	public static final QName ASPECT_WORKFLOW = QName.createQName(IMAGING_MODEL_1_0_URI, "workflow");
	/***/
	public static final QName ASPECT_MYPERSONAL = QName.createQName(IMAGING_MODEL_1_0_URI, "mypersonal");
	/***/
	public static final QName ASPECT_LEGACY_DOC = QName.createQName(IMAGING_MODEL_1_0_URI, "legacyDoc");
	/***/
	public static final QName ASPECT_KEYWORDS = QName.createQName(IMAGING_MODEL_1_0_URI, "keywordsAspect");
	/***/
	public static final QName PROP_FDS_ARCHIVED_DATE = QName.createQName(IMAGING_MODEL_1_0_URI, "archivedDate");
	/***/
	public static final QName PROP_FDS_DOC_SOURCE = QName.createQName(IMAGING_MODEL_1_0_URI, "docSource");
	/***/
	public static final QName PROP_FDS_DOC_TYPE = QName.createQName(IMAGING_MODEL_1_0_URI, "docType");
	/***/
	public static final QName PROP_FDS_DOC_CLASS = QName.createQName(IMAGING_MODEL_1_0_URI, "docClass");
	/***/
	public static final QName PROP_FDS_DOC_LETTER = QName.createQName(IMAGING_MODEL_1_0_URI, "docLetter");
	/***/
	public static final QName PROP_FDS_DOC_LETTER_TYPE = QName.createQName(IMAGING_MODEL_1_0_URI, "docLetterType");
	/***/
	public static final QName PROP_FDS_DOC_INDATE = QName.createQName(IMAGING_MODEL_1_0_URI, "docInDate");
	/***/
	public static final QName PROP_FDS_DOC_LINKED = QName.createQName(IMAGING_MODEL_1_0_URI, "docLinked");
	/***/
	public static final QName PROP_FDS_DOC_DOSSIER_NR = QName.createQName(IMAGING_MODEL_1_0_URI, "docDossierNr");
	/***/
	public static final QName PROP_FDS_DOC_PROCESSED_BY = QName.createQName(IMAGING_MODEL_1_0_URI, "docProcessedBy");
	/***/
	public static final QName PROP_FDS_ITEM_OWNER = QName.createQName(IMAGING_MODEL_1_0_URI, "itemOwner");
	/***/
	public static final QName PROP_FDS_ITEM_ENTRYTIME = QName.createQName(IMAGING_MODEL_1_0_URI, "itemEntryTime");
	/***/
	public static final QName PROP_FDS_MYPERS_ASSIGNEE = QName.createQName(IMAGING_MODEL_1_0_URI, "mypersAssignee");
	/***/
	public static final QName PROP_FDS_MYPERS_EXPEDITOR = QName.createQName(IMAGING_MODEL_1_0_URI, "mypersExpeditor");
	/***/
	public static final QName PROP_FDS_MYPERS_TYPE = QName.createQName(IMAGING_MODEL_1_0_URI, "mypersType");
	/***/
	public static final QName PROP_FDS_MYPERS_ENTRYTIME = QName.createQName(IMAGING_MODEL_1_0_URI, "mypersEntrytime");
	/***/
	public static final QName PROP_FDS_LEGACY_DOCID = QName.createQName(IMAGING_MODEL_1_0_URI, "legacyDocId");
	/***/
	public static final QName PROP_FDS_LEGACY_WIP = QName.createQName(IMAGING_MODEL_1_0_URI, "legacyWip");
	/***/
	public static final QName PROP_FDS_DL_VALUE_PARAM = QName.createQName(IMAGING_MODEL_1_0_URI, "valueParam");
	/***/
	public static final QName PROP_FDS_KEYWORDS = QName.createQName(IMAGING_MODEL_1_0_URI, "keywords");
	
	/**
	 * Document types that we need to add the keywords aspect.
	 */
	public static final String[] DOCTYPES_WITH_KEYWORDS_ASPECT = {DOCTYPE_BRIEF_LETTRE_GAAJ};
	
	/**
	 * Utility class good practice.
	 */
	private ImagingModel() {
		//Private constructor
	}
	
	
}
