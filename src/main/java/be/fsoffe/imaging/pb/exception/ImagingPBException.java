package be.fsoffe.imaging.pb.exception;


/**
 * Custom exception that handle an error during the migration process.
 * @author jbourlet
 *
 */
public class ImagingPBException extends Exception {
	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = 2837191111397587056L;

	/**
	 * Parameterless Constructor.
	 */
    public ImagingPBException() { }

    /**
     * Constructor that accepts a message.
     * @param message the exception message
     */
    public ImagingPBException(String message) {
       super(message);
    }
}
