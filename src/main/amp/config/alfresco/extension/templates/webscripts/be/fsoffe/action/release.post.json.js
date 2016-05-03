function main() {
   
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef");
		logger.log("action : release : " + nodeRef);
		var docnode = utils.getNodeFromString(nodeRef);
		docnode.removeAspect("fds:workitem");
		model.actionstatus = "OK";
		
	} else {
		// No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log(model.errormessage);
	}
}

main(); //Need rollback if error, do not catch exception