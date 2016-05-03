{
"limited" : "${limited?string("yes","no")}",
"error" : "${error!}",
"documents" : [
<#list documents as doc>
	{
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"docindate":"${doc.properties["fds:docInDate"]?string("yyyy-MM-dd-HH.mm.ss")}",
		"hasaspectmypers":"${doc.hasAspect("fds:mypersonal")?string}",
		"collaborator":"${doc.hasPermission("Collaborator")?string("yes","no")}",
		"name":"${doc.name}",
		"docdossiernr":"${doc.properties["fds:docDossierNr"]!}",
		"doctype":"${doc.properties["fds:docType"]!}",
		<#if doc.displayPath?index_of("/Final/") != -1 >
			"inbox":"Repository",
		<#else>
            "inbox":"",
		</#if>
		"doclinked":"${doc.properties["fds:docLinked"]?string("yes","no")}",
		"docsource":"${doc.properties["fds:docSource"]!}",
		"docletter":"${doc.properties["fds:docLetter"]!}",
		"itemowner":"${doc.properties["fds:itemOwner"]!}",
		"workexpeditor":"${doc.properties["fds:workExpeditor"]!}",
		<#if doc.properties["fds:workEntryTime"]??>
			"workentrytime":"${doc.properties["fds:workEntryTime"]?string("yyyy-MM-dd-HH.mm.ss")}"
		<#else>
			"workentrytime":""
		</#if>
	}
	<#if doc_has_next> , </#if>
</#list>
]
}