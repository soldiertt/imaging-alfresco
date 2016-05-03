function main () {
	var servicelead = false,
		leaderGroup = groups.getGroup("IMG-SERVICE-LEADS");
	
	var leaders = leaderGroup.getAllUsers();
	
	for (var i = 0; i < leaders.length; i++) {
		if (leaders[i].getShortName() == person.properties.userName) {
			servicelead = true;
			break;
		}
	}
    
	model.servicelead = servicelead;
	 
}

try {
	logger.log("ws : servicelead");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}