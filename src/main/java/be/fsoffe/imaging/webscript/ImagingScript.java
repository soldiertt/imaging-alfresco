/*
 * CVS file status:
 * 
 * $Id: SmalsScript.java,v 1.12 2011-09-12 09:41:33 alc Exp $
 * 
 * Copyright (c) Smals
 */
package be.fsoffe.imaging.webscript;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DescriptionImpl;

import be.fsoffe.imaging.SignatureUtil;
import be.fsoffe.imaging.action.evaluator.ActionEvaluator;
import be.fsoffe.imaging.action.evaluator.EvaluatorRegistry;
import be.fsoffe.imaging.audit.exception.ImagingAuditException;
import be.fsoffe.imaging.audit.model.EventBoxItem;
import be.fsoffe.imaging.audit.model.EventItem;
import be.fsoffe.imaging.audit.service.ImagingAuditService;
import be.fsoffe.imaging.migrator.MigratorUtil;
import be.fsoffe.imaging.model.ImagingAction;
import be.fsoffe.imaging.model.ImagingActionEvaluator;
import be.fsoffe.imaging.model.ImagingModel;
import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.service.ClientServerService;

/**
 * Extends the JavaScript API of Alfresco. Use this class to run our custom methods in api. 
 * 
 * @author Jean-Louis Bourlet
 * 
 * @since 1.0.0
 * 
 */
public class ImagingScript extends BaseScopableProcessorExtension {

    private static Log logger = LogFactory.getLog(ImagingScript.class);   
    private transient PermissionService permissionService;   
    private transient PersonService personService;
    private transient NodeService nodeService;
    private transient ContentService contentService;
    private transient ImagingAuditService imagingAuditService;
    private transient ClientServerService clientServerService;
    private EvaluatorRegistry evaluatorRegistry;
    private static Properties globalPropertiesStatic;
    private static String pathToArchive;
    private static String pathToCertificate;
    
    private static final String PARAM_NODEREF = "nodeRef";
    private static final String PARAM_ASSIGNEE = "assignee";
    private static final String IMAGING_DOCLIB_PATH = "Sites/Imaging/documentLibrary";
    private static final String IMAGING_ROOT_PATH_PREFIXES = "/app:company_home";
    private static final String IMAGING_DOCLIB_PATH_PREFIXES = "/app:company_home/st:sites/cm:imaging/cm:documentLibrary";
    private static final String BOXES_FOLDER_NAME = "Boxes";
    private static final String REPO_FOLDER_NAME = "Final";
    private static final String AUTOTRASH_FOLDER_NAME = "AutoTrash";
    private static final String FAILURE_FOLDER_NAME = "Failure";
    private static final String WORKFLOW_ENTRY_FOLDER_NAME = "Entry";
    private static final String DROPZONE_FOLDER_NAME = "DropZone";
    private static final String BOXES_PATH_PREFIXES = IMAGING_DOCLIB_PATH_PREFIXES + "/cm:" + BOXES_FOLDER_NAME;
    private static final String BOXES_PATH = IMAGING_DOCLIB_PATH + "/" + BOXES_FOLDER_NAME;
    private static final String REPO_PATH_PREFIXES = IMAGING_DOCLIB_PATH_PREFIXES + "/cm:" + REPO_FOLDER_NAME;
    private static final String REPO_PATH = IMAGING_DOCLIB_PATH + "/" + REPO_FOLDER_NAME;
    private static final String FAILURE_PATH_PREFIXES = IMAGING_ROOT_PATH_PREFIXES + "/cm:" + FAILURE_FOLDER_NAME;
    private static final String DROPZONE_PATH_PREFIXES = IMAGING_ROOT_PATH_PREFIXES + "/cm:" + DROPZONE_FOLDER_NAME;
    private static final String[] ALLOWED_PROPERTIES = {"vti.server.external.host", "vti.server.external.port"};
    
    /**
     * Insert a new audit event in img_audit table.
     * 
     * @param uname the user name
     * @param docId the document noderef id
     * @param docType the document type
     * @param fromBox the source functional box
     * @param destination the destination of the document (Workflow, Repository or MyPersonal)
     */
    public void logAuditEvent(String uname, String docId, String docType, String fromBox, String destination) {
    	EventItem newEvent = new EventItem();
    	newEvent.setUserName(uname);
    	newEvent.setDocId(docId);
    	newEvent.setDocType(docType);
    	newEvent.setFromBox(fromBox);
    	newEvent.setDestination(destination);
    	try {
			imagingAuditService.saveEvent(newEvent);
		} catch (ImagingAuditException e) {
			logger.error("Unable to save new audit event", e);
		}
    }
    
