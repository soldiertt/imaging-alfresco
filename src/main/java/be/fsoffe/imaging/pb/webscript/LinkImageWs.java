package be.fsoffe.imaging.pb.webscript;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.model.ImagingModel;
import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.model.LinkContext;
import be.fsoffe.imaging.pb.service.ClientServerService;

/**
 * WebScript : Link image in PB database.
 * 
 * @author jlbourlet
 * 
 */
public class LinkImageWs extends DeclarativeWebScript {
	
	private static Log logger = LogFactory.getLog(LinkImageWs.class);   
	
	private ClientServerService csService;
	
	private ServiceRegistry registry;

	public void setServiceRegistry(ServiceRegistry registrySrv) {
		this.registry = registrySrv;
	}
	
	public void setCsService(ClientServerService csService) {
		this.csService = csService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,	Status status) {
		Map<String, Object> model = new HashMap<String, Object>();
		
		// Process the JSON       

	    JSONParser parser = new JSONParser();

	    JSONObject json = null;

	    try {

	    	Reader reader = req.getContent().getReader();
	    	Object jsonO = null;
	    	if (reader.ready()) {
	    		jsonO = parser.parse(reader);
	    	}
	    	if (jsonO instanceof JSONObject && jsonO != null) {
	    		json = (JSONObject) jsonO;
	        } else {
	        	throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Wrong JSON type found " + jsonO);
            }
	    } catch (ParseException pe) {
	          throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON data received", pe);
        } catch (IOException ioe) {
	          throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Error while reading JSON data", ioe);
	    }
		
	    String documentRef = (String) json.get("nodeRef");
	    NodeRef docNodeRef = new NodeRef(documentRef);
	    String documentName = (String) registry.getNodeService().getProperty(docNodeRef, ContentModel.PROP_NAME);
	    String documentType = (String) registry.getNodeService().getProperty(docNodeRef, ImagingModel.PROP_FDS_DOC_TYPE);
	    boolean hasKeywordsAspect = registry.getNodeService().hasAspect(docNodeRef, ImagingModel.ASPECT_KEYWORDS);
	    JSONObject jsonContext = (JSONObject) json.get("context");
	    logger.debug("Context before calling linkImage: " + jsonContext.toJSONString());
	    LinkContext context = new LinkContext();
	    context.setSessionId((Long) jsonContext.get("sessionid"));
	    context.setUserName((String) jsonContext.get("username"));
	    context.setRefScreen((Long) jsonContext.get("refscreen"));
	    context.setRefDossier((Long) jsonContext.get("refdossier"));
	    context.setMap((String) jsonContext.get("map"));
	    context.setRefEmployer((Long) jsonContext.get("refemployer"));
	    context.setRefWorker((Long) jsonContext.get("refworker"));
	    context.setRefPerson((Long) jsonContext.get("refperson"));
	    context.setRefKeyword1((Long) jsonContext.get("refkeyword1"));
	    context.setRefKeyword2((Long) jsonContext.get("refkeyword2"));
	    context.setRefGajur((Long) jsonContext.get("refgajur"));
	   
	    //Update keywords if refGajur
	    if (context.getRefGajur() != null && context.getRefGajur() > 0 && hasKeywordsAspect) {
	    	@SuppressWarnings("unchecked")
			List<Integer> keywords = (List<Integer>) registry.getNodeService().getProperty(docNodeRef, ImagingModel.PROP_FDS_KEYWORDS);
	    	if (keywords != null && keywords.size() > 0) {
	    		context.setRefKeyword1(keywords.get(0).longValue());
	    		if (keywords.size() > 1) {
	    			context.setRefKeyword2(keywords.get(1).longValue());
	    		}
	    	}
	    }
	    
	    try {
			boolean ok = csService.linkImage(documentName, documentType, context);
			if (ok) {
				model.put("actionstatus", "OK");
			} else {
				model.put("actionstatus", "NOK");
				model.put("errormessage", "Document is already linked to this screen!");
			}
		} catch (ImagingPBException e) {
			model.put("actionstatus", "NOK");
			model.put("errormessage", "Unable to link image !");
			logger.error("Unable to link image !");
			e.printStackTrace();
		}
	    
		return model;
	}

}