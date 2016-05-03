function main () {
	var boxesPath = imaging.getConstant("boxesPathWithPrefixes");
	var results = search.luceneSearch("workspace://SpacesStore", "PATH:\"" + boxesPath + "//*\" AND EXACTTYPE:\"fds:document\" AND NOT ASPECT:\"fds:workitem\" AND ASPECT:\"fds:workflow\" AND @fds\\:workAssignee:\"" + person.properties.userName + "\"");
	model.documents = results;
}

try {
	logger.log("ws : mytasks");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}