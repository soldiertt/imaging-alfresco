logger.log("Start Task Check List Processing");
logger.log("--------------------------------");

var varExit ;

for(var i=0;i<bpm_package.children.length;i++) {
	
	// The Attached Document 
	var document = bpm_package.children[i];
	
	var docLinked = document.properties["fds:docLinked"];
	var message;
	if(docLinked) {
		message = "";
		varExit = 1;
	}
	else {
		var varDestination = execution.getVariable("varDestination");
		message = "This document is not linked";
		execution.setVariable("varNextWorkset",varDestination);
		
		varExit = 0;
	}
	execution.setVariable("wkMessages",message);
	execution.setVariable("varExitCl",varExit);
	
	// Sortie du post Processing
	if(varExit == 0) {
		logger.log("BEHANDELING") ;
	}
	else if(varExit == 1) {
		logger.log("REMOVE FROM WORKFLOW") ;
	}
	
}