package be.fsoffe.imaging.pb.dao;

/**
 * Static class for Client Server database constants.
 * 
 * @author jbourlet
 *
 */
public final class CSConstants {

	/**
	 * Static class recommandation.
	 */
	private CSConstants() {
		//Empty one
	}

	/**
	 * Default table for context navigation.
	 */
	public static final String TABLE_CONTEXT = "navi";
	
	/**
	 * NAVI session id.
	 */
	public static final String TBL_CTX_SESSIONID = "pbsession_id";
	
	/**
	 * NAVI username.
	 */
	public static final String TBL_CTX_USERNAME = "username";
	
	/**
	 * NAVI screen.
	 */
	public static final String TBL_CTX_SCREEN = "ref_scherm";
	
	
	/**
	 * NAVI ref_dossier.
	 */
	public static final String TBL_CTX_DOSSIER = "ref_dossier";
	
	/**
	 * NAVI map.
	 */
	public static final String TBL_CTX_MAP = "map";
	
	/**
	 * NAVI ref_werkgever.
	 */
	public static final String TBL_CTX_EMPLOYER = "ref_werkgever";
	
	/**
	 * NAVI ref_werknemer.
	 */
	public static final String TBL_CTX_WORKER = "ref_werknemer";
	
	/**
	 * NAVI ref_persoon.
	 */
	public static final String TBL_CTX_PERSON = "ref_persoon";
	
		
	/**
	 * NAVI keyword1.
	 */
	public static final String TBL_CTX_KEYW_ONE = "ref_keyword_1";
	/**
	 * NAVI keyword2.
	 */
	public static final String TBL_CTX_KEYW_TWO = "ref_keyword_2";
	/**
	 * NAVI vl_link.
	 */
	public static final String TBL_CTX_VL_LINK = "vl_link";
	
	/**
	 * NAVI logicalop (logical operator for keywords.)
	 */
	public static final String TBL_CTX_LOGICALOP = "logicalop";
	
	/**
	 * NAVI ref_doctype 
	 */
	public static final String TBL_CTX_DOCTYPE = "ref_doc_type";
	
	/**
	 * NAVI special JP 
	 */
	public static final String TBL_CTX_SPECIALJP = "specialjp";
	
	/**
	 * NAVI ref_gajur 
	 */
	public static final String TBL_CTX_GAJUR = "ref_gajur";
	
	/**
	 * Default table for images.
	 */
	public static final String TABLE_IMAGES = "image";
	/**
	 * IMAGE name.
	 */
	public static final String TBL_IMG_IMGNAME = "nr_image";
	/**
	 * IMAGE dossier.
	 */
	public static final String TBL_IMG_DOSSIER = "ref_dossier";
	/**
	 * IMAGE map.
	 */
	public static final String TBL_IMG_MAP = "map";
	/**
	 * IMAGE doctype.
	 */
	public static final String TBL_IMG_DOCTYPE = "ref_doc_type";
	/**
	 * IMAGE employer.
	 */
	public static final String TBL_IMG_EMPLOYER = "ref_werkgever";
	/**
	 * IMAGE worker.
	 */
	public static final String TBL_IMG_WORKER = "ref_werknemer";
	/**
	 * IMAGE person.
	 */
	public static final String TBL_IMG_PERSON = "ref_persoon";
	/**
	 * IMAGE keyword1.
	 */
	public static final String TBL_IMG_KEYW_ONE = "ref_keyword_1";
	/**
	 * IMAGE keyword2.
	 */
	public static final String TBL_IMG_KEYW_TWO = "ref_keyword_2";
	/**
	 * IMAGE vl_link.
	 */
	public static final String TBL_IMG_VL_LINK = "vl_link";
	
	/**
	 * IMAGE ref_jur.
	 */
	public static final String TBL_IMG_GAJUR = "ref_gajur";
	
	/**
	 * Employer table.
	 */
	public static final String TABLE_EMPLOYER = "wege";
	
	/**
	 * Employer ID column.
	 */
	public static final String TBL_WEGE_ID = "id_werkgever";
	
	/**
	 * Employer name column.
	 */
	public static final String TBL_WEGE_NAME = "naam_werkgever";
	
	/**
	 * Worker table.
	 */
	public static final String TABLE_WORKER = "wene";
	
	/**
	 * Worker ID column.
	 */
	public static final String TBL_WENE_ID = "id_werknemer";
	
	/**
	 * Worker name column.
	 */
	public static final String TBL_WENE_NAME = "naam_werknemer";
	
	/**
	 * Person table.
	 */
	public static final String TABLE_PERSON = "pers";
	
	/**
	 * Person ID column.
	 */
	public static final String TBL_PERS_ID = "id_persoon";
	
	/**
	 * Person name column.
	 */
	public static final String TBL_PERS_NAME = "naam_persoon";
	
	/**
	 * Keyword table.
	 */
	public static final String TABLE_KEYWORD = "keyw";
	
	/**
	 * Keyword ID column.
	 */
	public static final String TBL_KEYW_ID = "id_keyword";
	
	/**
	 * Keyword label fr column.
	 */
	public static final String TBL_KEYW_LABEL_FR = "keyword_bes_fr";
	
	/**
	 * Keyword label nl column.
	 */
	public static final String TBL_KEYW_LABEL_NL = "keyword_bes_nl";
		
	/*************************
	 * SQL QUERY'S.
	 ************************/
	
