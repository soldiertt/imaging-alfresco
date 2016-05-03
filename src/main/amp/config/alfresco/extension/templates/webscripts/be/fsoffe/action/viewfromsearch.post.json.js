function main() {
   
	var nodeRef = json.get("nodeRef");
	logger.log("action : viewfromsearch : " + nodeRef);
	
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var curDate = new Date();
		var docnode = utils.getNodeFromString(nodeRef);
		
		var isColllaborator = docnode.hasPermission("Collaborator");
		
		if (docnode.hasAspect("fds:mypersonal") && docnode.properties["fds:mypersAssignee"] != person.properties.userName) {
			//Do not add workitem aspect to prevent document locking, document will be open in readonly as well
			model.actionstatus = "OK";
		} else {
			if (isColllaborator && !docnode.hasAspect("fds:workitem")) {
				var properties = new Array(2);
				properties["fds:itemOwner"] = person.properties.userName;
				properties["fds:itemEntryTime"] = curDate;
				docnode.addAspect("fds:workitem", properties);
			}
			model.actionstatus = "OK";
		}
	} else {
		// No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log(model.errormessage);
	}
}

main(); //Need rollback if error, do not catch exception