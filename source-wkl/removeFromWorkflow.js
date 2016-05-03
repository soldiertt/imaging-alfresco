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
    	  stats.properties["fds:statDocTo"] = "Repository" ;  
	      
	      stats.properties["fds:statActorid"] = actor ;
	      stats.properties["fds:statActions"] = "SendToDefault" ;
	      stats.properties["fds:statActorEntryTime"] = new Date();
	      stats.save();
	    }
	}
}	

logger.log("Task Remove From Workflow");
logger.log("-------------------------");

for(var i=0;i<bpm_package.children.length;i++) {
	
	// The Attached Document 
	var document = bpm_package.children[i];
	var dossierName = document.name;
	
	//Statistique OutBox
	insertStatistiqueAction(document,document.properties["fds:workExpeditor"]);
	
	if(document.hasAspect("fds:workflow") == true) {
		logger.log("Remove fds:workflow Aspect");
		document.removeAspect("fds:workflow");
		document.save();
	}
	
	var storageDestination = "Storage";
	var storageDestinationPath = companyhome.childByNamePath(storageDestination);
	
	if(storageDestinationPath == null) {
		logger.log("Storage - Unknown destination");
	}
	else {
		logger.log("Storage Dossier "+dossierName);
		document.move(storageDestinationPath);
	}
}


