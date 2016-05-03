/*
	Algo
	
	Recup le type de document ...
	
	Si une condition particuliére existe, il faut la coder manuellement
	Requete de recherche dans la datalist pour recupérer la box d'output.
	
	
 */

/**
 * getParamMapping
 * @param typeDocument 	optional
 *
 * @return resultBoxes return the destination boxes
 */
function getParamMapping(typeDocument) {
	var resultBoxes=null;
	var docquery = "select e.* from fds:routingTableDataList as e where e.fds:docTypeDlInput='"+typeDocument+"'"; ;
		
	var def = {
		query : docquery,
		language : "cmis-alfresco"
	
	};
	
	var results = search.query(def);
	
	if(results != null) {
		
		if(results.length > 0) {
			resultBoxes = results[0].properties["fds:routineTableOutput"];
		}
	}
	
	return resultBoxes ;
} 


logger.log("Start Task Pre Processing");
logger.log("-------------------------");


for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];

	var typeDocument = document.properties["fds:docType"];
	var docClass = document.properties["fds:docClass"];
	var docSource = document.properties["fds:docSource"];
	
	var workDossierStatus = document.properties["fds:workDossierStatus"];
	var workDossierNr = document.properties["fds:workDossierNr"];
	
	var dossierName = document.name;
	
	var boxes;
	
	//variable temporaire juste pour les tests
	//var firstRun = true;
	var firstRun = execution.getVariable("varFirstRun");
	
	execution.setVariable("varDefaultDestination","EXIT");
	execution.setVariable("varClass",docClass);
	execution.setVariable("varDocType",typeDocument);
	execution.setVariable("varStatus",workDossierStatus);
	execution.setVariable("varDossierNr",workDossierNr);
	
	logger.log("typeDocument ="+typeDocument);
	logger.log("workDossierStatus ="+workDossierStatus);
	
	// Gestion des cas particulier
	if(typeDocument == null || typeDocument == "") {
		if(docSource = "mail") {
			boxes = "FONDSBOX";
		}
		else {
			boxes = "DISPATCHING";
		}
	}
	else {
		if(typeDocument == "NOTTOKEEP") {
			boxes = "DISPATCHING";
		}
		else {
			if(docClass == "Archive") {
				boxes = "LIEN-KOPPELING";
			}
			else {
				if(firstRun == true) {
					var resultMapping = getParamMapping(typeDocument);
					
					if(resultMapping != null) {
						boxes = 	resultMapping
					}
				}
				else {
					logger.log("PostProcessing defined the next WorkSet ");
					var varNextWorkset = execution.getVariable("varNextWorkset");
					execution.setVariable("varDestination",varNextWorkset);
					boxes = varNextWorkset ;
					
					if(document.hasAspect("fds:workflow")) {
						document.properties["fds:workAssignee"] = execution.getVariable("varAssignee") ;
						document.properties["fds:workExpeditor"] = execution.getVariable("varUser") ;
						document.properties["fds:workEntryTime"] = new Date() ;
						document.properties["fds:workDossierStatus"] = execution.getVariable("varStatus");
						document.properties["fds:workDossierNr"] = execution.getVariable("varDossierNr") ;
						document.save();
					}
				}
			}
		}
	}
		
	//Gestion du groupe des boxes
	var groupNames = new java.util.ArrayList();
	var saveBoxers = boxes;
	var groupName = "GROUP_"+saveBoxers.replace(' ','_');
	logger.log("groupName "+groupName);
	groupNames.add(groupName);
	
	execution.setVariable("boxesMembers",groupNames);
	
	// Le dossier va etre mappee dans la bonne box
	execution.setVariable("varDestination",boxes);
	execution.setVariable("varStandardWorkset",boxes);
	
	var boxesDestination = "Boxes/"+boxes;
	logger.log("Boxes Destination : "+ boxesDestination);
	var boxesDestinationPath = companyhome.childByNamePath(boxesDestination);
	
	if(boxesDestinationPath == null) {
		logger.log("Unknown destination");
	}
	else {
		logger.log("Mapping Dossier "+dossierName+" to boxes "+boxesDestination);
		document.move(boxesDestinationPath);
		
		document.properties["fds:workEntryTime"] = new Date() ;
		document.save();
		
	}
}