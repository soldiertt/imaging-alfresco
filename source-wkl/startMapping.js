logger.log("Start Task Start Mapping");
logger.log("------------------------");
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
	
	if(document.hasAspect("fds:workflow") == false) {
		document.addAspect("fds:workflow");
		
		document.properties["fds:workExpeditor"] = workExpeditor ;
		document.properties["fds:varDestination"] = "" ;
		document.properties["fds:varStandardWorkset"] = "" ;
		document.properties["fds:varCountCollaborative"] = 0 ;
		document.properties["fds:varFirstRun"] = true ;
		document.properties["fds:varDefaultDestination"] = "EXIT" ;
		document.properties["fds:workBox"] = "EXIT" ;
		document.save();
	}
}