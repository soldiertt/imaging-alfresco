function insertStatistiqueAction(doc,actor) {
	var dlList = search.luceneSearch("TYPE:\"dl:dataList\"");

	for(var i=0;i<dlList.length;i++) {

	  var dataList = dlList[i];
	  
	  logger.log(dataList.properties["cm:title"]);
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

logger.log("Start Task Check List Processing");
logger.log("--------------------------------");

var varExit ;

for(var i=0;i<bpm_package.children.length;i++) {
	
	// The Attached Document 
	var document = bpm_package.children[i];
	
	var docLinked = document.properties["fds:docLinked"];
	var message;
	if(docLinked) {
		message = "";
		varExit = 1;
	}
	else {
		var varDestination = document.properties["fds:varDestination"];
		message = "This document is not linked";
		document.properties["fds:varNextWorkset"] = varDestination;
		document.save();
		
		varExit = 0;
		
		//On enregistre les actions sur le document
		insertStatistiqueAction(document,document.properties["fds:workExpeditor"]);
	}
	document.properties["fds:wkMessage"] = message ;
	document.save();
	execution.setVariable("varExitCl",varExit);
	
	// Sortie du post Processing
	if(varExit == 0) {
		logger.log("BEHANDELING") ;
	}
	else if(varExit == 1) {
		logger.log("REMOVE FROM WORKFLOW") ;
	}
	
}