function main() {
	
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef");
		logger.log("action : send : " + nodeRef);
	    var assignee = json.get("assignee");
    	var docnode = utils.getNodeFromString(nodeRef);
    	var priority = json.get("priority");
    	
    	docnode.removeAspect("fds:workitem");
  	  
    	if (docnode.hasAspect("fds:mypersonal")) {
    		docnode.properties["fds:mypersExpeditor"] = person.properties.userName;
    		docnode.properties["fds:mypersAssignee"] = assignee;
    		docnode.properties["fds:mypersEntrytime"] = new Date();
    		docnode.properties["fds:mypersType"] = "REQUEST";
    		docnode.properties["fds:docPriority"] = priority;
    		docnode.save();
    	} else {
	    	var properties = new Array(3);
	    	properties["fds:mypersExpeditor"] = person.properties.userName;
	    	properties["fds:mypersAssignee"] = assignee;
	    	properties["fds:mypersEntrytime"] = new Date();
	    	docnode.addAspect("fds:mypersonal", properties);
	    	docnode.properties["fds:docPriority"] = priority;
    		docnode.save();
    	}
    	insertStatistiqueAction(docnode, person.properties.userName, "My Personal");
    	imaging.logAuditEvent(person.properties.userName, docnode.id, 
				docnode.properties["fds:docType"], "Repository", "MyPersonal");
    	model.actionstatus = "OK";
	   
	} else {
		//No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log(model.errormessage);
	}
}

function insertStatistiqueAction(doc, actor, destination) {
	var dlList = search.luceneSearch("TYPE:\"dl:dataList\" AND @cm\\:title:\"StatistiquesActions\"");
	
	if(dlList.length === 1) {

		var dataList = dlList[0];
		var stats = dataList.createNode(null,"fds:statistiqueDataList");
		stats.properties["fds:statIdDoc"] = doc.id ;
		stats.properties["fds:statDocName"] = doc.name ;
		stats.properties["fds:statDocType"] = doc.properties["fds:docType"] ;
		stats.properties["fds:statDocFrom"] = "Repository";
		stats.properties["fds:statDocTo"] = destination;  
		stats.properties["fds:statActorid"] = actor ;
		stats.properties["fds:statActions"] = "Send" ;
		stats.properties["fds:statActorEntryTime"] = new Date();
		stats.save();
	  
	}
}

main(); //Need rollback if error, do not catch exception