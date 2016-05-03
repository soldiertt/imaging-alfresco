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


logger.log("Start Task Start Processing");
logger.log("---------------------------");
for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	
	logger.log("document "+document.name);

	var typeDocument = document.properties["fds:docType"];
	var docClass = document.properties["fds:docClass"];
	var docSource = document.properties["fds:docSource"];
	
	logger.log("typeDocument "+typeDocument);
	logger.log("docClass "+docClass);
	logger.log("docSource "+docSource);
	
	var workExpeditor ;
	
	if(docSource == "mail") {
		workExpeditor = "Mail" ;
	}
	else if(docSource == "scanner") {
		workExpeditor = "Scanner" ;
	}
	else if(docSource == "upload") {
		workExpeditor = "Upload" ;
	}
	else if(docSource == "printer") {
		workExpeditor = "Printer" ;
	}
	logger.log("workExpeditor "+workExpeditor);
	
	if(document.hasAspect("fds:workflow")) {
		document.properties["fds:workExpeditor"] = workExpeditor ;
		document.save();
	}
	else {
		document.addAspect("fds:workflow");
		document.properties["fds:workExpeditor"] = workExpeditor ;
		document.save();
	}
	
	
	execution.setVariable("varDestination","");
	execution.setVariable("varStandardWorkset","");
	execution.setVariable("varCountCollaborative",0);
	execution.setVariable("varFirstRun",true);
	execution.setVariable("varDefaultDestination","EXIT");
	execution.setVariable("wkMessages","");
	execution.setVariable("varUser",workExpeditor);
	execution.setVariable("varDocType",typeDocument);
	
	
	if(docClass != "Archive" && docSource != "mail") {
		var mappingDestination = getParamMapping(typeDocument) ;
		execution.setVariable("varDestination",mappingDestination);
		execution.setVariable("varStandardWorkset",mappingDestination);
	}
}