package be.fsoffe.imaging.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import be.fsoffe.imaging.model.ImagingBox;

/**
 * Java class responsible for the webscript "List boxes" List the available
 * boxes for a user, taking the permissions into account.
 * Return a Json string with a list of boxes.
 * 
 * @author jlbourlet
 * 
 */
public class ListBoxesWs extends DeclarativeWebScript {
	
	private ServiceRegistry registry;
	private Repository repository;
	private static final String COLLABORATOR_PERMISSION = "Collaborator";

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setServiceRegistry(ServiceRegistry registrySrv) {
		this.registry = registrySrv;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status) {
		Map<String, Object> model = new HashMap<String, Object>();
		List<ImagingBox> imagingBoxes = new ArrayList<ImagingBox>();
		
		NodeRef boxesRootNode = getBoxesNodeRef();
		List<FileInfo> boxes = registry.getFileFolderService().listFolders(boxesRootNode);
		for (FileInfo box : boxes) {
			// NEW FILTER : only where we are collaborator
			if (AccessStatus.ALLOWED == registry.getPermissionService().hasPermission(box.getNodeRef(), COLLABORATOR_PERMISSION)) {
				ImagingBox imagingBox = new ImagingBox();
				NodeRef boxRef = box.getNodeRef();
				imagingBox.setNodeRef(boxRef.toString());
				imagingBox.setName((String) registry.getNodeService().getProperty(boxRef, ContentModel.PROP_NAME));
				imagingBox.setDocCount(registry.getFileFolderService().listFolders(boxRef).size());
				imagingBoxes.add(imagingBox);
			}
		}
		model.put("boxes", imagingBoxes);
		return model;
	}

	/**
	 * Get root folder of functional boxes reference.
	 * @return folder reference
	 */
	protected NodeRef getBoxesNodeRef() {
		NodeRef companyHomeRef = repository.getCompanyHome();
		String boxesPath = ImagingScript.getConstant("boxesPath");
		String[] pathElements = boxesPath.split("/");
		NodeRef boxesNodeRef = companyHomeRef;
		for (int i = 0; i < pathElements.length; i++) {
			boxesNodeRef = registry.getFileFolderService().searchSimple(boxesNodeRef, pathElements[i]);
		}
		if (boxesNodeRef != null) {
			return boxesNodeRef;
		} else {
			throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
					"Unable to locate path");
		}
	}
}