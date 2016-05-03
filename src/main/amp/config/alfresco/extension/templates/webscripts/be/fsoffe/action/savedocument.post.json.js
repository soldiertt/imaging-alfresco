function getMainDoc(docnode) {
	var docChildren = docnode.children;
	var contentdoc = null;
	for (var i = 0; i < docChildren.length; i++) {
		if (docChildren[i].typeShort == "fds:content") {
			contentdoc = docChildren[i];
			break;
		}
	}
	return contentdoc;
}

function unindexDocument(docnode) {
	var pdfNode = getMainDoc(docnode);
	if (pdfNode != null) {
		pdfNode.properties["cm:isContentIndexed"]=false;
		pdfNode.save();
		pdfNode.properties.content.write(pdfNode.properties.content);
	}
}
function reindexDocument(docnode) {
	var pdfNode = getMainDoc(docnode);
	if (pdfNode != null) {
		pdfNode.properties["cm:isContentIndexed"]=true;
		pdfNode.save();
		pdfNode.properties.content.write(pdfNode.properties.content);
	}
}

function main() {
	
	var ok = imaging.validate(webscript, json);
	
	if (ok) {
		var nodeRef = json.get("nodeRef"),
			doctypeBefore = "",
			doctypeAfter = "",
			fieldValue,
			intArr = [],
			DOCTYPES_TO_INDEX_CONTENT = ["BRIEF/LETTRE GAAJ", "F1 CONTR", "F1 BT CE"],
			DOCTYPES_WITH_KEYWORDS_ASPECT = ["BRIEF/LETTRE GAAJ"];
		
		logger.log("action : savedocument : " + nodeRef);
		var docnode = utils.getNodeFromString(nodeRef);
		jsonObj = jsonUtils.toObject(json);
		for (var field in jsonObj) {
			fieldValue = jsonObj[field];
			if (field != "nodeRef") {
				logger.log("field '" + field + "' value '" + fieldValue + "'");
				if (field === "fds:docType") {
					doctypeBefore = docnode.properties[field];
					doctypeAfter = fieldValue;
					logger.log("doc type before - after: " + doctypeBefore + " - " + doctypeAfter);
					if (DOCTYPES_TO_INDEX_CONTENT.indexOf(doctypeBefore) != -1 && DOCTYPES_TO_INDEX_CONTENT.indexOf(doctypeAfter) == -1) {
						logger.log("unindexing document...");
						unindexDocument(docnode);
					} else if (DOCTYPES_TO_INDEX_CONTENT.indexOf(doctypeBefore) == -1 && DOCTYPES_TO_INDEX_CONTENT.indexOf(doctypeAfter) != -1) {
						logger.log("reindexing document...");
						reindexDocument(docnode);
					}
					if (DOCTYPES_WITH_KEYWORDS_ASPECT.indexOf(doctypeBefore) != -1 && DOCTYPES_WITH_KEYWORDS_ASPECT.indexOf(doctypeAfter) == -1) {
						docnode.removeAspect("fds:keywordsAspect");
					} else if (DOCTYPES_WITH_KEYWORDS_ASPECT.indexOf(doctypeBefore) == -1 && DOCTYPES_WITH_KEYWORDS_ASPECT.indexOf(doctypeAfter) != -1) {
						docnode.addAspect("fds:keywordsAspect");
					}
					docnode.properties[field]=fieldValue;
				} else if (field === "fds:workDossierStatus") {
					if (fieldValue != "") {
						docnode.properties[field]=parseInt(fieldValue);
					} else {
						docnode.properties[field]=null;
					}
				} else if (field === "fds:keywords") { //Array of int
					var fieldValueArray = fieldValue.toArray();
					fieldValueArray.forEach(function(elem, i) {
						if (elem != "") {
							intArr.push(parseInt(elem));
						}
					});
					docnode.properties[field]=intArr;
				} else {
					docnode.properties[field]=fieldValue;
				}
			}
		}
		docnode.save();
		model.actionstatus = "OK";
	} else {
		//No rollback needed
		model.actionstatus = "NOK";
	    model.errormessage = "Webscript validation did not pass !";
	    logger.log(model.errormessage);
	}
}

main(); //Need rollback if error, do not catch exception
