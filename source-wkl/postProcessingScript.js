/**
 * getParamMapping
 * @param typeDocument 	optional
 *
 * @return resultBoxes return the destination boxes
 */
function getParamMapping(typeDocument) {
	var resultBoxes;
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

logger.log("Start Task Post Processing");
logger.log("-------------------------");

var varExit = 0;

for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	
	var typeDocument = document.properties["fds:docType"];
	var docStatus = document.properties["fds:docDossierStatus"];
	var docClass = document.properties["fds:docClass"];
	var docLinked = document.properties["fds:docLinked"];
	//Recup du parent
	var inboxParent = document.getParent() ;
	logger.log("inboxParent "+inboxParent.name) ;
	
	var varStandardWorkset =execution.getVariable("varStandardWorkset");
	var varDestination = execution.getVariable("varDestination");
	var varCountCollaborative = execution.getVariable("varCountCollaborative");
	var varClass = execution.getVariable("varClass");
	
	logger.log("varDestination "+varDestination) ;
	
	if(inboxParent.name == "FONDSBOX") {
		varExit = 0;
		execution.setVariable("varFirstRun",true);
		
		var resultParamMapping = getParamMapping(typeDocument);
		execution.setVariable("varStandardWorkset",resultParamMapping);
	}
	else {
		if(inboxParent.name == "DISPATCHING") {
			if(typeDocument == "NOTTOKEEP") {
				varExit = 2;
			}
			else {
				varExit = 0;
				execution.setVariable("varFirstRun",true);
				
				var resultParamMapping = getParamMapping(typeDocument);
				execution.setVariable("varStandardWorkset",resultParamMapping);
			}
		}
		else {
			if(varDestination == "EXIT") {
				if(varCountCollaborative > 0) {
					execution.setVariable("varNextWorkset",varStandardWorkset);
					execution.setVariable("varAssignee",null);
					varExit = 0;
				}
				else {
					if(inboxParent.name == "IDWG" && typeDocument == "F1 CONTR") {
						if(docStatus == 1 || docStatus == 9) {
							execution.setVariable("varNextWorkset","VOORBER_PREPAR");
							execution.setVariable("varStandardWorkset","VOORBER_PREPAR");
							
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
				execution.setVariable("varNextWorkset",varDestination);
				varExit = 0;
				
				if(varDestination == "DISPATCHING" && docLinked) {
					
					var message = "Linked document cannot be sent to Dispatching";
					execution.setVariable("wkMessages",message);
					
					execution.setVariable("varNextWorkset",inboxParent.name);
				}
				else {
					varCountCollaborative = varCountCollaborative + 1 ;
					
					if(varCountCollaborative == 1) {
						execution.setVariable("varStandardWorkset",inboxParent.name);
					}
				}
			}
		}
	}
	
	execution.setVariable("varExitPoP",varExit);
	
	// Sortie du post Processing
	if(varExit == 0) {
		logger.log("BEHANDELING") ;
	}
	else if(varExit == 1) {
		logger.log("CHECK LINK") ;
	}
	else if(varExit == 2) {
		logger.log("REMOVE FROM WORKFLOW") ;
	}
}