function main () {
	
	var nodeRef = args.nodeRef;
	logger.log("ws : hasmypersonalaspect : " + nodeRef);
	var curDate = new Date();
	var docnode = utils.getNodeFromString(nodeRef);
	if (docnode.hasAspect("fds:mypersonal")) {
		model.hasaspect = "yes";
		if (docnode.properties["fds:mypersAssignee"] == person.properties.userName) {
			model.isinmypers = "yes";
		} else {
			model.isinmypers = "no";
		}
		model.type = docnode.properties["fds:mypersType"];
	} else {
		model.hasaspect = "no";
		model.isinmypers = "";
		model.type = "";
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