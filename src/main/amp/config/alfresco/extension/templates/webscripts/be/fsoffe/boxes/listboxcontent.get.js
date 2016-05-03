function main () { 
    if (args.nodeRef != null && args.assignedtome != null && args.showworkitems) { 
        var assignedtome = false; 
        var onlyworkitems = false;
        if (args.assignedtome == "yes") { 
        	assignedtome = true; 
        } 
        if (args.showworkitems == "yes") { 
        	onlyworkitems = true; 
        }
        var boxnode = utils.getNodeFromString(args.nodeRef); 
        var childrenNodes = boxnode.children; 
        var documents = new Array();
        var pushit;
        for (var i = 0; i < childrenNodes.length; i++) { 
            if (childrenNodes[i].typeShort == "fds:document") {
            	if (onlyworkitems) {
            		pushit = childrenNodes[i].hasAspect("fds:workitem");
            	} else {
            		pushit = !childrenNodes[i].hasAspect("fds:workitem");
            	}
            	if (assignedtome) {
            		pushit = pushit && childrenNodes[i].properties["fds:workAssignee"] == person.properties.userName;
            	}
                if (pushit) { 
                    documents.push(childrenNodes[i]); 
                } 
            } 
        } 
        model.boxname = boxnode.name;
        model.documents = documents; 
    } else { 
        status.code = 400; 
        status.message = "Bad request : missing required 'nodeRef' or 'assignedtome' parameter !"; 
        status.redirect = true; 
    } 
       
} 
 
try {
	logger.log("ws : listboxcontent");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true;
    logger.log(status.message);
}
