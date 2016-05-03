function main () {
	
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef"),
			docToSend = utils.getNodeFromString(nodeRef),
			wklDescription = docToSend.name,
			actionStatus,
			workflowName = "activiti$imagingProcessingWorkflow",
			processedBy = person.properties.userName,
			workBox;
		
		logger.log("Running send to default with user : " + processedBy);
			
		if (docToSend.hasAspect("fds:workflow")) {
			
			workBox = docToSend.properties["fds:workBox"];
			
			if (workBox === "MY PERSONAL") {
				logger.log("Also send to mypersonal");
				docToSend.addAspect("fds:mypersonalCandidate");
				docToSend.properties["fds:workBox"] = "EXIT";
			}
			
			docToSend.properties["cm:owner"] = processedBy;
			docToSend.properties["fds:workExpeditor"] = processedBy;
			docToSend.properties["fds:docProcessedBy"] = processedBy;
			docToSend.save();
			
			//Enlever l'aspect workitem
			docToSend.removeAspect("fds:workitem");
			
			var workflow = actions.create("start-workflow");
			workflow.parameters.workflowName = workflowName;
			workflow.parameters["bpm:workflowDescription"] = wklDescription;
			workflow.parameters["sendEMailNotifications"] = false;
			workflow.execute(docToSend);
			
			logger.log("Send To Default : " + docToSend.name);
			actionStatus = "OK";
			
		} else if (docToSend.hasAspect("fds:mypersonal") && docToSend.properties["fds:mypersAssignee"] === person.properties.userName) {
	
			docToSend.properties["cm:owner"] = person.properties.userName;
			docToSend.save();
			
			//Enlever l'aspect workitem
			docToSend.removeAspect("fds:workitem");
			
			//My personal workflow
			docToSend.removeAspect("fds:mypersonal");
			insertStatistiqueAction(docToSend, person.properties.userName, "My personal", "Repository");
			imaging.logAuditEvent(person.properties.userName, docToSend.id, 
					docToSend.properties["fds:docType"], "MyPersonal", "Repository");
			docToSend.save();
			logger.log("Send To Default (My personal) : " + docToSend.name);
			
			actionStatus = "OK";
		} else if (docToSend.hasAspect("fds:mypersonal") && docToSend.properties["fds:mypersAssignee"] !== person.properties.userName) {
			// No rollback needed
			actionStatus = "NOK";
			model.errormessage = "Your are not the assignee of the document !"; 
		} else {
			// No rollback needed
			actionStatus = "NOK";
			model.errormessage = "Document is not in the workflow or in My Personal !"; 
		}
		
		model.contentNode = docToSend;
		model.actionstatus = actionStatus;
	} else {
		// No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log(model.errormessage);
	}
}

function insertStatistiqueAction(doc, actor, from, destination) {
	var dlList = search.luceneSearch("TYPE:\"dl:dataList\" AND @cm\\:title:\"StatistiquesActions\"");
	
	if(dlList.length === 1) {

		var dataList = dlList[0];
		var stats = dataList.createNode(null,"fds:statistiqueDataList");
		stats.properties["fds:statIdDoc"] = doc.id ;
		stats.properties["fds:statDocName"] = doc.name ;
		stats.properties["fds:statDocType"] = doc.properties["fds:docType"] ;
		stats.properties["fds:statDocFrom"] = from;
		stats.properties["fds:statDocTo"] = destination;  
		stats.properties["fds:statActorid"] = actor ;
		stats.properties["fds:statActions"] = "SendToDefault" ;
		stats.properties["fds:statActorEntryTime"] = new Date();
		stats.save();
	  
	}
}

main(); //Need rollback if error, do not catch exception