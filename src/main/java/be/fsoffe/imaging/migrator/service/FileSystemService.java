package be.fsoffe.imaging.migrator.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import be.fsoffe.imaging.SignatureUtil;
import be.fsoffe.imaging.migrator.FFEImportsFileNameFilter;
import be.fsoffe.imaging.migrator.ImagingMigratorException;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.migrator.model.ImagingDocument;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
/**
 * Service to interact with file system input/output operations.
 * 
 * @author jbourlet
 *
 */
public class FileSystemService {
	
	private static final Log LOGGER = LogFactory.getLog(FileSystemService.class);
	
	private static final String EXTENSION_TIF_IGNORE = ".tif";
	
	private static final String EXTENSION_PDF = ".pdf";
	
	// WHERE trid program is located
	private String pathToTrid;
	// WHERE the certificate that will sign the pdf's is located
	private String pathToCertificate;
	// WHERE original ffe imports are stored
	private String pathToFFEImports;
	// WHERE original ATU imports are stored
	private String pathToATUImports;
	// WHERE merged ATU + PDF files are merged for archiving
	private String pathToATUFinal;
	// WHERE the main pdf document is stored
	private String pathToMainFinal;
	// WHERE we need to copy the files for TSM archiving
	private String pathToArchive;
	
	/**
	 * Locate the ffe imports to import into document.
	 * @param imgDocument the imaging document
	 * @return files list
	 */
	public List<File> findValidFFEImports(ImagingDocument imgDocument) {
		List<File> validImports = new ArrayList<File>();
		String uewiExportPathForDocument = pathToFFEImports + File.separator + imgDocument.getParentPath()
				+ File.separator + imgDocument.getLegacyDocId();
		LOGGER.info("import path :" + uewiExportPathForDocument);
		File docImportsRoot = new File(uewiExportPathForDocument);
		File[] ffeImports = docImportsRoot.listFiles(new FFEImportsFileNameFilter());
		if (ffeImports == null) {
			LOGGER.error("Cannot list folder");
		} else {
			for (int i = 0; i < ffeImports.length; i++) {
				if (!ffeImports[i].getName().equalsIgnoreCase(imgDocument.getLegacyDocId() + EXTENSION_TIF_IGNORE)) {
					validImports.add(ffeImports[i]);
				}
			}
		}
		return validImports;
	}
	
	/**
	 * Locate the ATU imports to import into document.
	 * @param imgDocument the imaging document
	 * @return files list
	 */
	public List<File> findValidATUImports(ImagingDocument imgDocument) {
		List<File> validImports = new ArrayList<File>();
		if (imgDocument.isHasAtu()) {
			String uewiExportPathForDocument = pathToATUImports + File.separator + imgDocument.getParentPath() 
					+ File.separator + imgDocument.getLegacyDocId();
			LOGGER.info("atu import path :" + uewiExportPathForDocument);
			File docImportsRoot = new File(uewiExportPathForDocument);
			File[] atuImports = docImportsRoot.listFiles(new FFEImportsFileNameFilter());
			if (atuImports == null) {
				LOGGER.error("Cannot list folder");
			} else {
				for (int i = 0; i < atuImports.length; i++) {
					validImports.add(atuImports[i]);
				}
			}
		}
		return validImports;
	}
	
	public List<File> tridFolderAndFindValidFFEImports(ImagingDocument imgDocument) {
		String uewiExportPathForDocument = pathToFFEImports + File.separator + imgDocument.getParentPath()
				+ File.separator + imgDocument.getLegacyDocId();
		tridFolder(uewiExportPathForDocument);
		return findValidFFEImports(imgDocument);
	}
	
	public List<File> tridFolderAndFindValidATUImports(ImagingDocument imgDocument) {
		String uewiExportPathForDocument = pathToATUImports + File.separator + imgDocument.getParentPath() 
				+ File.separator + imgDocument.getLegacyDocId();
		tridFolder(uewiExportPathForDocument);
		return findValidATUImports(imgDocument);
	}
	
	/**
	 * Make sure all files have an extension.
	 * 
	 * @param folderPath
	 */
	private void tridFolder(String folderPath) {
		try {
		    ProcessBuilder pb = new ProcessBuilder(pathToTrid, "-ce", folderPath + "/*");
		    Process p = pb.start();
		    p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Locate the main pdf file to attach to imaging document.
	 * @param imgDocument the imaging document
	 * @return the file as a stream
	 * @throws ImagingMigratorException 
	 */
	public ByteArrayInputStream getMainPdfAsStream(ImagingDocument imgDocument) throws ImagingMigratorException {
		String pdfFilePath = "";
		try {
			pdfFilePath = pathToMainFinal + File.separator + imgDocument.getLegacyDocId() + EXTENSION_PDF;
			return new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(pdfFilePath)));
		} catch (IOException e) {
			LOGGER.error("ERROR reading main pdf file : " + pdfFilePath, e);
			throw new ImagingMigratorException();
		}
	}
	
	/**
	 * Remove the pdf from the file system.
	 * 
	 * @param imgDocument the imaging document.
	 * @throws ImagingMigratorException if error occurs
	 */
	public void removeMainPDF(ImagingDocument imgDocument) throws ImagingMigratorException {
		String pdfFilePath = pathToMainFinal + File.separator + imgDocument.getLegacyDocId() + EXTENSION_PDF;
		File toDelete = new File(pdfFilePath);
		if (toDelete.exists() && !toDelete.delete()) {
			LOGGER.error("Cannot delete main pdf file : " + pdfFilePath);
			throw new ImagingMigratorException();
		}
		toDelete = null;
	}
	
