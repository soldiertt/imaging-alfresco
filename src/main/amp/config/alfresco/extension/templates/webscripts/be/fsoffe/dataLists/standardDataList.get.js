function main() {
	
	var type = args.type;
	var docquery = "select e.fds:typeParamStandard, e.fds:idParamStandard, e.fds:libParamStandard from fds:standardDataList as e where e.fds:typeParamStandard='"+type+"' order by e.fds:libParamStandard";
	
    var def = {
        query : docquery,
        language : "cmis-alfresco"
    };

    var results = search.query(def);
    if (results != null) {
    	model.standardDataList = results ;
    }
    
}

try {
	logger.log("ws : standardDataList");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message; 
    status.redirect = true; 
    logger.log(status.message);
}