package be.fsoffe.imaging.scheduled;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class TrashcanCleanerJob  implements Job {

	private static final Log LOGGER = LogFactory.getLog(TrashcanCleanerJob.class);
	
	@Override
	public void execute(JobExecutionContext jobexecutioncontext) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING TrashcanCleanerJob @@@");
		
		
		final TransactionService transactionService = (TransactionService) jobexecutioncontext.getJobDetail().getJobDataMap().get("transactionService");
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final NodeService nodeService = (NodeService) jobexecutioncontext.getJobDetail().getJobDataMap().get("nodeService");
		final String deleteBatchCount = (String) jobexecutioncontext.getJobDetail().getJobDataMap().get("batchCount");
		final String daysToKeep = (String) jobexecutioncontext.getJobDetail().getJobDataMap().get("daysToKeep");
		
		final RetryingTransactionCallback<Void> callbackInit = new RetryingTransactionCallback<Void>() {

			@Override
			public Void execute() throws Throwable {
				int deleteBatchCountInt = Integer.parseInt(deleteBatchCount);
				int daysToKeepInt = Integer.parseInt(daysToKeep);
				TrashcanCleaner cleaner = new TrashcanCleaner(nodeService, deleteBatchCountInt, daysToKeepInt);
				cleaner.clean();
				return null;
			}
			
		};
		
		AuthenticationUtil.runAs(new RunAsWork<Void>() {
			@Override
			public Void doWork() {
				return txnHelper.doInTransaction(callbackInit);
			}
		}, AuthenticationUtil.SYSTEM_USER_NAME);
		
		LOGGER.info("@@@ ENDING TrashcanCleanerJob @@@");
		
	}

}
