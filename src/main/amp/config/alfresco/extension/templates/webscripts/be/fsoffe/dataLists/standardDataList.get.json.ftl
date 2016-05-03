{
	"standardDataList":
	[
		<#list standardDataList as std>
		{
			"id":"${std.properties["fds:idParamStandard"]!}",	
			"lib":"${std.properties["fds:libParamStandard"]!}"	
		}
		<#if std_has_next>,</#if>
		</#list>
	]
}