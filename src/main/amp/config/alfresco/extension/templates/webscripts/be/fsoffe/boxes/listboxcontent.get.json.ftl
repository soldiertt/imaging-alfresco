{
"boxname" : "${boxname}",
"documents" : [
<#list documents as doc>
	{
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"name":"${doc.name}",
		"doctype":"${doc.properties["fds:docType"]!}",
		<#if doc.properties["fds:workDossierStatus"]??>
			"docstatus":${doc.properties["fds:workDossierStatus"]?c},
		</#if>
		"docdossiernr":"${doc.properties["fds:docDossierNr"]!}",
		"doclinked":"${doc.properties["fds:docLinked"]?string("yes","no")}",
		"docsource":"${doc.properties["fds:docSource"]!}",
		<#if doc.properties["fds:docLetterType"]?? && doc.properties["fds:docLetterType"] != 'NONE' >
			"doclettertype":"${doc.properties["fds:docLetterType"]}",
		<#else>
			"doclettertype":"",
		</#if>
		"itemowner":"${doc.properties["fds:itemOwner"]!}",
		"workexpeditor":"${doc.properties["fds:workExpeditor"]!}",
		<#if doc.properties["fds:workEntryTime"]??>
			"workentrytime":"${doc.properties["fds:workEntryTime"]?string("yyyy-MM-dd-HH:mm")}",
		<#else>
			"workentrytime":"",
		</#if>
		"workassignee":"${doc.properties["fds:workAssignee"]!}",
		"workpending":"${doc.properties["fds:workPending"]?string("yes","no")}",
		"wkmessage":"${doc.properties["fds:wkMessage"]!}"
	}
	<#if doc_has_next> , </#if>
</#list>
]
}