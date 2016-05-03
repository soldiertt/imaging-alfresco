function main() {

	var prefName = args.prefName,
		docquery = "select e.fds:prefValue from fds:preferencesDataList as e WHERE e.fds:prefUserid='"
		 + person.properties.userName + "' AND e.fds:prefName='" + prefName + "'";
	
    var def = {
        query : docquery,
        language : "cmis-alfresco"
    };

    var results = search.query(def);
    if (results != null && results.length == 1) {
        
    	model.prefValue = results[0].properties["fds:prefValue"] ;
		
    } else {
    	// manage DEFAULT values
    	if (prefName === "autoview" || prefName === "autoform" || prefName === "annotationautosave") {
    		model.prefValue = "false";
    	} else if (prefName === "landingpage") {
    		model.prefValue = "inbox";
    	} else {
    		model.prefValue = "ERROR";
    	}
    }
    
}

try {
	logger.log("ws : getuserpref");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}