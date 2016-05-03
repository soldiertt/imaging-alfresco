package be.fsoffe.imaging.audit.exception;


/**
 * Custom exception that handle an error during the migration process.
 * @author jbourlet
 *
 */
public class ImagingAuditException extends Exception {
	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = 2837191111397587056L;

	/**
	 * Parameterless Constructor.
	 */
    public ImagingAuditException() { }

    /**
     * Constructor that accepts a message.
     * @param message the exception message
     */
    public ImagingAuditException(String message) {
       super(message);
    }
}
