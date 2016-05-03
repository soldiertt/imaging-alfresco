package be.fsoffe.imaging.pb.webscript;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.model.ImagingModel;
import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.service.ClientServerService;

/**
 * WebScript : Check if an image can be linked base on its document type.
 * 
 * @author jlbourlet
 * 
 */
public class IsLinkableWs extends DeclarativeWebScript {
	
	
	private static Log logger = LogFactory.getLog(IsLinkableWs.class);   
	
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
		
		// VALIDATE INPUT
		String refScreen = req.getParameter("refscreen");
		String specialjpYesNo = req.getParameter("specialjp");
		boolean specialjp = "yes".equals(specialjpYesNo);
		String documentRef = req.getParameter("nodeRef");
		String refGajur = req.getParameter("refgajur");
		
		boolean isLinkable = false;
		
		if (documentRef != null && ((!specialjp && refScreen != null) || specialjp)) {
			try {
				NodeRef docNodeRef = new NodeRef(documentRef);
				if (docNodeRef != null) {
					if (specialjp && refGajur == null) {
						isLinkable = true;
					} else {
						String documentType = (String) registry.getNodeService().getProperty(docNodeRef, ImagingModel.PROP_FDS_DOC_TYPE);
						logger.debug("found doc type = " + documentType);
						isLinkable = csService.isLinkable(Long.parseLong(refScreen), documentType);
					}
				}
			} catch (ImagingPBException e) {
				logger.error("Unable to check if document is linkable from c/s database !");
				e.printStackTrace();
			}
		} else {
			logger.error("Invalid parameters given to isLinkable webscript !");
		}
		
		model.put("islinkable", isLinkable);
		
		return model;
	}

}