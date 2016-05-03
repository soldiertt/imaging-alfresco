package be.fsoffe.imaging.scheduled;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import be.fsoffe.imaging.audit.service.ImagingAuditService;

/**
 * Job that clean duplicate entries in audit table for :
 * 	- the same user
 * 	- the same document
 *  - the same DAY (YYY-MM-DD)
 *  WHERE from=destination=Repository (stamp 'initials' Audit event)
 * 
 * @author jbourlet
 *
 */
public class AuditCleanerJob implements Job {

	private static final Log LOGGER = LogFactory.getLog(AuditCleanerJob.class);
	
	@Override
	public void execute(JobExecutionContext jobexecutioncontect) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING AuditCleanerJob @@@");
		
		
		final TransactionService transactionService = (TransactionService) jobexecutioncontect.getJobDetail()
				.getJobDataMap().get("transactionService");
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final ImagingAuditService imagingAuditService = (ImagingAuditService) jobexecutioncontect.getJobDetail()
				.getJobDataMap().get("imagingAuditService");
		
		final RetryingTransactionCallback<Void> callbackInit = new RetryingTransactionCallback<Void>() {

			@Override
			public Void execute() throws Throwable {
				imagingAuditService.cleanAudit();
				return null;
			}
			
		};
		
		AuthenticationUtil.runAs(new RunAsWork<Void>() {
			@Override
			public Void doWork() {
				return txnHelper.doInTransaction(callbackInit);
			}
		}, AuthenticationUtil.SYSTEM_USER_NAME);
		
		LOGGER.info("@@@ ENDING AuditCleanerJob @@@");
	}

}
