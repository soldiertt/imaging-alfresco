{
"boxes" : 
[
	<#list boxes as box> 
	{
		"nodeRef" : "${box.nodeRef}",
		"name" : "${box.name}",
		"docCount" : ${box.docCount?c}
	}
	<#if box_has_next> , </#if>
	</#list>
]
}