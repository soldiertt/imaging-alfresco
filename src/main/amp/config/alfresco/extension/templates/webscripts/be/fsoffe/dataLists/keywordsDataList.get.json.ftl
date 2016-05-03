{
	"keywordsDataList":
	[
		<#list keywordsDataList?sort_by(sortKey) as keyword>
		{
			"id":${keyword.id},	
			"lib":"${keyword[sortKey]!}"
		}
		<#if keyword_has_next>,</#if>
		</#list>
	]
}