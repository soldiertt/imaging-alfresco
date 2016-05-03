{
"allowedit" : "${allowedit}"
<#if allowedit == "false" >
, "hasaspectworkitem" : "${hasaspectworkitem}"
, "hasaspectmypersonal" : "${hasaspectmypersonal}"
	<#if hasaspectworkitem == "true" >
		, "itemowner" : "${itemowner}"
	</#if>
	<#if hasaspectmypersonal == "true" >
		, "mypersassignee" : "${mypersassignee}"
	</#if>
</#if>
}