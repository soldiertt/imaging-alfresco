package be.fsoffe.imaging.scheduled;

import org.alfresco.util.CronTriggerBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Cron trigger manager to allow JMX bean allowed operations.
 * 
 * @author jbourlet
 *
 */
public class CronTriggerBeanManager implements CronTriggerBeanManagerInterface {
	private static Log logger = LogFactory.getLog(CronTriggerBeanManager.class);

	private CronTriggerBean triggerBean;

	@Override
	public boolean isEnabled() {
		return triggerBean.isEnabled();
	}

	@Override
	public void enable() throws Exception {
		triggerBean.setEnabled(true);
		triggerBean.afterPropertiesSet();
		logger.info("Job " + triggerBean.getBeanName() + " enabled");
	}

	@Override
	public void disable() throws Exception {
		triggerBean.setEnabled(false);
		triggerBean.destroy();
		logger.info("Job " + triggerBean.getBeanName() + " disabled");
	}

	@Override
	public String getCronExpression() {
		return triggerBean.getCronExpression();
	}

	@Override
	public void setCronExpression(String cronExpression) {
		triggerBean.setCronExpression(cronExpression);
		logger.info("Job " + triggerBean.getBeanName()
				+ " scheduled with expression " + cronExpression);
		try {
			triggerBean.afterPropertiesSet();
		} catch (Exception e) {
			logger.error("Setting cron expression " + cronExpression
					+ " for job " + triggerBean.getBeanName()
					+ " throws error : " + e.getMessage());
			logger.error(e.getStackTrace());
		}
	}

	public void setTriggerBean(CronTriggerBean triggerBean) {
		this.triggerBean = triggerBean;
	}

}
