function main () {
	
	var nodeRef = args.nodeRef;
	logger.log("ws : allowedit : " + nodeRef);
	var docnode = utils.getNodeFromString(nodeRef);
	if (docnode.hasAspect("fds:workitem") 
			&& docnode.properties["fds:itemOwner"] == person.properties.userName) {
		model.allowedit = "true";
	} else if (docnode.hasAspect("fds:workitem")) {
		model.allowedit = "false";
		model.hasaspectworkitem = "true";
		model.hasaspectmypersonal = "false";
		model.itemowner = docnode.properties["fds:itemOwner"];
	}  else if (docnode.hasAspect("fds:mypersonal")) {
		model.allowedit = "false";
		model.hasaspectworkitem = "false";
		model.hasaspectmypersonal = "true";
		model.mypersassignee = docnode.properties["fds:mypersAssignee"];
	} else {
		model.allowedit = "false";
		model.hasaspectworkitem = "false";
		model.hasaspectmypersonal = "false";
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