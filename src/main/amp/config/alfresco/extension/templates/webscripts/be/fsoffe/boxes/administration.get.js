function contains(a, obj) {
    var i = a.length;
    while (i--) {
       if (a[i] == obj) {
           return true;
       }
    }
    return false;
}

function main () { 
	
	var allLockedDocuments = search.luceneSearch("workspace://SpacesStore", "EXACTTYPE:\"fds:document\" AND (ASPECT:\"fds:mypersonal\" OR ASPECT:\"fds:workitem\")"); 
	var filteredDocuments = [];
	var serviceMembers = [];
	
	if (people.isAdmin(person)) {
		model.documents = allLockedDocuments;
	} else {
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
						serviceMembers.push(grpMembers[j].getShortName());
					}
				}
			}
		}
		for(var i=0; i < allLockedDocuments.length; i++){
			if (allLockedDocuments[i].parent.hasPermission("Collaborator")) {
				if (allLockedDocuments[i].hasAspect("fds:mypersonal") && contains(serviceMembers, allLockedDocuments[i].properties["fds:mypersAssignee"])) {
					filteredDocuments.push(allLockedDocuments[i]);
				} else if (allLockedDocuments[i].hasAspect("fds:workitem") && contains(serviceMembers, allLockedDocuments[i].properties["fds:itemOwner"])) {
					filteredDocuments.push(allLockedDocuments[i]);
				}
			} else {
				logger.log("WARNING ADMIN : user '" + person.properties.userName + "' has access to '" + allLockedDocuments[i].name + "' but not to its parent box !!");
				logger.log("Check if the user is not owner of the document.");
			}
		}
		model.documents = filteredDocuments;
	}
       
} 

try {
	logger.log("ws : administration");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}
