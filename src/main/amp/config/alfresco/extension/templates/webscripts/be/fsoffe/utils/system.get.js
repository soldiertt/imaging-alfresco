function main() {
	
	model.vtihost = imaging.getAlfGlobal("vti.server.external.host");
	model.vtiport = imaging.getAlfGlobal("vti.server.external.port");
}

try {
	logger.log("ws : system");
	main(); // No rollback needed, can catch exception
} catch(e) {
	status.code = 500; 
    status.message = "Webscript error : " + e.name + " - " + e.message;
    status.redirect = true; 
    logger.log(status.message);
}