    /**
     * Insert a new audit event in img_audit_box table.
     * 
     * @param docId the document noderef id
     * @param direction IN or OUT
     * @param boxName name of the box
     */
    public void logAuditBoxEvent(ScriptNode document, String direction) {
    	
    	try {
    		
	    	String docId = document.getId();
	    	String boxName = document.getParent().getName();
	    	
	    	if ("IN".equals(direction)) {
	    		String docSource = (String) document.getProperties().get(ImagingModel.PROP_FDS_DOC_SOURCE);
	    		EventBoxItem eventToCreate = new EventBoxItem();
	    		eventToCreate.setDocId(docId);
	    		eventToCreate.setDocSource(docSource);
	        	eventToCreate.setBoxName(boxName);
	        	eventToCreate.setInDate(Calendar.getInstance().getTime());
	        	imagingAuditService.saveBoxEvent(eventToCreate);
	        	
	    	} else if ("OUT".equals(direction)) {
	    		
	    		EventBoxItem eventToUpdate = new EventBoxItem();
	    		String updatedBy = (String) document.getProperties().get(ImagingModel.PROP_FDS_DOC_PROCESSED_BY);
	    		String docLetterType = (String) document.getProperties().get(ImagingModel.PROP_FDS_DOC_LETTER_TYPE);
	    		String docType = (String) document.getProperties().get(ImagingModel.PROP_FDS_DOC_TYPE);
	    		Long auditId = imagingAuditService.findInAuditBoxEvent(docId, boxName);
	    		if (auditId != null) {
		    		eventToUpdate.setAuditId(auditId);
		    		eventToUpdate.setOutDate(Calendar.getInstance().getTime());
		    		eventToUpdate.setDocLetterType(docLetterType);
		    		eventToUpdate.setDocType(docType);
		    		eventToUpdate.setUserName(updatedBy);
		    		imagingAuditService.updateBoxEvent(eventToUpdate);
	    		} else {
	    			logger.warn("Cannot find audit 'IN' event in database.");
	    		}
	    	}
    	
			
		} catch (ImagingAuditException e) {
			logger.error("Unable to save new audit box event", e);
		}
    }
    
	
    /**
	 * Return a list of arrays with count of activities by document type
	 * given the username and date range.
	 * 
	 * @param uname the user name
	 * @param dateRangeStart Date range start date
	 * @param dateRangeEnd Date range end date
	 * @param byLetterType if need extra column with letter type
	 * @return count by doc type arrays
	 */
    public List<String[]> activitiesByUserAndDate(String uname, String dateRangeStart, String dateRangeEnd, boolean byLetterType) {
    	try {
    		Date validStartDate = checkDateValidity(dateRangeStart);
    		Date validEndDate = checkDateValidity(dateRangeEnd);
    		
    		if (validStartDate == null || validEndDate == null) {
    			logger.error("Given date range format is not valid !");
    			return Collections.emptyList();
    		} else {
    			return imagingAuditService.findActivitiesByUserAndDate(uname, validStartDate, validEndDate, byLetterType);
    		}
		} catch (ImagingAuditException e) {
			logger.error("Unable to select audit events", e);
			return Collections.emptyList();
		}
    }
    
    /**
     * Check date validity with different formats.
     * 
     * @param dateStr the date as a string
     * @return the valid date or null if invalid
     */
    private Date checkDateValidity(String dateStr) {
    	String[] acceptedDateFormats = new String[] {"dd-MM-yyyy", "d-MM-yyyy", "dd-M-yyyy", "d-M-yyyy",
				"dd/MM/yyyy", "d/MM/yyyy", "dd/M/yyyy", "d/M/yyyy"};
		
		Date validDate = null;
		for (int i = 0; i < acceptedDateFormats.length; i++) {
			SimpleDateFormat sdf = new SimpleDateFormat(acceptedDateFormats[i]);
			sdf.setLenient(false);
			try {
    			validDate = sdf.parse(dateStr);
    			break;
    		} catch (ParseException e) {
    			validDate = null;
    		}	
		}
		return validDate;
    }
	/**
	 * Digitally signed the PDF and send it to archive folder.
	 * Called from DropZone2 Rule !
	 * @param pdfDocument the node to archive
	 * @param finalDocName the document name
	 * @return true if ok, false if error occurs
	 */
	public boolean signAndArchivePDF(ScriptNode pdfDocument, String finalDocName) {
		String nodeRefStr = "workspace://SpacesStore/" + pdfDocument.getId();
		NodeRef pdfNodeRef = new NodeRef(nodeRefStr);
		ContentReader cReader = contentService.getReader(pdfNodeRef, ContentModel.PROP_CONTENT);
		InputStream pdfStream = cReader.getContentInputStream();
		return SignatureUtil.signAndArchivePDF(pdfStream, finalDocName, pathToArchive, pathToCertificate);
	}
	
