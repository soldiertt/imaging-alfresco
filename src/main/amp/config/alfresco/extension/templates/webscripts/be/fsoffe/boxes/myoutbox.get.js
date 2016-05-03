function getOutBoxFrom(user) {

	var docquery = "select e.* from fds:statistiqueDataList as e where e.fds:statActorid='"+user+"' ORDER BY e.fds:statActorEntryTime DESC";
      
    var def = {
    		query : docquery,
    		language : "cmis-alfresco"
    };
              
    var results = search.query(def);
    
    if(results != null) {
    	return results;
    }
        
}

/**
 * Get the difference between two date.
 * Difference in :
 * Days
 * Hours
 * Minutes
 * Secons
 * @param date1 the first date
 * @param date2 the second date, the most recent
 * @returns the difference object 
 */
function differenceBetweenDate(date1,date2) {
	var diff =[];
	
	date1 = date1.getTime();
	date2 = date2.getTime();
	
	var tmp = date2 - date1 ;
	
	tmp = Math.floor(tmp/1000);
	diff.sec = tmp % 60;
	
	tmp = Math.floor((tmp-diff.sec)/60);
	diff.min = tmp % 60;
	
	tmp = Math.floor((tmp-diff.min)/60);
	diff.hour = tmp % 24;
	
	tmp = Math.floor((tmp-diff.hour)/24);
	diff.day = tmp;
	
	return diff;
}

function getImagingParameter(parameterName) {

    var docquery = "select e.* from fds:imagingParametersList as e where e.fds:idParam='"+parameterName+"'";
    var value="";
    var def = {
        query : docquery,
        language : "cmis-alfresco"
    };
            
    var results = search.query(def);
          
    if(results != null) {
    	value = results[0].properties["fds:valueParam"] ;
                 
    }
return value;
      
}

function main () {

	var outboxes = getOutBoxFrom(person.properties.userName),
		nbDays = getImagingParameter("outboxDays"),
		distinctResults = [], //Array of arrays with 2 elements (statdoc node and document node)
		distinctDocIds = [];
	
	for (var i = 0; i < outboxes.length; i++) {
		var outbox = outboxes[i],
	 		currentDocId=outbox.properties["fds:statIdDoc"],
	 		dateStat = outbox.properties["fds:statActorEntryTime"],
	 		now = new Date(),
	 		diffDate = differenceBetweenDate(dateStat,now);
	 	
	 	if (distinctDocIds.indexOf(currentDocId) == -1 && diffDate.day <= nbDays) {
	 		var targetNode = utils.getNodeFromString("workspace://SpacesStore/" + currentDocId);
	 		try {
		 		if (targetNode != null && targetNode.hasPermission("Collaborator")) {
			 		if (targetNode.parent.hasPermission("Collaborator")) {
				 		distinctResults.push([outbox, targetNode]);
				 		distinctDocIds.push(currentDocId);
			 		} else {
			 			logger.log("WARNING OUTBOX : user '" + person.properties.userName + "' has access to '" + targetNode.name + "' (" + currentDocId + ") but not to its parent box !!");
						logger.log("Check if the user is not owner of the document.");
			 		}
		 		} else {
		 			logger.log("WARNING : user '" + person.properties.userName + "' has no more access to '" + "workspace://SpacesStore/" + currentDocId + "'");
		 		}
	 		} catch (e) {
	 			logger.log("Outbox Exception : " + e.message);
	 		}
	 	}
	  
	} 
	
	model.outboxes = distinctResults; 
	
}

try {
	logger.log("ws : myoutbox");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}