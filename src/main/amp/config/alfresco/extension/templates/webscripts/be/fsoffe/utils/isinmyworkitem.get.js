function main () {
	
	var nodeRef = args.nodeRef;
	logger.log("ws : isinmyworkitem : " + nodeRef);
	var curDate = new Date();
	var docnode = utils.getNodeFromString(nodeRef);
	if (docnode.hasAspect("fds:workitem")) {
		model.hasaspect = "yes";
		if (docnode.properties["fds:itemOwner"] == person.properties.userName) {
			model.isinmyworkitem = "yes";
		} else {
			model.isinmyworkitem = "no";
		}
	} else {
		model.hasaspect = "no";
		model.isinmyworkitem = "no";
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