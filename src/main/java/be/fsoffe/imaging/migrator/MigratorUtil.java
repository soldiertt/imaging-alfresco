package be.fsoffe.imaging.migrator;

import java.util.Arrays;
import java.util.Calendar;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fsoffe.imaging.model.ImagingModel;
import be.fsoffe.imaging.webscript.ImagingScript;

/**
 * Utility class for migration stuff.
 * 
 * @author jbourlet
 *
 */
public final class MigratorUtil {

	private static final Log LOGGER = LogFactory.getLog(MigratorUtil.class);
	
	/**
	 * DOC was migrated successfully.
	 */
	public static final int STATUS_MIGRATED = 11;
	/**
	 * DOC was migrated successfully and updated by updater job.
	 */
	public static final int STATUS_MIGRATED_AND_UPDATED = 110;
	/**
	 * DOC was migrated successfully and imports complete via the job.
	 */
	public static final int STATUS_MIGRATED_AND_COMPLETED_IMPORTS = 1; // OTHER COLUMN
	/**
	 * DOC was not found during update document job.
	 */
	public static final int STATUS_DOC_NOT_FOUND_IN_IMAGING = 120;
	/**
	 * DOC is not linked in Client/server.
	 */
	public static final int STATUS_NOT_LINKED = 12;
	/**
	 * Cannot delete old version of document in alfresco.
	 */
	public static final int STATUS_CANNOT_DELETE_PREVIOUS_DOCUMENT = 13;
	/**
	 * Input properties file not found.
	 */
	public static final int STATUS_CANNOT_FIND_INPUT_PROPERTIES_FILE = 14;
	/**
	 * Input properties file cannot be parsed as json.
	 */
	public static final int STATUS_CANNOT_PARSE_INPUT_PROPERTIES_FILE = 15;
	/**
	 * Wrong or missing main PDF file.
	 */
	public static final int WRONG_OR_MISSING_MAIN_PDF_FILE = 16;
	
	/**
	 * JUST SKIP.
	 */
	public static final int SKIP_EXCEPTION = 99;
	
	/**
	 * Name of the file that will be processed by the workflow (entry folder).
	 */
	public static final String JSON_OUTPUT_FILENAME = "workflow.json";
	/**
	 * QName for the annotation relationship with its parent document.
	 */
	public static final QName ASSOC_ANNOT_CONTAINS = QName.createQName("http://www.alfresco.org/model/annotation", "contains");
	/**
	 * Workflow entry point folder name.
	 */
	public static final String WORKFLOW_ENTRY_POINT_FOLDER = "Entry";
	
	private static final String DOCTYPE_BRIEF_LETTRE_GAAJ = "BRIEF/LETTRE GAAJ";
	private static final String DOCTYPE_F1_CONTR = "F1 CONTR";
	private static final String DOCTYPE_F1_BT_CE = "F1 BT CE";
	
	/**
	 * Document types that need to be content indexed.
	 */
	public static final String[] DOCTYPES_TO_INDEX_CONTENT = {DOCTYPE_BRIEF_LETTRE_GAAJ, DOCTYPE_F1_CONTR, DOCTYPE_F1_BT_CE};
	

	/**
	 * Utility class recommandation.
	 */
	private MigratorUtil() {
		// private constructor
	}

	/**
	 * Create the repository path for the document.
	 * 
	 * @param fileFolderService utility service
	 * @param companyHome root of alfresco
	 * @param docInDate the date that will determine the path parts
	 * @return the nodeRef of the newly created (or existing) destination folder
	 * @throws ImagingMigratorException if error occurs
	 */
	public static NodeRef createDestinationPath(FileFolderService fileFolderService, NodeRef companyHome,
			Calendar docInDate) throws ImagingMigratorException {
		String[] foldersToFinal = ImagingScript.getConstant("finalPath").split("/");
		FileInfo repoNode;
		try {
			repoNode = fileFolderService.resolveNamePath(companyHome, Arrays.asList(foldersToFinal), true);
		} catch (FileNotFoundException e) {
			LOGGER.error("Unable to find final folder !", e);
			throw new ImagingMigratorException();
		}
		String year = String.valueOf(docInDate.get(Calendar.YEAR));
		String month = String.valueOf(docInDate.get(Calendar.MONTH) + 1);
		String day = String.valueOf(docInDate.get(Calendar.DAY_OF_MONTH));
		String[] repoPath = { year, month, day };
		NodeRef tempPathNode;
		NodeRef currentPathNode = repoNode.getNodeRef();
		for (int i = 0; i < repoPath.length; i++) {
			tempPathNode = fileFolderService.searchSimple(currentPathNode,
					repoPath[i]);
			if (tempPathNode == null) {
				tempPathNode = fileFolderService.create(currentPathNode,
						repoPath[i], ImagingModel.TYPE_FDS_FOLDER).getNodeRef();
			}
			currentPathNode = tempPathNode;
		}
		return currentPathNode;
	}

	/**
	 * Return the mimetype based on the filename.
	 * 
	 * @param mimetypeService utility service
	 * @param filename the name of the file
	 * @return the mimetype
	 */
	public static String getMimeTypeForFileName(
			MimetypeService mimetypeService, String filename) {
		// fall back to binary mimetype if no match found
		String mimetype = MimetypeMap.MIMETYPE_BINARY;
		int extIndex = filename.lastIndexOf('.');
		if (extIndex != -1) {
			String ext = filename.substring(extIndex + 1).toLowerCase();
			String mt = mimetypeService.getMimetypesByExtension().get(ext);
			if (mt != null) {
				mimetype = mt;
			}
		}

		return mimetype;
	}

	/**
	 * Check if a string can be converted to a numeric value.
	 * 
	 * @param str the input string
	 * @return true or false
	 */
	public static boolean isNumeric(String str) {
		try {
			Long.parseLong(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
