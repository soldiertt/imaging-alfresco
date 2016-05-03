function main () {
	if (args.nodeRef != null) {
		logger.log("ws : docdetails : " + args.nodeRef);
		var docnode = utils.getNodeFromString(args.nodeRef),
			icon;
		model.docname = docnode.name;
		model.parentboxid = docnode.parent.id;
		
		if (docnode.hasAspect("fds:mypersonal")) {
			icon = "home.gif";
		} else {
			if (docnode.hasAspect("fds:workflow")) {
				logger.log("Set workflow icon ");
				icon = "wf-16.png";
				if ((docnode.properties["fds:varCountCollaborative"] > 0) && (docnode.properties["fds:varStandardWorkset"] != docnode.properties["fds:varNextWorkset"])) {
					// change the icon for postman activity as it's not a true collaborative workflow 
					logger.log("Set collaborative icon "); 
					icon = "collaborative-16.png";
				} 
			} else {
				icon = "repository-16.png";
			}
		}
		model.icon = icon;
		
	} else {
		status.code = 400;
		status.message = "Bad request : missing required 'nodeRef' !";
		status.redirect = true;
		logger.log(status.message);
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