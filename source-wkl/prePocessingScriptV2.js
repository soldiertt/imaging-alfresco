/*
	Algo
	
	Recup le type de document ...
	
	Si une condition particuliére existe, il faut la coder manuellement
	Requete de recherche dans la datalist pour recupérer la box d'output.
	
	
 */

function getLibelleBox(boxId) {
	var libelle="";
	var docquery = "select e.* from fds:boxesDataList as e where e.fds:idBoxe='"+boxId+"'";
	
	var def = {
			query : docquery,
			language : "cmis-alfresco"
		
		};
		
		var results = search.query(def);
		
		if(results != null) {
			
			if(results.length > 0) {
				libelle = results[0].properties["fds:libBoxe"];
			}
		}
		return libelle;
}

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
	var libBoxes;
	var firstRun = document.properties["fds:varFirstRun"];
	
	// Gestion des cas particulier
	if(typeDocument == "NONE") {
		if(docSource == "mail") {
			boxes = "FONDSBOX";
			libBoxes = "FONDSBOX";
		}
		else {
			boxes = "DISPATCHING";
			libBoxes = "DISPATCHING";
		}
	}
	else {
		if(typeDocument == "NOTTOKEEP") {
			boxes = "DISPATCHING";
			libBoxes = "DISPATCHING";
		}
		else {
			if(docClass == "Archive") {
				boxes = "LIEN_KOPPELING";
				libBoxes = "LIEN-KOPPELING";
			}
			else {
				if(firstRun == true) {
					var resultMapping = getParamMapping(typeDocument);
					
					if(resultMapping != null) {
						boxes = resultMapping;
						libBoxes = getLibelleBox(resultMapping);
					}
				}
				else {
					logger.log("PostProcessing defined the next WorkSet ");
					var varNextWorkset = document.properties["fds:varNextWorkset"];
					boxes = varNextWorkset ;
					libBoxes = getLibelleBox(boxes);
					
					if(document.hasAspect("fds:workflow")) {
						document.properties["fds:workEntryTime"] = new Date() ;
						document.save();
					}
				}
			}
		}
	}
		
		
	// Le dossier va etre mappee dans la bonne box
	var boxesDestination = "Boxes/"+libBoxes;
	logger.log("Boxes Destination : "+ boxesDestination);
	var boxesDestinationPath = companyhome.childByNamePath(boxesDestination);
	
	if(boxesDestinationPath == null) {
		logger.log("Unknown destination");
	}
	else {
		logger.log("Mapping Dossier "+dossierName+" to boxes "+boxesDestination);
		
		logger.log(person.properties.userName);
		boxesDestinationPath.setPermission("OnlyWrite","GROUP_EVERYONE");
		
		if(document.hasAspect("fds:workflow")) {
			
			document.properties["fds:workBox"] = "EXIT" ;
			document.properties["fds:varDestination"] = boxes ;
			document.properties["fds:workEntryTime"] = new Date() ;
			document.save();
		}
		
		document.move(boxesDestinationPath);
		
		
	}
}