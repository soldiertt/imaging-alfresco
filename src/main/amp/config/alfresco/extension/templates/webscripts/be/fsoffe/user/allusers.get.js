function main () {
	logger.log("ws : allusers");
	var allusers = search.luceneSearch("TYPE:\"{http://www.alfresco.org/model/content/1.0}person\"");
	model.allusers = allusers;
}

try {
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}