	/**
	 * Update GroupAnswer or Vrgl table depending the docType.
	 * Only for document with UPLOAD source coming from DropZone2.
	 * 
	 * @param docTypeIntStr
	 * @param docName
	 * @param docQuestionId
	 */
	public void updateGroupAnswerOrVrgl(String docTypeIntStr, String docName, String docQuestionId) {
		
		try {
			if (MigratorUtil.isNumeric(docTypeIntStr) && MigratorUtil.isNumeric(docQuestionId)) {
				clientServerService.updateGroupAnswerOrVrgl(Integer.parseInt(docTypeIntStr), docName, Long.parseLong(docQuestionId));
			} else {
				logger.error("Parameter docTypeIntStr or docQuestionId is not numeric !");
			}
		} catch (NumberFormatException | ImagingPBException e) {
			logger.error("Unable to update clientServer database with questionId !", e);
		}
		
	}
	
    /**
     * Return an alfresco global property value.
     * @param property name of property
     * @return value of property
     */
    public static String getAlfGlobal(String property) {
    	if (Arrays.asList(ALLOWED_PROPERTIES).contains(property)) { //Security reason
    		if (globalPropertiesStatic.get(property) != null) {
    			return (String) globalPropertiesStatic.get(property);
    		} else {
    			//Default values
    			if (property.equals("vti.server.external.port")) {
    				return "7070";
    			} else {
    				return "";
    			}
    		}
    	} else {
    		return "[NOT ALLOWED]";
    	}
    }
    
    /**
     * Constants used in webscripts.
     * 
     * @param constantName constant name
     * @return constant value
     */
    public static String getConstant(String constantName) {
    	if (constantName.equals("boxesPathWithPrefixes")) {
    		return BOXES_PATH_PREFIXES;
    	} else if (constantName.equals("boxesPath")) {
    		return BOXES_PATH;
    	} else if (constantName.equals("finalPathWithPrefixes")) {
        	return REPO_PATH_PREFIXES;
    	} else if (constantName.equals("failurePathWithPrefixes")) {
        	return FAILURE_PATH_PREFIXES;
    	} else if (constantName.equals("dropZonePathWithPrefixes")) {
        	return DROPZONE_PATH_PREFIXES;
    	} else if (constantName.equals("finalPath")) {
    		return REPO_PATH;
    	} else if (constantName.equals("imagingDocLibPath")) {
    		return IMAGING_DOCLIB_PATH;
    	} else if (constantName.equals("boxesFolderName")) {
    		return BOXES_FOLDER_NAME;
    	} else if (constantName.equals("repoFolderName")) {
    		return REPO_FOLDER_NAME;
    	} else if (constantName.equals("autoTrashFolderName")) {
    		return AUTOTRASH_FOLDER_NAME;
    	} else if (constantName.equals("failureFolderName")) {
    		return FAILURE_FOLDER_NAME;
    	} else if (constantName.equals("workflowEntryFolderName")) {
    		return WORKFLOW_ENTRY_FOLDER_NAME;
    	} else if (constantName.equals("dropzoneFolderName")) {
    		return DROPZONE_FOLDER_NAME;
    	} else {
    		return "";
    	}
    }
    /**
     * Validate a webscript.
     * 
     * @param script the script descriptor
     * @param json the webscript parameters
     * @return validation status
     */
    public boolean validate(DescriptionImpl script, JSONObject json) {
        String[] urlParts = script.getURIs()[0].split("/");
        String actionName = urlParts[urlParts.length - 1];
        
        boolean valid = false;
        
        try {
            ImagingAction imagingAction = ImagingActionsParser.getActionByName(actionName);
            boolean dataOk = dataCheck(imagingAction, json);
            if (dataOk) {
            	return securityCheck(imagingAction, json);
            }
        } catch (JSONException e) {
            logger.error("Cannot parse json request : " + e.getMessage());
        }
        
        return valid;
    }

