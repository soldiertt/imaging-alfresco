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
    	  stats.properties["fds:statDocTo"] = "Trashcan" ;  
	      
	      stats.properties["fds:statActorid"] = actor ;
	      stats.properties["fds:statActions"] = "SendToDefault" ;
	      stats.properties["fds:statActorEntryTime"] = new Date();
	      stats.save();
	    }
	}
}

logger.log("Task Remove From Repository");
logger.log("---------------------------");

for(var i=0;i<bpm_package.children.length;i++) {
	
	// The Attached Document 
	var document = bpm_package.children[i];
	var dossierName = document.name;
	
	//Statistique OutBox
	insertStatistiqueAction(document,document.properties["fds:workExpeditor"]);
	
	//Suppression du document
	var deleteImagingFolderDestination = "Delete_Imaging_Folder";
	var deleteImagingFolderDestinationPath = companyhome.childByNamePath(deleteImagingFolderDestination);
	
	if(deleteImagingFolderDestinationPath == null) {
		logger.log("Delete_Imaging_Folder - Unknown destination");
	}
	else {
		logger.log("Suppression Dossier "+dossierName);
		document.move(deleteImagingFolderDestinationPath);
	}
}