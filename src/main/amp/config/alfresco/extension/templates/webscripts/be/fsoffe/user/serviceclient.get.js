function main () {
	var ismember = false,
		targetGroup = groups.getGroup("IMG-BRIEF-LETTRE-CLIENT");
	
	var members = targetGroup.getAllUsers();
	
	for (var i = 0; i < members.length; i++) {
		if (members[i].getShortName() == person.properties.userName) {
			ismember = true;
			break;
		}
	}
    
	model.serviceclient = ismember;
	 
}

try {
	logger.log("ws : serviceclient");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}