logger.log("Create Listener Processing ");
logger.log("--------------------------");
for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	
	logger.log("document "+document.name);
	
	var typeDocument = document.properties["fds:docType"];
	var docClass = document.properties["fds:docClass"];
	var docStatus = document.properties["fds:docDossierStatus"];
	
	// Recopie des valeurs du document pour le workflow
	task.setVariable("fdswk_boxDocClass",docClass);
	task.setVariable("fdswk_boxDocType",typeDocument);
	task.setVariable("fdswk_boxDocStatus",docStatus);
		
	task.setVariable("fdswk_messages",execution.getVariable("wkMessages"));
}