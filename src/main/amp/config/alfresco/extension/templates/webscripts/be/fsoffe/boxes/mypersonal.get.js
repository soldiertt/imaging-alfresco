function main () { 
	//var finalPath = imaging.getConstant("finalPathWithPrefixes");
	//REMOVE PATH QUERY (very slow) PATH:\"/" + finalPath + "//*\" AND 
	var results = search.luceneSearch("workspace://SpacesStore", "EXACTTYPE:\"fds:document\" AND ASPECT:\"fds:mypersonal\" AND @fds\\:mypersAssignee:\"" + person.properties.userName + "\"",
			"@fds:docPriority", true);
	model.documents = results;
} 
 
try {
	logger.log("ws : mypersonal");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}