function main () {
	
	for (argsV in argsM) {
		logger.log(argsV);
	}
	var nodeRefs = argsM["nodeRef[]"];
	var allhaveaspect = true;
	for each (nodeRef in nodeRefs) {
		logger.log("ws : hasworkflowaspect : " + nodeRef);
		var docnode = utils.getNodeFromString(nodeRef);
		if (!docnode.hasAspect("fds:workflow")) {
			allhaveaspect = false;
		}
	}
	
	if (allhaveaspect) {
		model.hasaspect = "yes";
	} else {
		model.hasaspect = "no";
	}
}

try {
	logger.log("ws : hasworkflowaspect");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}