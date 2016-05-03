package be.fsoffe.imaging.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptSession;

import be.fsoffe.imaging.model.ImagingModel;

/**
 * WebScript for the Search feature.
 * 
 * @author jlbourlet
 * 
 */
public class SearchWs extends DeclarativeWebScript {
	
	private static Log logger = LogFactory.getLog(SearchWs.class);  
	
	private ServiceRegistry registry;

	private static final String SEARCH_DOCS_SESSION_VAR = "searchDocs";
	
	public void setServiceRegistry(ServiceRegistry registrySrv) {
		this.registry = registrySrv;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status) {
		Map<String, Object> model = new HashMap<String, Object>();
		List<TemplateNode> docNodes = new ArrayList<TemplateNode>();
		WebScriptSession session = req.getRuntime().getSession();
		 
		String search = req.getParameter("search");
		String docname = req.getParameter("docname");
		String doctype = req.getParameter("doctype");
		String docsource = req.getParameter("docsource");
		String doclinked = req.getParameter("doclinked");
		String doccreationdateStart = req.getParameter("doccreationdate-start");
		String doccreationdateEnd = req.getParameter("doccreationdate-end");
		String fulltext1 = req.getParameter("fulltext1");
		String fulltext2 = req.getParameter("fulltext2");
		String operator1 = req.getParameter("operator1");
		String dossierNr = req.getParameter("dossiernr");

		int maxItems = getMaxItems();
		
		//check if load results from session
		if (!("true").equals(search)) {
		    @SuppressWarnings("unchecked")
            List<TemplateNode> searchNodes = (List<TemplateNode>) session.getValue(SEARCH_DOCS_SESSION_VAR);
		    if (searchNodes != null) {
		        //Avoid templateNode caching
		        for (TemplateNode templateNode : searchNodes) {
		            docNodes.add(new TemplateNode(templateNode.getNodeRef(), registry, null));
                }
		    }
		    model.put("documents", docNodes);
		    model.put("limited", false);
	        return model;
		}
		
		//Set search locale
		I18NUtil.setLocale(new Locale("en-US"));
		
		String topQuery = buildTopQuery(docname, doctype, docsource, doclinked, doccreationdateStart, doccreationdateEnd, dossierNr); 

		if (topQuery != null) { // CRITERIA FOR FDS:DOCUMENT
		
			List<NodeRef> nodeRefs = executeQuery(topQuery, maxItems);
			
			if (nodeRefs.size() > 0) {
				String childQuery = buildChildQuery(fulltext1, fulltext2, operator1, nodeRefs);
				
				if (childQuery != null) { // CRITERIA FOR FDS:CONTENT
					
					nodeRefs = executeQuery(childQuery, maxItems);
					
					// CONVERT ALL REMAINING DOCUMENTS TO THEIR FDS:DOCUMENT EQUIVALENT
					for (NodeRef nodeRef : nodeRefs) {
						ChildAssociationRef primaryParent = registry.getNodeService().getPrimaryParent(nodeRef);
						docNodes.add(new TemplateNode(primaryParent.getParentRef(), registry, null));
					}
					
				} else {
					// JUST SEND THE FDS:DOCUMENT BACK
					for (NodeRef nodeRef : nodeRefs) {
						docNodes.add(new TemplateNode(nodeRef, registry, null));
					}
				}
			}
			
		} else {
			
			String childQuery = buildChildQuery(fulltext1, fulltext2, operator1, null);
			
			if (childQuery != null) { // CRITERIA FOR FDS:CONTENT
				
				List<NodeRef> nodeRefs = executeQuery(childQuery, maxItems);
				logger.debug("childQuery noderefs size : " + nodeRefs.size());
				// CONVERT ALL REMAINING DOCUMENTS TO THEIR FDS:DOCUMENT EQUIVALENT
				for (NodeRef nodeRef : nodeRefs) {
					ChildAssociationRef primaryParent = registry.getNodeService().getPrimaryParent(nodeRef);
					NodeRef parentDocument = primaryParent.getParentRef();
					if (registry.getNodeService().getType(parentDocument).equals(ImagingModel.TYPE_FDS_DOCUMENT)) {
						docNodes.add(new TemplateNode(primaryParent.getParentRef(), registry, null));
					}
				}
			}
		}
		//put the results in session
		session.setValue(SEARCH_DOCS_SESSION_VAR, docNodes);
		
		model.put("documents", docNodes);
		
		if (docNodes.size() >= maxItems) {
			model.put("limited", true);
		} else {
			model.put("limited", false);
		}
		return model;
	}

