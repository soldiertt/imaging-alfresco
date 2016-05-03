package be.fsoffe.imaging.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.model.Keyword;
import be.fsoffe.imaging.pb.service.ClientServerService;

/**
 * Java class responsible for the webscript "keywordsDataList".
 * List the available keywords based on Client/Server database.
 * Return a Json string with a list of keyword objects.
 * 
 * @author jlbourlet
 * 
 */
public class KeywordsDataListWs extends DeclarativeWebScript {
	
	private static Log logger = LogFactory.getLog(KeywordsDataListWs.class); 
	
	private ClientServerService csService;
	
	public void setCsService(ClientServerService csService) {
		this.csService = csService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status) {
		Map<String, Object> model = new HashMap<String, Object>();
		List<Keyword> keywords = new ArrayList<Keyword>();
		String localeParam = req.getParameter("locale");
		
		LocaleEditor localeEditor = new LocaleEditor();
		Locale locale = new Locale("nl"); // Default
		
		if (localeParam != null) {
			localeEditor.setAsText(localeParam);
			locale = (Locale) localeEditor.getValue();
		}
		
		if (locale.getLanguage().equals("fr")) {
			model.put("sortKey", "nameFr");
		} else {
			// DEFAULT NL
			model.put("sortKey", "nameNl");
		}
		
		try {
			keywords = csService.findKeywords();
		} catch (ImagingPBException e) {
			logger.error("Unable to read keywords from c/s database !");
			logger.error(e);
		}
		
		model.put("keywordsDataList", keywords);
		return model;
	}

}