function main() {
	
	var docquery = "select e.fds:stateBoxe, e.fds:libBoxe from fds:boxesDataList as e order by e.fds:libBoxe";
	
    var def = {
        query : docquery,
        language : "cmis-alfresco"
    };

    var results = search.query(def);
    if (results != null) {
    	model.boxesDataList = results ;
    }
    
}

try {
	logger.log("ws : boxesDataList");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}