function main () {

	// WORK ITEMS
	// 1 - must be fds:document
	// 2 - aspect fds:workitem
	// 3 - fds:itemOwner = user
	
	var wiQuery = "EXACTTYPE:\"fds:document\" AND ASPECT:\"fds:workitem\" AND @fds\\:itemOwner:\"" + person.properties.userName + "\"";
	var results = search.luceneSearch("workspace://SpacesStore", wiQuery);
	model.documents = results;
}

try {
	logger.log("ws : myworkitems");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}