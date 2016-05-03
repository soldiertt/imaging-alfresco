function main() {
   
	var nodeRef = json.get("nodeRef");
	logger.log("action : stampauditevent : " + nodeRef);
	var docnode = utils.getNodeFromString(nodeRef);
	if (!docnode.hasAspect("fds:workflow")) {
		insertStatistiqueAction(docnode, person.properties.userName);
		imaging.logAuditEvent(person.properties.userName, docnode.id, 
				docnode.properties["fds:docType"], "Repository", "Repository");
	}
	model.actionstatus = "OK";

}

function insertStatistiqueAction(doc, actor) {
	var dlList = search.luceneSearch("TYPE:\"dl:dataList\" AND @cm\\:title:\"StatistiquesActions\"");
	
	if(dlList.length === 1) {

		var dataList = dlList[0];
		var stats = dataList.createNode(null,"fds:statistiqueDataList");
		stats.properties["fds:statIdDoc"] = doc.id ;
		stats.properties["fds:statDocName"] = doc.name ;
		stats.properties["fds:statDocType"] = doc.properties["fds:docType"] ;
		stats.properties["fds:statDocFrom"] = "Repository";
		stats.properties["fds:statDocTo"] = "Repository";  
		stats.properties["fds:statActorid"] = actor ;
		stats.properties["fds:statActions"] = "Stamp" ;
		stats.properties["fds:statActorEntryTime"] = new Date();
		stats.save();
	  
	}
}

main(); //Need rollback if error, do not catch exception