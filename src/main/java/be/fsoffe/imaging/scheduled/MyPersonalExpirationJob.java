package be.fsoffe.imaging.scheduled;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * Job that check for Documents in a my personal workflow for too long and need to get back
 * to expeditor with an expiration type.
 * 
 * @author jbourlet
 *
 */
public class MyPersonalExpirationJob implements Job {

	private static final Log LOGGER = LogFactory.getLog(MyPersonalExpirationJob.class);
	
	@Override
	public void execute(JobExecutionContext jobexecutioncontect) throws JobExecutionException {
		LOGGER.info("@@@ RUNNING MyPersonalExpirationJob @@@");
		
		final String nbDaysExpiration = (String) jobexecutioncontect.getJobDetail().getJobDataMap().get("nbdaysexpiration");
		final NodeService nodeService = (NodeService) jobexecutioncontect.getJobDetail().getJobDataMap().get("nodeService");
		final SearchService searchService = (SearchService) jobexecutioncontect.getJobDetail().getJobDataMap().get("searchService");
		final TransactionService transactionService = (TransactionService) jobexecutioncontect.getJobDetail()
				.getJobDataMap().get("transactionService");
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final RetryingTransactionCallback<Void> callbackInit = new RetryingTransactionCallback<Void>() {

			@Override
			public Void execute() throws Throwable {
				Calendar xDaysAgo = Calendar.getInstance();
				xDaysAgo.add(Calendar.DAY_OF_MONTH, -(Integer.parseInt(nbDaysExpiration)));
				SimpleDateFormat luceneFormat = new SimpleDateFormat("yyyy\\-MM\\-dd'T'00:00:00"); //2003\-12\-16T00:00:00
				String expirationDate = luceneFormat.format(xDaysAgo.getTime());      
				
				SearchParameters sp = new SearchParameters();
				sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				sp.setQuery("EXACTTYPE:\"fds:document\" AND ASPECT:\"fds:mypersonal\" AND @fds\\:mypersType:\"REQUEST\""
						+ " AND @fds\\:mypersEntrytime:[MIN TO " + expirationDate + "]");
				sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
				LOGGER.info("query : " + sp.getQuery());
				ResultSet rs = searchService.query(sp);
				
				List<NodeRef> expiredMypersonalDocuments = rs.getNodeRefs();
				LOGGER.info("nb results : " + expiredMypersonalDocuments.size());
				for (NodeRef document : expiredMypersonalDocuments) {
					if (nodeService.hasAspect(document, ImagingModel.ASPECT_WORKITEM)) {
						nodeService.removeAspect(document, ImagingModel.ASPECT_WORKITEM);
					}
					String expeditor = (String) nodeService.getProperty(document, ImagingModel.PROP_FDS_MYPERS_EXPEDITOR);
					String assignee = (String) nodeService.getProperty(document, ImagingModel.PROP_FDS_MYPERS_ASSIGNEE);
					nodeService.setProperty(document, ImagingModel.PROP_FDS_MYPERS_ASSIGNEE, expeditor);
					nodeService.setProperty(document, ImagingModel.PROP_FDS_MYPERS_EXPEDITOR, assignee);
					nodeService.setProperty(document, ImagingModel.PROP_FDS_MYPERS_ENTRYTIME, new Date());
					nodeService.setProperty(document, ImagingModel.PROP_FDS_MYPERS_TYPE, "EXPIRED");
				}
				rs.close();
				
				return null;
			}
			
		};
		
		AuthenticationUtil.runAs(new RunAsWork<Void>() {
			@Override
			public Void doWork() {
				return txnHelper.doInTransaction(callbackInit);
			}
		}, AuthenticationUtil.SYSTEM_USER_NAME);
		
		LOGGER.info("@@@ ENDING MyPersonalExpirationJob @@@");
	}

}
