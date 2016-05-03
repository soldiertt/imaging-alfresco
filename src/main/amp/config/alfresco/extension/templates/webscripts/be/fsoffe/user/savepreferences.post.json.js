function getPreviousPreference(userId, prefName) {
	var docquery = "select e.fds:prefValue from fds:preferencesDataList as e WHERE e.fds:prefUserid='"
		 + userId + "' AND e.fds:prefName='" + prefName + "'";
	
    var def = {
        query : docquery,
        language : "cmis-alfresco"
    };
    
	return search.query(def);
}

function insertPreference(userId, prefName, prefValue) {
	var dlList = search.luceneSearch("TYPE:\"dl:dataList\" AND @cm\\:title:\"UserPreferences\"");
	if(dlList.length === 1) {
		var dataList = dlList[0];
		var prefNode = dataList.createNode(null,"fds:preferencesDataList");
		prefNode.properties["fds:prefUserid"] = userId;
		prefNode.properties["fds:prefName"] = prefName;
		prefNode.properties["fds:prefValue"] = prefValue;
		prefNode.save();
	}
}

function updatePreference(prefNode, prefValue) {
	prefNode.properties["fds:prefValue"] = prefValue;
	prefNode.save();
}

function isTrue(value) {
	// Force conversion of value to String (the only way)
	return value + '' === 'true';
}

function main() {
	
	var userId = person.properties.userName,
		autoview = json.get("autoview"),
		autoform = json.get("autoform"),
		landingpage = json.get("landingpage"),
		annotationautosave = json.get("annotationautosave"),
		results;
	
	logger.log("ws : savepreferences for " + userId);
	logger.log("autoview = " + autoview); 
	logger.log("autoform = " + autoform); 
	logger.log("landingpage = " + landingpage); 
	logger.log("annotationautosave = " + annotationautosave); 
	
	if (autoview != null && autoform != null && landingpage != null && annotationautosave != null) {
		
	    results = getPreviousPreference(userId, "autoview");
	    
	    if (results.length == 1 && !isTrue(autoview)) {
	        results[0].remove(); // Remove preferences so the default value is false
	    } else if (results.length == 0 && isTrue(autoview)) {
	    	insertPreference(userId, "autoview", autoview); // Add the preference document to set the preference value to true
	    }	
	    
	    results = getPreviousPreference(userId, "autoform");
    
	    if (results.length == 1 && !isTrue(autoform)) {
	        results[0].remove(); // Remove preferences so the default value is false
	    } else if (results.length == 0 && isTrue(autoform)) {
	    	insertPreference(userId, "autoform", autoform); // Add the preference document to set the preference value to true
	    }
    
	    results = getPreviousPreference(userId, "annotationautosave");
	    if (results.length == 1 && !isTrue(annotationautosave)) {
	        results[0].remove(); // Remove preferences so the default value is false
	    } else if (results.length == 0 && isTrue(annotationautosave)) {
	    	insertPreference(userId, "annotationautosave", annotationautosave); // Add the preference document to set the preference value to true
	    }
	    
	    results = getPreviousPreference(userId, "landingpage");
	    
	    if (results.length == 1) { // A preference != "inbox" is saved
		    if (landingpage == "inbox") { // Default value
		        results[0].remove(); // Remove preferences so the default value is taken
		    } else {
		    	updatePreference(results[0], landingpage);
		    }
	    } else if (landingpage != "inbox") { // If desired value is != from default value
	    	insertPreference(userId, "landingpage", landingpage); // Add the preference document
	    }
	    
	    model.actionstatus = "OK";
	    
	} else {
		//No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log("Webscript validation did not pass !");
	}
}

main(); //Need rollback if error, do not catch exception