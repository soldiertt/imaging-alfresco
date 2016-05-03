function main() {
   
	var nodeRef = json.get("nodeRef");
	logger.log("action : releaseOptional : " + nodeRef);
	var docnode = utils.getNodeFromString(nodeRef);
	if (docnode.hasAspect("fds:workitem")) {
		if (docnode.properties["fds:itemOwner"] == person.properties.userName) {
			docnode.removeAspect("fds:workitem");
		}
	}
	model.actionstatus = "OK";

}

main(); //Need rollback if error, do not catch exception