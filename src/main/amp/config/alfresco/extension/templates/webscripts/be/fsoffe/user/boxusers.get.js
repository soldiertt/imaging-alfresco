function main () {
	var boxname=args.boxname,
		groupname,
		imagingGroup,
		boxusers = [];
	
	if (boxname != null) {
		groupname = "IMG-" + boxname.toUpperCase();
		imagingGroup = groups.getGroup(groupname);
		
		if (imagingGroup != null) {
			logger.log("found group : " + groupname);
			boxusers = imagingGroup.getAllUsers();
		} else {
			logger.log("error : cannot find group " + groupname);
		}
	} 
    
	model.boxusers = boxusers;
	 
}

try {
	logger.log("ws : boxusers");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}