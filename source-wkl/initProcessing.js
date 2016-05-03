logger.log("Start Init Processing");
logger.log("---------------------");


for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	
	if(document.hasAspect("fds:workflow")) {
		
		document.properties["fds:varFirstRun"] = false ;
		
		document.properties["fds:varDefaultDestination"] = document.properties["fds:workBox"];
		document.save();
		
		var varDestinationDefault = document.properties["fds:varDefaultDestination"];
		document.properties["fds:varDestination"] = varDestinationDefault ;
		document.save();
	}
}