	/**
	 * VALID LINKABLE CONTEXT (Except specialJP)
	 */
	public static final String SQL_VALID_CONTEXT_FOR_USER_1 =  "SELECT * FROM " + TABLE_CONTEXT + " WHERE " 
			+ TBL_CTX_USERNAME + " = ? AND "
			+ "(("
			+ TBL_CTX_SCREEN + " IS NOT NULL AND " + TBL_IMG_VL_LINK + " = 1 "
			+ ") OR ("
			+ TBL_CTX_SPECIALJP + " = 1"
			+ "))";
	
	/**
	 * DOC TYPES BY SCREEN ID.
	 */
	public static final String SQL_DOCTYPES_BY_SCREEN_1 =  "SELECT docu.bes_document FROM docu, koppel"
			+ " WHERE docu.id_doc_type=koppel.ref_doc_type AND koppel.ref_scherm = ?";
	/**
	 * INSERT NEW IMAGE LINK.
	 */
	public static final String SQL_INSERT_IMAGE_LINK = "INSERT INTO " + TABLE_IMAGES + "(" 
			+ TBL_IMG_IMGNAME + ","
			+ TBL_IMG_DOSSIER + ","
			+ TBL_IMG_MAP + ","
			+ TBL_IMG_DOCTYPE + ","
			+ TBL_IMG_EMPLOYER + ","
			+ TBL_IMG_WORKER + ","
			+ TBL_IMG_PERSON + ","
			+ TBL_IMG_KEYW_ONE + ","
			+ TBL_IMG_KEYW_TWO + ","
			+ TBL_IMG_GAJUR + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
	
	/**
	 * UPDATE GROUPANSWER for incoming dropZone2 documents.
	 */
	public static final String SQL_UPDATE_GROUPANSWER = "UPDATE groupanswer set wf_document_id = ? WHERE id_groupanswer = ?";
	
	/**
	 * UPDATE Wrgl for incoming dropZone2 documents.
	 */
	public static final String SQL_UPDATE_VRGL = "UPDATE vrgl set wf_document_id = ? WHERE id_vrgl = ?";
	
	/**
	 * CONTEXT BY SESSION.
	 */
	
	public static final String SQL_CONTEXT_BY_SESSION_ID_1_AND_USER_2 =  "SELECT " + TBL_CTX_SESSIONID 
			+ ", " + TBL_CTX_USERNAME + ", " + TBL_CTX_SCREEN  + ", " + TBL_CTX_DOSSIER   + ", " + TBL_CTX_MAP
			+ ", " + TBL_CTX_EMPLOYER + ", " + TBL_CTX_WORKER  + ", " + TBL_CTX_PERSON    + ", " + TBL_CTX_KEYW_ONE 
			+ ", " + TBL_CTX_KEYW_TWO + ", " + TBL_CTX_VL_LINK + ", " + TBL_CTX_LOGICALOP + ", " + TBL_CTX_DOCTYPE
			+ ", " + TBL_CTX_SPECIALJP + ", " + TBL_CTX_GAJUR 
 			+ " FROM " + TABLE_CONTEXT + " WHERE " + TBL_CTX_SESSIONID + " = ? AND " + TBL_CTX_USERNAME + " = ?";
	
	
	/**
	 * Employer name by id.
	 */
	public static final String SQL_EMPLOYER_NAME_BY_ID_1 =  "SELECT " + TBL_WEGE_NAME + " FROM " + TABLE_EMPLOYER + " WHERE " + TBL_WEGE_ID + " = ?";
	
	/**
	 * Worker name by id.
	 */
	public static final String SQL_WORKER_NAME_BY_ID_1 =  "SELECT " + TBL_WENE_NAME + " FROM " + TABLE_WORKER + " WHERE " + TBL_WENE_ID + " = ?";
	
	/**
	 * Person name by id.
	 */
	public static final String SQL_PERSON_NAME_BY_ID_1 =  "SELECT " + TBL_PERS_NAME + " FROM " + TABLE_PERSON + " WHERE " + TBL_PERS_ID + " = ?";
	
	/**
	 * All keywords.
	 */
	public static final String SQL_ALL_KEYWORDS =  "SELECT " + TBL_KEYW_ID + ", trim(" + TBL_KEYW_LABEL_NL + ") as keywnl, trim(" + TBL_KEYW_LABEL_FR + ") as keywfr FROM " 
			+ TABLE_KEYWORD;
	
	/**
	 * Keyword label by id.
	 */
	public static final String SQL_KEYWORD_NAME_BY_ID_1 =  "SELECT trim(" + TBL_KEYW_LABEL_NL + ") || ' / ' || trim(" + TBL_KEYW_LABEL_FR + ") FROM " 
			+ TABLE_KEYWORD + " WHERE " + TBL_KEYW_ID + " = ?";
	
	/**
	 * DOCTYPES BY NAME.
	 */
	public static final String SQL_ID_DOCTYPE_BY_NAME_1 =  "SELECT id_doc_type FROM docu WHERE TRIM(bes_document) = ?";
	/**
	 * IMAGES COUNT BY NAME.
	 */
	public static final String SQL_COUNT_IMAGES_BY_IMAGENAME_1 = "SELECT count(*) total FROM " + TABLE_IMAGES + " WHERE " + TBL_IMG_IMGNAME + " = ?";
	/**
	 * SELECT IMAGES PREFIX SQL.
	 */
	public static final String SQL_IMGNAME_FROM_IMAGE = "SELECT " + TBL_IMG_IMGNAME + " FROM " +  TABLE_IMAGES;
}
