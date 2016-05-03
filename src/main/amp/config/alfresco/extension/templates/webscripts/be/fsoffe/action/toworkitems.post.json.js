function main() {
   
	var nodeRef = json.get("nodeRef");
	logger.log("action : toworkitems : " + nodeRef);
	var curDate = new Date();
	var docnode = utils.getNodeFromString(nodeRef);
	if (docnode.hasAspect("fds:workitem")) {
		var currentOwner = docnode.properties["fds:itemOwner"];
		if (currentOwner == person.properties.userName) {
			// Already in my workitem
			model.actionstatus = "OK";
		} else {
			//No rollback needed
			model.actionstatus = "NOK";
		    model.errormessage = "This document has been already taken by another user (" + currentOwner + "). Please refresh the inbox.";
		    logger.log(model.errormessage);
		}
	} else if (!docnode.hasAspect("fds:workitem")) {
		var ok = imaging.validate(webscript, json);
		if (ok) {
			// WORK ITEM ASPECT
			var properties = new Array(2);
			properties["fds:itemOwner"] = person.properties.userName;
			properties["fds:itemEntryTime"] = curDate;
			docnode.addAspect("fds:workitem", properties);
			model.actionstatus = "OK";
		} else {
			//No rollback needed
			model.actionstatus = "NOK";
		    model.errormessage = "Webscript validation did not pass !";
		    logger.log(model.errormessage);
		}
	}
}

main(); //Need rollback if error, do not catch exception