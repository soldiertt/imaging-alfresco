function main() {
   
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef");
		logger.log("action : sendtoworkflow : " + nodeRef);
		var docnode = utils.getNodeFromString(nodeRef);
		docnode.removeAspect("fds:workitem");
		var entryFolder = companyhome.childByNamePath("Entry");
		docnode.move(entryFolder);
		model.actionstatus = "OK";
		
	} else {
		//No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log("Webscript validation did not pass !");
	}
}

main(); //Need rollback if error, do not catch exception
