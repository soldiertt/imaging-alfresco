logger.log("Complete Processing Listener");
logger.log("------------------------------");

logger.log(person.properties.userName);
var userName = person.properties.userName;
execution.setVariable("varCurrentUser",userName);


for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	//var document = bpm_package.children[i];
		
	// Le document est liberé
	//Réservation du document
	var workingCopyeId = execution.getVariable("workingCopyId");
	
	var workingCopy = utils.getNodeFromString("workspace://SpacesStore/"+workingCopyeId) ;
	logger.log("workingCopy "+workingCopy.name);
	
	var document = workingCopy.cancelCheckout();
	
	logger.log("document "+document.name);
	if(document.hasAspect("fds:workitem") == true) {
		logger.log("Remove workitem Aspect");
		document.removeAspect("fds:workitem");
		document.save();
	}
	
	var typeDocument = task.getVariable("fdswk_boxDocType");
	var docStatus = task.getVariable("fdswk_boxDocStatus");
	var numDossierNr = task.getVariable("fdswk_dossierNr");
	var resultVarBox = task.getVariable("fdswk_boxList");
	var resultVarAssignee = task.getVariable("fdswk_boxAssignee");
	
	execution.setVariable("varDefaultDestination",resultVarBox);
	if(resultVarAssignee != null) {
		execution.setVariable("varAssignee",resultVarAssignee.properties.userName) ;
	}
		
	document.properties["fds:docType"] = typeDocument ;
	
	if(document.hasAspect("fds:workflow") == true) {
		document.properties["fds:workDossierNr"] = numDossierNr ;
		document.properties["fds:workDossierStatus"] = docStatus ;
		document.properties["fds:workBox"] = resultVarBox;
		if(resultVarAssignee != null) {
			document.properties["fds:workAssignee"] = resultVarAssignee.properties.userName ;
		}
		document.save();
	}
		
	document.save();
}

logger.log("Start Post Processing Listener");
logger.log("------------------------------");

execution.setVariable("varUser",userName);
execution.setVariable("varFirstRun",false);
var varDestinationDefault = execution.getVariable("varDefaultDestination");
execution.setVariable("varDestination",varDestinationDefault);
logger.log("varDestinationDefault "+varDestinationDefault);
logger.log("varDestination "+execution.getVariable("varDestination"));

