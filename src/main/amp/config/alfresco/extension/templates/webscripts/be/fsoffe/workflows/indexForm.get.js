function main () {
	var nodeRef = args.nodeRef;
	if (nodeRef != null) {
		logger.log("ws : indexForm : " + nodeRef);
		var document = utils.getNodeFromString(nodeRef);
		if (document.hasAspect("fds:workflow")) {
			model.haswfaspect = "yes";
		} else {
			model.haswfaspect = "no";
		}
		model.document = document;
	} else {
		// No rollback needed
		status.code = 400;
		status.message = "Bad request : missing required 'nodeRef' !";
		status.redirect = true;
	}
}

try {
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}