	/**
	 * Make a query based on criteria.
	 * 
	 * @param docname docment name
	 * @param doctype document type
	 * @param doclinked is linked ?
	 * @param doccreationdateStart creation date range start
	 * @param doccreationdateEnd creation date range end
	 * @param dossierNr dossier number
	 * @return the lucene query as a string
	 */
	private String buildTopQuery(String docname, String doctype, String docsource, String doclinked, String doccreationdateStart, 
			String doccreationdateEnd, String dossierNr) {
		
		boolean atleastonetopfield = false;
		String query = "EXACTTYPE:\"fds:document\" AND NOT (PATH:\"" + ImagingScript.getConstant("failurePathWithPrefixes") + "/*\" OR PATH:\"" 
				+ ImagingScript.getConstant("dropZonePathWithPrefixes") + "/*\")";

		if (docname != null && !docname.trim().equals("")) {
			query += " AND @cm\\:name:\"" + docname + "\"";
			atleastonetopfield = true;
		}
		if (doctype != null && !doctype.equals("NONE")) {
			query += " AND @fds\\:docType:\"" + doctype + "\"";
			atleastonetopfield = true;
		}
		if (docsource != null && !docsource.equals("NONE")) {
			query += " AND @fds\\:docSource:\"" + docsource + "\"";
			atleastonetopfield = true;
		}
		if (doclinked != null && !doclinked.equals("")) {
			if (doclinked.equals("true")) {
				query += " AND @fds\\:docLinked:true";
			} else {
				query += " AND @fds\\:docLinked:false";
			}
			atleastonetopfield = true;
		}
		
		//MANAGE DATE RANGE FIELD
		boolean hasStartDate = doccreationdateStart != null && !doccreationdateStart.equals("");
		boolean hasEndDate = doccreationdateEnd != null && !doccreationdateEnd.equals("");
		if (hasStartDate) {
			atleastonetopfield = true;
			if (hasEndDate) {
				query += " AND @fds\\:docInDate:[" + formatDate(doccreationdateStart) + " TO " + formatDate(doccreationdateEnd) + "]";
			} else {
				query += " AND @fds\\:docInDate:[" + formatDate(doccreationdateStart) + " TO MAX]";
			}
		} else if (hasEndDate) {
			atleastonetopfield = true;
			query += " AND @fds\\:docInDate:[MIN TO " + formatDate(doccreationdateEnd) + "]";
		}
		//-------------------------
		
		if (dossierNr != null && !dossierNr.trim().equals("")) {
			query += " AND @fds\\:docDossierNr:\"" + dossierNr + "\"";
			atleastonetopfield = true;
		}
		
		if (atleastonetopfield) {
			return query;
		} else {
			return null;
		}
	}
	
	/**
	 * Make a query based on criteria related to child documents.
	 * @param fulltext1 full text field 1
	 * @param fulltext2 full text field 2
	 * @param operator1 AND/OR operator
	 * @param parentRefs already matching parent documents
	 * @return the lucene query as a string
	 */
	private String buildChildQuery(String fulltext1, String fulltext2, String operator1, List<NodeRef> parentRefs) {
		
		if (fulltext1 != null && !fulltext1.trim().equals("")) {
			String query = "(PATH:\"" + ImagingScript.getConstant("finalPathWithPrefixes") + "//*\" OR PATH:\"" 
					+ ImagingScript.getConstant("boxesPathWithPrefixes") + "//*\")" 
					+ " AND EXACTTYPE:\"fds:content\"";
			
			if (parentRefs != null) {
				query += " AND (";
				for (NodeRef parentRef : parentRefs) {
					query += "PRIMARYPARENT:" + parentRef.toString().replace(":", "\\:") + " OR ";
				}
				query = query.substring(0, query.length() - 4);
				query += ")";
			}
			query += " AND (@cm\\:content:\"" + fulltext1 + "\"";
			if (fulltext2 != null && !fulltext2.trim().equals("")) {
				query += " " + operator1.toUpperCase() + " @cm\\:content:\"" + fulltext2 + "\")";
			} else {
				query += ")";
			}
			return query;
		} else {
			return null;
		}
	}
	
	/**
	 * Execute the lucene query.
	 * @param query the lucene query as a string
	 * @param maxItems the maximum items to return
	 * @return the resulting node references
	 */
	private List<NodeRef> executeQuery(String query, int maxItems) {
		
		SearchParameters sp = new SearchParameters();
		ResultSet searchResults = null;
		List<NodeRef> results = new ArrayList<NodeRef>();
		
		try {
			sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
	        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
			sp.setQuery(query);
			sp.setMaxItems(maxItems);
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
	
	/**
	 * Format a date to the lucene appropriate format.
	 * @param stringDate input date
	 * @return the formatted date
	 */
	private String formatDate(String stringDate) {
		String[] dateParts = stringDate.split("-");
		String newDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
		return newDate.replaceAll("-", "\\\\-").concat("T00:00:00");
	}
}