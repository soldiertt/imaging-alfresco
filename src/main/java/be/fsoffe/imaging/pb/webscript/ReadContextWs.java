package be.fsoffe.imaging.pb.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.model.LinkContext;
import be.fsoffe.imaging.pb.service.ClientServerService;

/**
 * WebScript : Read the context from C/S database for link with an image.
 * 
 * @author jlbourlet
 * 
 */
public class ReadContextWs extends DeclarativeWebScript {
	
	
	private static Log logger = LogFactory.getLog(ReadContextWs.class);   
	
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
		List<LinkContext> linkCtxList;
		String userName = registry.getAuthenticationService().getCurrentUserName();
		String sessionId = req.getParameter("sessionid");
		
		
		try {
			
			if (sessionId != null) {
				linkCtxList = new ArrayList<LinkContext>();
				try {
					LinkContext context = csService.getContextBySessionId(Long.parseLong(sessionId), userName);
					linkCtxList.add(context);
				} catch (NumberFormatException e) {
					logger.warn("Cannot parse session id " + sessionId);
				}
			} else {
				logger.info("Read Context for user : " + userName);
				linkCtxList = csService.readContextForUser(userName);
				
			}
			
		} catch (ImagingPBException e) {
			linkCtxList = new ArrayList<LinkContext>();
			logger.error("Unable to read context from c/s database !");
			logger.error(e);
		}
		
		model.put("contextlist", linkCtxList);
		
		return model;
	}

}