{
"documents" : [
<#list documents as doc>
	{
		"docid":"${doc.id}",
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"name":"${doc.name}",
		"creator":"${doc.properties.creator}",
		"path":"${doc.displayPath}",
		"mimetype":"${doc.mimetype}"
	}
	<#if doc_has_next> , </#if>
</#list>
],
"isinmyworkitem": "${isinmyworkitem}"
}