function main () {
	if (args.nodeRef != null && args.type != null) {
		var docnode = utils.getNodeFromString(args.nodeRef);
		if (args.type == "imports") {
			var subFolder = docnode.childByNamePath(args.type);
			if (subFolder == null) {
				model.documents = new Array();
			} else {
				var documents = subFolder.children;
				model.documents = documents;
			}
			if (docnode.hasAspect("fds:workitem") && docnode.properties["fds:itemOwner"] === person.properties.userName) {
				model.isinmyworkitem = "yes";
			} else {
				model.isinmyworkitem = "no";
			}
		} else {
			status.code = 400;
			status.message = "Bad request : invalid 'type' parameter !";
			status.redirect = true;
			logger.log(status.message);
		}
	} else {
		status.code = 400;
		status.message = "Bad request : missing required 'nodeRef' or 'type' parameter !";
		status.redirect = true;
		logger.log(status.message);
	}
	 
}

try {
	logger.log("ws : listsubdocs");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}