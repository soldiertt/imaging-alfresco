function main () {
	
	var username = person.properties.userName;
	var dateRangeStart = "";
	var dateRangeEnd = "";
	var byLetterType = false;
	
	if (args.username != null) {
		username = args.username;
	}
	if (args.dateRangeStart != null) {
		dateRangeStart = args.dateRangeStart;
	}
	if (args.dateRangeEnd != null) {
		dateRangeEnd = args.dateRangeEnd;
	}
	if (args.byLetterType != null && args.byLetterType === "true") {
		byLetterType = true;
	}
	model.bylettertype = byLetterType;
	model.documents = imaging.activitiesByUserAndDate(username, dateRangeStart, dateRangeEnd, byLetterType);
 
}

try {
	logger.log("ws : myactivities");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}