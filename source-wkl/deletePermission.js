logger.log("Take Delete Permission");
logger.log("----------------------");

var varExit = 0;

for(var i=0;i<bpm_package.children.length;i++) {

	// The Attached Document 
	var document = bpm_package.children[i];
	var inboxParent = document.getParent() ;
	
	logger.log("varDestination "+document.properties["fds:varDestination"]);
	
	logger.log(inboxParent.name);
	
	for(var x=0;x<inboxParent.getPermissions().length;x++) {

		  var permission = inboxParent.getPermissions()[x] ;
		  
		  if(permission.indexOf("OnlyWrite") > 0) {
			  logger.log("Enleve la permission");
			  inboxParent.removePermission("OnlyWrite","GROUP_EVERYONE");
		  }
		    
	}
	
}