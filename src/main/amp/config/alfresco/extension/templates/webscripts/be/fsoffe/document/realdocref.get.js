function main () {
	
	if (args.nodeRef != null) {
		logger.log("ws : realdocref : " + args.nodeRef);
		var docnode = utils.getNodeFromString(args.nodeRef);
		var docChildren = docnode.children;
		var contentdoc = null;
		for (var i = 0; i < docChildren.length; i++) {
			if (docChildren[i].typeShort == "fds:content") {
				contentdoc = docChildren[i];
				break;
			}
		}
		
		if (contentdoc != null) {
			model.docid = contentdoc.id;
		} else {
			status.code = 500;
			status.message = "Internal error : cannot find a child content document !";
			status.redirect = true;
			logger.log(status.message);
		}
		
	} else {
		status.code = 400;
		status.message = "Bad request : missing required 'nodeRef' parameter !";
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