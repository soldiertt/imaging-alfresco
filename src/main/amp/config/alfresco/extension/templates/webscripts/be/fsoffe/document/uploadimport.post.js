function main() {
	var filename = null;
	var content = null;
	var destination = "";
	var docNode, importsNode, fullFilename;
	
	logger.log("ws : uploadimport");
	
	// locate file attributes
	for each (field in formdata.fields) {
	  if (field.name == "destination") {
	    destination = field.value;
	  } else if (field.name == "file" && field.isFile) {
	    filename = field.filename;
	    content = field.content;
	  }
	}
	
	// ensure mandatory file attributes have been located
	if (filename == undefined || content == undefined || destination == "") {
		//No rollback needed
		status.code = 400;
		status.message = "Uploaded file cannot be located in request";
		status.redirect = true;
		logger.log(status.message);
	} else {
	  
		docNode = utils.getNodeFromString(destination);
		importsNode = docNode.childByNamePath("imports");
		if (importsNode == null) {
			importsNode = docNode.createNode("imports", "fds:folder");
		}
		fullFilename = getUniqueName(importsNode, filename);
		upload = importsNode.createFile(fullFilename) ;
		upload.properties.content.write(content, false, true);
		upload.properties.content.encoding = "UTF-8";
		upload.specializeType("fds:content");
		upload.properties["fds:contentOrigin"] = "Internal";
		upload.save();
  
		// setup model for response template
		model.upload = upload;
	}
}

function getUniqueName(parentNode, filename) {
	var counter = 0,
		checkNodeExist = parentNode.childByNamePath(filename),
		lastDotIndex,
		fileprefix = "", 
		extension = "";
	
	while (checkNodeExist != null) {
		counter++;
		lastDotIndex = filename.lastIndexOf(".");
		if (lastDotIndex == -1) {
			fileprefix = filename;
		} else {
			fileprefix = filename.substr(0,lastDotIndex);
			extension = filename.substr(lastDotIndex);
		}
		checkNodeExist = parentNode.childByNamePath(fileprefix + "_" + counter + extension);
	}
	
	if (counter != 0) {
		return fileprefix + "_" + counter + extension;
	} else {
		return filename;
	}
}


main(); //Need rollback if error, do not catch exception
