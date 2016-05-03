{
	"boxesDataList":
	[
		<#list boxesDataList as box>
		{
			"lib":"${box.properties["fds:libBoxe"]!}",
			"state":"${box.properties["fds:stateBoxe"]?string!}"	
		}
		<#if box_has_next>,</#if>
		</#list>
	]
}