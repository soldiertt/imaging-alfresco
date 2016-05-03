function insertStatistiqueAction(doc,actor) {
	var dlList = search.luceneSearch("TYPE:\"dl:dataList\"");

	for(var i=0;i<dlList.length;i++) {

	  var dataList = dlList[i];
	  
	  if(dataList.properties["cm:title"] == "StatistiquesActions") {
	      var stats = dataList.createNode(null,"fds:statistiqueDataList");
	      stats.properties["fds:statIdDoc"] = doc.id ;
	      stats.properties["fds:statDocName"] = doc.name ;
	      stats.properties["fds:statDocType"] = doc.properties["fds:docType"] ;
	      stats.properties["fds:statDocFrom"] = doc.parent.name ;
    	  stats.properties["fds:statDocTo"] = "Workflow" ;  
	      
	      stats.properties["fds:statActorid"] = actor ;
	      stats.properties["fds:statActions"] = "SendToDefault" ;
	      stats.properties["fds:statActorEntryTime"] = new Date();
	      stats.save();
	    }
	}
}

logger.log("Start Task Post Processing");
logger.log("-------------------------");

var varExit = 0;

for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	
	var typeDocument = document.properties["fds:docType"];
	var workDossierStatus = document.properties["fds:workDossierStatus"];
	var docLinked = document.properties["fds:docLinked"];
	//Recup du parent
	var inboxParent = document.getParent() ;
	logger.log("inboxParent "+inboxParent.name) ;
	
	var varStandardWorkset = document.properties["fds:varStandardWorkset"];
	var varDestination = document.properties["fds:varDestination"];
	var varCountCollaborative = document.properties["fds:varCountCollaborative"];
	
	logger.log("varDestination "+varDestination) ;
	
	if(inboxParent.name == "FONDSBOX") {
		varExit = 0;
		
		document.properties["fds:varFirstRun"] = true;
		document.save();
	}
	else {
		if(inboxParent.name == "DISPATCHING") {
			if(typeDocument == "NOTTOKEEP") {
				varExit = 2;
			}
			else {
				varExit = 0;
				
				document.properties["fds:varFirstRun"] = true;
				document.save();
			}
		}
		else {
			if(varDestination == "EXIT") {
				if(varCountCollaborative > 0) {
					
					document.properties["fds:varNextWorkset"] = varStandardWorkset;
					document.properties["fds:varAssignee"] = null;
					document.properties["fds:varCountCollaborative"] = 0 ;
					document.save();
					varExit = 0;
				}
				else {
					logger.log("workDossierStatus "+workDossierStatus);
					if(inboxParent.name == "IDWG" && typeDocument == "F1 CONTR") {
						if(workDossierStatus == 1 || workDossierStatus == 9) {
							document.properties["fds:varNextWorkset"] = "VOORBER_PREPAR";
							document.properties["fds:varStandardWorkset"] = "VOORBER_PREPAR";
							document.save();
							varExit = 0;
						}
						else {
							varExit = 1;
						}
					}
					else {
						varExit = 1;
					}
				}
			}
			else {
				document.properties["fds:varNextWorkset"] = varDestination;
				document.save();
				varExit = 0;
				
				if(varDestination == "DISPATCHING" && docLinked) {
					
					var message = "Linked document cannot be sent to Dispatching";
					document.properties["fds:wkMessage"] = message ;
					
					document.properties["fds:varNextWorkset"] = inboxParent.name;
					document.save();
				}
				else {
					varCountCollaborative = varCountCollaborative + 1 ;
					
					if(varCountCollaborative == 1) {
						document.properties["fds:varStandardWorkset"] = inboxParent.name;
						document.save();
					}
					document.properties["fds:varCountCollaborative"] = varCountCollaborative ;
					document.save();
					
				}
			}
		}
	}
	
	execution.setVariable("varExitPoP",varExit);
	
	// Sortie du post Processing
	if(varExit == 0) {
		logger.log("BEHANDELING") ;
		//On enregistre les actions sur le document
		insertStatistiqueAction(document,document.properties["fds:workExpeditor"]);
	}
	else if(varExit == 1) {
		logger.log("CHECK LINK") ;
	}
	else if(varExit == 2) {
		logger.log("REMOVE FROM REPOSITORY") ;
	}
}