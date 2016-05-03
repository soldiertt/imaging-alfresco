function main () {
	if (args.nodeRef != null) {
		logger.log("ws : parentdoc : " + args.nodeRef);
		var contentNode = utils.getNodeFromString(args.nodeRef);
		if (contentNode.typeShort == "fds:content") { 
			var fdsDocument = null;
			var parentnode = contentNode.parent;
			if (parentnode.typeShort == "fds:document") {
				fdsDocument = parentnode;
			} else if (parentnode.typeShort == "fds:folder") {
				fdsDocument = parentnode.parent;
			}
	
			model.docid = fdsDocument.id;
		} else {
			status.code = 400;
			status.message = "Bad request : node is not a content !";
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