	/**
	 * Locate the json properties file containing the document properties and annotations and notes.
	 * @param imgDocument the imaging document
	 * @return properties as a JSon object
	 * @throws ImagingMigratorException if error occurs
	 */
	public JSONObject getDocumentPropertiesAsJson(ImagingDocument imgDocument) throws ImagingMigratorException {
		JSONParser jsonParser = new JSONParser();
		try {	
			return (JSONObject) jsonParser.parse(new FileReader(pathToFFEImports + File.separator + imgDocument.getParentPath() + File.separator
					+ imgDocument.getLegacyDocId() + File.separator + "properties.json"));
		} catch (FileNotFoundException e) {
			//Manage this exception with a status in database
			throw new ImagingMigratorException(MigratorUtil.STATUS_CANNOT_FIND_INPUT_PROPERTIES_FILE);
		} catch (IOException e) {
			LOGGER.error("ERROR reading json properties file !", e);
			throw new ImagingMigratorException();
		} catch (ParseException e) {
			//Manage this exception with a status in database
			throw new ImagingMigratorException(MigratorUtil.STATUS_CANNOT_PARSE_INPUT_PROPERTIES_FILE);
		}
	}

	/**
	 * Digitally signed the PDF and send it to archive folder.
	 * @param imgDocument the imaging document
	 * @throws ImagingMigratorException 
	 */
	public void signAndArchivePDF(ImagingDocument imgDocument) throws ImagingMigratorException {
		PdfReader reader;
		PdfSignatureAppearance signatureAppearance;
		PdfStamper stamper;
		FileOutputStream output;
		PrivateKey key;
		Certificate[] chain;
		KeyStore ks;
		String filePath = "";
		
		// GET MAIN PDF TO ARCHIVE !!
		if (imgDocument.isHasAtu()) {
			filePath = pathToATUFinal + File.separator + imgDocument.getLegacyDocId() + EXTENSION_PDF;
		} else {
			filePath = pathToMainFinal + File.separator + imgDocument.getLegacyDocId() + EXTENSION_PDF;
		}
		
		File pdfSourceFile = new File(filePath);
		File destinationTempFile = new File(pathToArchive + File.separator + imgDocument.getDocName() + EXTENSION_PDF);
		
		try {
			ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(pathToCertificate), SignatureUtil.CERTIFICATE_PASSWORD.toCharArray());
		} catch (Exception e) {
			LOGGER.error("Unable to load the certificate store !", e);
			throw new ImagingMigratorException();
		}

		try {
			String alias = (String) ks.aliases().nextElement();
			key = (PrivateKey) ks.getKey(alias, SignatureUtil.CERTIFICATE_PASSWORD.toCharArray());
			chain = ks.getCertificateChain(alias);
		} catch (Exception e) {
			LOGGER.error("Problems loading key or chain !", e);
			throw new ImagingMigratorException();
		}

		try {
			reader = new PdfReader(pdfSourceFile.getPath());
			output = new FileOutputStream(destinationTempFile);
		} catch (Exception e) {
			LOGGER.error("Problems initialising PDF reader !", e);
			throw new ImagingMigratorException(MigratorUtil.WRONG_OR_MISSING_MAIN_PDF_FILE);
		}

		try {
			stamper = PdfStamper.createSignature(reader, output, '\0', new File("/tmp"));
			signatureAppearance = stamper.getSignatureAppearance();
		} catch (Exception e) {
			LOGGER.error("Problems creating signed document !", e);
			throw new ImagingMigratorException();
		}

		try {
			signatureAppearance.setCrypto(key, chain, null,	PdfSignatureAppearance.WINCER_SIGNED);
		} catch (Exception e) {
			LOGGER.error("Problems setting crypto !", e);
			throw new ImagingMigratorException();
		}

		try {
			signatureAppearance.setReason("Integrity signature");
			signatureAppearance.setLocation("FFE-FSO");
		} catch (Exception e) {
			LOGGER.error("Problems setting settings !", e);
			throw new ImagingMigratorException();
		}

		try {
			stamper.close();
		} catch (Exception e) {
			LOGGER.error("Problems closing stamper !", e);
			throw new ImagingMigratorException();
		}

	}
	
	
	public void setPathToTrid(String pathToTrid) {
		this.pathToTrid = pathToTrid;
	}

	public void setPathToCertificate(String pathToCertificate) {
		this.pathToCertificate = pathToCertificate;
	}

	public void setPathToFFEImports(String pathToFFEImports) {
		this.pathToFFEImports = pathToFFEImports;
	}

	public void setPathToATUImports(String pathToATUImports) {
		this.pathToATUImports = pathToATUImports;
	}

	public void setPathToATUFinal(String pathToATUFinal) {
		this.pathToATUFinal = pathToATUFinal;
	}

	public void setPathToMainFinal(String pathToMainFinal) {
		this.pathToMainFinal = pathToMainFinal;
	}

	public void setPathToArchive(String pathToArchive) {
		this.pathToArchive = pathToArchive;
	}
	
}
