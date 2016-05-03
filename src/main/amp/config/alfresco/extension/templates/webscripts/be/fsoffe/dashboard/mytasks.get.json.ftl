{
"documents" : [
<#list documents as doc>
	{
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"name":"${doc.name}",
		"doctype":"${doc.properties['fds:docType']!}",
		"docsource":"${doc.properties["fds:docSource"]!}",
		"workexpeditor":"${doc.properties["fds:workExpeditor"]!}",
		"inbox":"${doc.parent.name}",
		"doclinked":"${doc.properties["fds:docLinked"]?string("yes","no")}"
	}
	<#if doc_has_next> , </#if>
</#list>
]
}