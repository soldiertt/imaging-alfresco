{
"actions" : [
	<#list actions as action> 
	{
		"name" : "${action.name}",
		"type" : "${action.type}",
		"url" : "${action.url}",
		"urlTarget" : "${action.urlTarget}"
	}
	<#if action_has_next> , </#if>
	</#list>
	]
}