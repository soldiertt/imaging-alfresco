{
"documents" : [
<#list documents as doc>
	{
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"name":"${doc.name}",
		"doctype":"${doc.properties["fds:docType"]}",
		"docsource":"${doc.properties["fds:docSource"]!}",
		"doclinked":"${doc.properties["fds:docLinked"]?string("yes","no")}",
		"docpriority":"${doc.properties["fds:docPriority"]?string}",
		"itemowner":"${doc.properties["fds:itemOwner"]!}",
		"mypersexpeditor":"${doc.properties["fds:mypersExpeditor"]!}",
		<#if doc.properties["fds:mypersEntrytime"]??>
			"mypersentrytime":"${doc.properties["fds:mypersEntrytime"]?string("yyyy-MM-dd-HH:mm")}",
		<#else>
			"mypersentrytime":"",
		</#if>
		"mypersassignee":"${doc.properties["fds:mypersAssignee"]!}",
		<#if doc.properties["fds:mypersType"]??>
			"myperstype":"${doc.properties["fds:mypersType"]?lower_case}"
		<#else>
			"myperstype":""
		</#if>
	}
	<#if doc_has_next> , </#if>
</#list>
]
}