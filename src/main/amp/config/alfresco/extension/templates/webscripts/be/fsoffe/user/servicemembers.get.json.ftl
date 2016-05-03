{
"members" : [
	<#list servicemembers as member>
		{ 
		 "username":"${member.getShortName()}",
		 "displayname":"${member.getFullName()} (${member.getShortName()})"
		}
		<#if member_has_next>,</#if>
	</#list>
	]
}