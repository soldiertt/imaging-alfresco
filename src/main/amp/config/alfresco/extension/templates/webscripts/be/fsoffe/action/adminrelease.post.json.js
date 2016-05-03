function main() {
   
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef");
		logger.log("action : adminrelease : " + nodeRef);
		var docnode = utils.getNodeFromString(nodeRef);
		if (docnode.hasAspect("fds:workitem")) {
			docnode.removeAspect("fds:workitem");
		}
		if (docnode.hasAspect("fds:mypersonal")) {
			docnode.removeAspect("fds:mypersonal");
		}
		model.actionstatus = "OK";
		
	} else {
		//No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log("Webscript validation did not pass !");
	}
}

main(); //Need rollback if error, do not catch exception
