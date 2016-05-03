package be.fsoffe.imaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;

/**
 * Utility class to sign and archive PDF document.
 * 
 * @author jbourlet
 *
 */
public final class SignatureUtil {

	private static Log logger = LogFactory.getLog(SignatureUtil.class);  
	
	/**
	 * Password of the certificate.
	 */
	public static final String CERTIFICATE_PASSWORD = "fsoffe12";
	
	/**
	 * Private constructor.
	 */
	private SignatureUtil() {
		// Private constructor
	}

	/**
	 * Digitally signed the PDF and send it to archive folder.
	 * @param pdfStream the pdf stream to sign and to archive
	 * @param finalDocName the document name
	 * @param pathToArchive path to the archive folder
	 * @param pathToCertificate path to the certificate that will sign the pdf
	 * @return true if ok, false if error occurs
	 */
	public static boolean signAndArchivePDF(InputStream pdfStream, String finalDocName, String pathToArchive, String pathToCertificate) {
		PdfReader reader;
		PdfSignatureAppearance signatureAppearance;
		PdfStamper stamper;
		FileOutputStream output;
		PrivateKey key;
		Certificate[] chain;
		KeyStore ks;
		
		File destinationTempFile = new File(pathToArchive + File.separator + finalDocName + ".pdf");
		
		try {
			ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(pathToCertificate), CERTIFICATE_PASSWORD.toCharArray());
		} catch (Exception e) {
			logger.error("Unable to load the certificate store !", e);
			return false;
		}

		try {
			String alias = (String) ks.aliases().nextElement();
			key = (PrivateKey) ks.getKey(alias, CERTIFICATE_PASSWORD.toCharArray());
			chain = ks.getCertificateChain(alias);
		} catch (Exception e) {
			logger.error("Problems loading key or chain !", e);
			return false;
		}

		try {
			reader = new PdfReader(pdfStream);
			output = new FileOutputStream(destinationTempFile);
		} catch (Exception e) {
			logger.error("Problems initialising PDF reader !", e);
			return false;
		}

		try {
			stamper = PdfStamper.createSignature(reader, output, '\0', new File("/tmp"));
			signatureAppearance = stamper.getSignatureAppearance();
		} catch (Exception e) {
			logger.error("Problems creating signed document !", e);
			return false;
		}

		try {
			signatureAppearance.setCrypto(key, chain, null,	PdfSignatureAppearance.WINCER_SIGNED);
		} catch (Exception e) {
			logger.error("Problems setting crypto !", e);
			return false;
		}

		try {
			signatureAppearance.setReason("Integrity signature");
			signatureAppearance.setLocation("FFE-FSO");
		} catch (Exception e) {
			logger.error("Problems setting settings !", e);
			return false;
		}

		try {
			stamper.close();
		} catch (Exception e) {
			logger.error("Problems closing stamper !", e);
			return false;
		}

		return true;
	}

}
