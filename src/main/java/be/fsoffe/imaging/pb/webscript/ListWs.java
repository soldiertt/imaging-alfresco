package be.fsoffe.imaging.pb.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.model.ImagingModel;
import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.model.LinkContext;
import be.fsoffe.imaging.pb.service.ClientServerService;

/**
 * WebScript : List images given the context from C/S database.
 * 
 * @author jlbourlet
 * 
 */
public class ListWs extends DeclarativeWebScript {
	
	
	private static Log logger = LogFactory.getLog(ListWs.class);   
	
	private ServiceRegistry registry;

	private ClientServerService csService;
	
	public void setServiceRegistry(ServiceRegistry registrySrv) {
		this.registry = registrySrv;
	}

	public void setCsService(ClientServerService csService) {
		this.csService = csService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,	Status status) {
		Map<String, Object> model = new HashMap<String, Object>();
		List<TemplateNode> docNodes = new ArrayList<TemplateNode>();
		
		int maxItems = getMaxItems();
		boolean limited = false;
		
		String userName = registry.getAuthenticationService().getCurrentUserName();
		Long sessionId;
		try {
			sessionId = Long.parseLong(req.getParameter("sessionid"));
		} catch (NumberFormatException e1) {
			logger.warn("Cannot parse sessionid " + req.getParameter("sessionid"));
			model.put("documents", docNodes);
			return model;
		}
		
		try {
			LinkContext linkCtx = csService.getContextBySessionId(sessionId, userName);
			List<String> refImages = csService.findImagesByContext(linkCtx, maxItems);
			if (refImages.size() > 0) {
				
				if (refImages.size() == maxItems) {
					limited = true;
				}
				//String baseQuery = "EXACTTYPE:\"fds:document\" AND @fds\\:docLinked:true AND (";
				String baseQuery = "EXACTTYPE:\"fds:document\" AND (";
				for (String imageName : refImages) {
					baseQuery += "@cm\\:name:\"" + imageName + "\" OR ";
				}
				baseQuery = baseQuery.substring(0, baseQuery.length() - 4);
				baseQuery += ")";
				
				List<NodeRef> nodeRefs = executeQuery(baseQuery);
				
				// JUST SEND THE FDS:DOCUMENT BACK
				for (NodeRef nodeRef : nodeRefs) {
					logger.debug("DISPLAY PATH :" + (new TemplateNode(nodeRef, registry, null)).getDisplayPath());
					docNodes.add(new TemplateNode(nodeRef, registry, null));
				}
			}
			
		} catch (ImagingPBException e) {
			logger.info("Unable to read context from c/s database !");
			logger.info(e);
			model.put("error", "An error occured when reading context with sessionId " + sessionId);
		}
		
		model.put("documents", docNodes);
		model.put("limited", limited);
		
		return model;
	}

	/**
	 * Execute the lucene query.
	 * @param query the lucene query as a string
	 * @return the resulting node references
	 */
	private List<NodeRef> executeQuery(String query) {
		
		SearchParameters sp = new SearchParameters();
		ResultSet searchResults = null;
		List<NodeRef> results = new ArrayList<NodeRef>();
		
		try {
			sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
	        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
			sp.setQuery(query);
			sp.addSort("@{http://fsoffe.rva.fgov.be/model/fsoffeModel/1.0}docInDate", true);
			logger.debug("query : " + query);
			searchResults = registry.getSearchService().query(sp);
			results = searchResults.getNodeRefs();
			logger.debug("size: " + results.size());
			
		} finally {
		    if (searchResults != null) {
		    	searchResults.close();
		    }
		}
		
		return results;
		
	}
	
	/**
	 * Get the application settings for the maximum results to return for a single search.
	 * @return the number of items
	 */
	private int getMaxItems() {
		String configQuery = "select e.* from fds:imagingParametersList as e where e.fds:idParam='searchMaxResults'";
		int value = 0;
		 
		SearchParameters sp = new SearchParameters();
		ResultSet configResult = null;
		List<NodeRef> results = new ArrayList<NodeRef>();
		
		try {
			sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		    sp.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
			sp.setQuery(configQuery);
			configResult = registry.getSearchService().query(sp);
			results = configResult.getNodeRefs();
			
		} finally {
		    if (configResult != null) {
		    	configResult.close();
		    }
		}
		
		if (results.size() == 1) {
			String valueStr = (String) registry.getNodeService().getProperty(results.get(0), ImagingModel.PROP_FDS_DL_VALUE_PARAM);
			value = Integer.valueOf(valueStr);
		}
		return value;
	}
}