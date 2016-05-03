{
"documents" : [
<#list documents as doc>
	{
		"doctype":"${doc[0]}",
		<#if bylettertype>
		"lettertype":"${doc[1]!}",
		"count":"${doc[2]}"
		</#if>
		<#if !bylettertype>
		"count":"${doc[1]}"
		</#if>
	}
	<#if doc_has_next> , </#if>
</#list>
]
}