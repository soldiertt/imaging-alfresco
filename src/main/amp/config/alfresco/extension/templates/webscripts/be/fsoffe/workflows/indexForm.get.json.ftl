{
"document" : 
	{
		"name":"${document.name}",
		"nodeRef":"${document.nodeRef}",
		"parent":"${document.parent.name}",
		"docType":"${document.properties["fds:docType"]!}",
		"doclettertype":"${document.properties["fds:docLetterType"]!}",
		"docPriority":"${document.properties["fds:docPriority"]!}",
		"docprocessedby":"${document.properties["fds:docProcessedBy"]!}",
		"created":"${document.properties["fds:docInDate"]?string("yyyy-MM-dd-HH:mm")}",
		"docLinked":"${document.properties["fds:docLinked"]?string("yes","no")}",
		"haswfaspect":"${haswfaspect}",
		"varDefaultDestination":"${document.properties["fds:workBox"]!}",
		"workAssignee":"${document.properties["fds:workAssignee"]!}",
		<#if document.properties["fds:workDossierStatus"]??>
			"workStatus":${document.properties["fds:workDossierStatus"]?c},
		</#if>
		"docdossiernr":"${document.properties["fds:docDossierNr"]!}",
		<#if document.properties["fds:workEntryTime"]??>
			"workEntryTime":"${document.properties["fds:workEntryTime"]?string("yyyy-MM-dd")}",
		<#else>
			"workEntryTime":"",
		</#if>
		<#if document.properties["fds:workPending"]??>
			"workpending":"${document.properties["fds:workPending"]?string("yes","no")}",
		<#else>
			"workpending":"",
		</#if>
		"keywords":[
			<#if document.hasAspect("fds:keywordsAspect") && document.properties["fds:keywords"]?? >
				<#list document.properties["fds:keywords"] as keyword>
					${keyword}
					<#if keyword_has_next>,</#if>
				</#list>
			</#if>
		]
	}
}