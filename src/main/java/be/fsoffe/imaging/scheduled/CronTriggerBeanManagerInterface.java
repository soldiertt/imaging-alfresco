package be.fsoffe.imaging.scheduled;

/**
 * Interface to manage the scheduled cron jobs via JMX Console.
 * 
 * @author jbourlet
 *
 */
public interface CronTriggerBeanManagerInterface {

	/**
	 * Etat actuel du job.
	 * @return booléen vrai si le job est démarré
	 */
	boolean isEnabled();

	/**
	 * Démarre le job.
	 * @throws Exception if error occurs
	 */
	void enable() throws Exception;

	/**
	 * Arrête le job.
	 * @throws Exception if error occurs
	 */
	void disable() throws Exception;

	/**
	 * Récupération de l'expression cron actuellement définie.
	 * @return chaîne de caractère au format cron
	 */
	String getCronExpression();

	/**
	 * Définition de l'expression cron à utiliser.
	 * @param cronExpression expression cron
	 */
	void setCronExpression(String cronExpression);
}