    /**
     * Check the webscript parameters validity.
     * @param imagingAction the imaging action
     * @param json the webscript parameters
     * @return validation status
     * @throws JSONException if error occurs
     */
    private boolean dataCheck(ImagingAction imagingAction, JSONObject json) throws JSONException {
    	
    	boolean valid = false;
    	
    	String nodeRef = json.getString(PARAM_NODEREF);
    	
    	if (nodeRef != null && !nodeRef.equals("")) {
    		//DEFAULT CHECK IS A VALID NODE REF OBJECT
    		NodeRef evalNode = new NodeRef(nodeRef);
    		
    		if (evalNode != null &&	nodeService.exists(evalNode) 
    				&& nodeService.getType(evalNode).equals(ImagingModel.TYPE_FDS_DOCUMENT)) {
        		valid = true;
        	} else {
        		logger.warn("Wrong parameter : " + PARAM_NODEREF);
        	}
    		
    		//FOR SEND ACTION : CHECK THE ASSIGNEE PARAMETER
        	if (valid && imagingAction != null && imagingAction.getName().equals("send")) {
        		valid = false;
        		String assignee = json.getString(PARAM_ASSIGNEE);
        		if (assignee != null && !assignee.equals("") &&	personService.personExists(assignee)) {
        			valid = true;
        		} else {
        			logger.warn("Wrong parameter : " + PARAM_ASSIGNEE);
        		}
        	}
    	} else {
    		logger.warn("Wrong parameter : " + PARAM_NODEREF);
    	}
    	
    	return valid;
    }
    
    /**
     * Check the permissions of the user upon the given nodeRef.
     * 
     * @param imagingAction the imaging action
     * @param json the webscript parameters
     * @return check status
     * @throws JSONException if error occurs
     */
    private boolean securityCheck(ImagingAction imagingAction, JSONObject json) throws JSONException {
    	String nodeRef = json.getString(PARAM_NODEREF);
    	NodeRef evalNode = new NodeRef(nodeRef);
    	boolean allowed = false;
    	if (imagingAction == null) {
    		allowed = AccessStatus.ALLOWED.equals(permissionService.hasPermission(evalNode, PermissionService.READ));
    	} else {
    		allowed = AccessStatus.ALLOWED.equals(permissionService.hasPermission(evalNode, PermissionService.WRITE_PROPERTIES));
    	}
        if (imagingAction != null) {
	        for (ImagingActionEvaluator imagingEvaluator : imagingAction.getEvaluators()) {
	        	allowed = allowed && evaluateAction(imagingEvaluator, evalNode);
	        }
        }
        return allowed;
    }
    
    /**
     * Evaluate the action visibility.
     * @param evaluator the evaluator
     * @param evalNode the node reference
     * @return evaluation result, true = displayed, false = hidden
     */
    private boolean evaluateAction(ImagingActionEvaluator evaluator, NodeRef evalNode) {
        
        boolean display = true;
    
        ActionEvaluator actionEvaluator = evaluatorRegistry.getEvaluatorByBeanName(evaluator.getName());
        if (actionEvaluator != null) {
            if (evaluator.isNegate()) {
                display = !actionEvaluator.evaluate(evalNode);
            } else {
                display = actionEvaluator.evaluate(evalNode);
            }
        } else {
            logger.error("EVALUATOR not found : " + evaluator.getName());
        }
        
        return display;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setImagingAuditService(ImagingAuditService imagingAuditService) {
		this.imagingAuditService = imagingAuditService;
	}

	public void setClientServerService(ClientServerService clientServerService) {
		this.clientServerService = clientServerService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setEvaluatorRegistry(EvaluatorRegistry evaluatorRegistry) {
        this.evaluatorRegistry = evaluatorRegistry;
    }

	public void setGlobalProperties(Properties globalProperties) {
		ImagingScript.globalPropertiesStatic = globalProperties;
	}

	public void setPathToArchive(String pathToArchive) {
		ImagingScript.pathToArchive = pathToArchive;
	}

	public void setPathToCertificate(String pathToCertificate) {
		ImagingScript.pathToCertificate = pathToCertificate;
	}
	
}
