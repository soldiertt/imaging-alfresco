package be.fsoffe.imaging.migrator;

import org.quartz.JobExecutionException;

/**
 * Custom exception that handle an error during the migration process.
 * @author jbourlet
 *
 */
public class ImagingMigratorException extends JobExecutionException {
	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = 2837191111397587056L;

	/**
	 * Error code.
	 */
	private int errorCode;
	
	/**
	 * Parameterless Constructor.
	 */
    public ImagingMigratorException() { }

    /**
     * Constructor that accepts a message.
     * @param message the exception message
     */
    public ImagingMigratorException(String message) {
       super(message);
    }
    
    /**
     * Constructor that accepts a int (error code).
     * @param exceptionCode the exception code
     */
    public ImagingMigratorException(int exceptionCode) {
       this.errorCode = exceptionCode;
    }

	public int getErrorCode() {
		return errorCode;
	}
    
}
