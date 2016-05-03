function main() {
   
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef");
		logger.log("action : myperstoworkitems : " + nodeRef);
		var curDate = new Date();
		var docnode = utils.getNodeFromString(nodeRef);
		if (docnode.hasAspect("fds:workitem") && docnode.properties["fds:itemOwner"] == person.properties.userName) {
			model.actionstatus = "OK";
		} else if (!docnode.hasAspect("fds:workitem")) {
			// WORK ITEM ASPECT
			var properties = new Array(2);
			properties["fds:itemOwner"] = person.properties.userName;
			properties["fds:itemEntryTime"] = curDate;
			docnode.addAspect("fds:workitem", properties);
			model.actionstatus = "OK";
		} else {
			//No rollback needed
			model.actionstatus = "NOK";
		    model.errormessage = "Invalid itemOwner !";
		    logger.log(model.errormessage);
		}
	} else {
		//No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log(model.errormessage);
	}
}

main(); //Need rollback if error, do not catch exception