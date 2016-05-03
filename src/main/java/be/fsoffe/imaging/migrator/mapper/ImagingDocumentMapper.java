package be.fsoffe.imaging.migrator.mapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fsoffe.imaging.migrator.model.ImagingDocument;
import be.fsoffe.imaging.migrator.model.annotation.ImagingAnnotation;

/**
 * Mapper class to map old metadata to new values in Imaging application.
 * 
 * @author jbourlet
 *
 */
public final class ImagingDocumentMapper {

	private static final Log LOGGER = LogFactory.getLog(ImagingAnnotation.class);
	
	/**
	 * Utility class good practice.
	 */
	private ImagingDocumentMapper() {
		// private constructor
	}

	/**
	 * Map the document type.
	 * @param docType old document type
	 * @return new document type
	 */
	public static String mapDocType(String docType) {
		String mappedDocType = "";
		switch (docType) {
			case "BC901A" : 
			case "BC901B" :
			case "BC901D" :
				mappedDocType = "F1 CONTR";
				break;
			case "BC901C" :
				mappedDocType = "F1 BT CE";
				break;
			case "BRIEF/LETTRE BC901A" :
			case "BRIEF/LETTRE BC901B" :
				mappedDocType = "BRIEF/LETTRE CONTR";
				break;
			case "BRIEF/LETTRE BC901C" :
				mappedDocType = "BRIEF/LETTRE BT CE";
				break;
			case "PL/LP+SYND" :
				mappedDocType = "DIMONA/DMFA";
				break;
			case "" :
				mappedDocType = "NONE"; //Empty doctype => NONE
				break;
			default:
				mappedDocType = docType;
				break;
		}
		return mappedDocType;
	}
	
	/**
	 * Map the link status.
	 * @param docLinked old linked status
	 * @return new linked status
	 */
	public static boolean mapLinkStatus(String docLinked) {
		boolean isLinked;
		
		switch (docLinked) {
			case "L" :
			case "Y" :
				isLinked = true;
				break;
			default :
				isLinked = false;
		}
		
		return isLinked;
	}
	
	/**
	 * Map document class.
	 * @param docClass old class value
	 * @return new class value
	 */
	public static String mapDocClass(String docClass) {
		String mappedDocClass;
		
		switch (docClass) {
			case "RECENT" :
				mappedDocClass = "Archive";
				break;
			case "Dossier" : 
			case "MAIL" :
			case "DIRECTOR" :				
				mappedDocClass = "Dossier";
				break;
			default : 
				mappedDocClass = "";
		}
		return mappedDocClass;
	}
	
	/**
	 * Map the document inDate.
	 * @param docInDateStr the inDate as a string
	 * @return the new inDate
	 */
	public static Calendar mapDocInDate(String docInDateStr) {
		Calendar mappedInDate = null;
		Calendar cal = Calendar.getInstance();
		String[] dateFormats = {"yyyyMMddHHmmss", "yyyy-MM-dd"};
		if (!"".equals(docInDateStr)) {
			boolean formatOK = false;
			for (int i = 0; i < dateFormats.length; i++) {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormats[i]);
				try {
					cal.setTime(sdf.parse(docInDateStr));
					formatOK = true;
					break;
				} catch (java.text.ParseException e) {
					//Unable to parse this date with this format (try next format)
					formatOK = false;
				}
			}
			if (!formatOK) {
				LOGGER.error("Unable to parse scandate '" + docInDateStr + "', use actual date");
			}
		} else {
			LOGGER.warn("Empty scandate, use actual date");
		}
		mappedInDate = cal;
		return mappedInDate;
	}
	
	/**
	 * Map the dossier status.
	 * @param wfDossierStatus old dossier status
	 * @return new dossier status
	 */
	public static int mapDossierStatus(String wfDossierStatus) {
		if (!"".equals(wfDossierStatus)) {
			int mappedStatus = Integer.parseInt(wfDossierStatus);
			if (mappedStatus != 99) {
				return mappedStatus;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	/**
	 * Map to numeric value.
	 * @param string
	 * @return int
	 */
	public static int mapStringToValue(String strValue) {
		if (!"".equals(strValue)) {
			return Integer.parseInt(strValue);
		} else {
			return 0;
		}
	}
	
	/**
	 * Map the work in progress status.
	 * @param legacyWip wip status as a long
	 * @return new boolean wip status
	 */
	public static boolean mapLegacyWip(long legacyWip) {
		return (legacyWip == 0);
	}

	/**
	 * Map the document source.
	 * @param imgDoc the imaging document
	 * @return the new document source
	 */
	public static String mapDocSource(ImagingDocument imgDoc) {
		Set<String> uploadDocTypes = new HashSet<String>();
		uploadDocTypes.add("VL/QUESTIONNAIRE");
		uploadDocTypes.add("BRIEF/LETTRE BC901A");
		uploadDocTypes.add("BRIEF/LETTRE BC901C");
		
		if (imgDoc.getLegacyDocId().indexOf("RBE") != -1) {
			return "scanner";
		} else if (imgDoc.getLegacyDocId().indexOf("FSO") != -1) {
			if (uploadDocTypes.contains(imgDoc.getDocType()) && imgDoc.getWorkflowDossierStatus() == 99)  {
				return "upload";
			} else if (imgDoc.getDocClass().equals("MAIL")) {
				return "mail";
			}
			return "printer";
		}
		return "---"; //should not happen
	}
	
	/**
	 * Generate the new document name.
	 * @param imgDoc the imaging document
	 * @return the new document name
	 */
	public static String mapDocumentName(ImagingDocument imgDoc) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String docInDateStr = sdf.format(imgDoc.getDocInDate().getTime());
		String docSourceAbb = "NON";
		if (imgDoc.getDocSource() != null) {
			switch (imgDoc.getDocSource()) {
				case "upload": 
					docSourceAbb = "UPD";
					break;
				case "mail":
					docSourceAbb = "EML";
					break;
				case "scanner":
					docSourceAbb = "SCN";
					break;
				case "printer":
					docSourceAbb = "PRN";
					break;
				case "fax":
					docSourceAbb = "FAX";
					break;
				default :
					docSourceAbb = "NON";
			}
		}
		int millis = Calendar.getInstance().get(Calendar.MILLISECOND);
		return docInDateStr + docSourceAbb + String.format("%03d", millis);
	}
	
}
