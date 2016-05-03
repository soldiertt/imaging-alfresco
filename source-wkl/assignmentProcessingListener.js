logger.log("Assignment Processing Listener");
logger.log("------------------------------");

for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	
	logger.log("document "+document.name);
	
	//Ajout de l'aspect workitem
	if(document.hasAspect("fds:workitem") == false) {
		document.addAspect("fds:workitem");
		document.save();
	}
	
	document.properties["fds:itemOwner"] = person.properties.userName;
	document.properties["fds:itemEntryTime"] = new Date();
	
	document.save();
	
	var workingCopy = document.checkout();
	execution.setVariable("workingCopyId",workingCopy.id);
}