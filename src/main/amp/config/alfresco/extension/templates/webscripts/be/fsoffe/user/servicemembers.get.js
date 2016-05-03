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
    
	var serviceMembers = [];
	
	if (servicelead) {
		//Service leader
		//1. Get user groups
		var leadGroups = people.getContainerGroups(person);
		for(var i=0; i < leadGroups.length; i++){
			var groupName = leadGroups[i].getQnamePath();
			groupName = groupName.substr(groupName.indexOf("cm:GROUP_") + 9);
			if (groupName != "IMG-SERVICE-LEADS") {
				var group = groups.getGroup(groupName);
				if (group != null) {
					//2. Get group members
					var grpMembers = group.getAllUsers();
					for(var j=0; j < grpMembers.length; j++){
						//3. Put user in an array
						serviceMembers.push(grpMembers[j]);
					}
				}
			}
		}
	} 
	model.servicemembers = serviceMembers;
}

try {
	logger.log("ws : servicemembers");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}