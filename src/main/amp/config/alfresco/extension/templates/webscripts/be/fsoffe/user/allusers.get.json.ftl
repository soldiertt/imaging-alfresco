{
"allusers" : [
<#list allusers as user>
	{
		"userid":"${user.properties["cm:userName"]}",
		"displayname":"${user.properties["cm:firstName"]} <#if user.properties["cm:lastName"]??>${user.properties["cm:lastName"]}</#if> (${user.properties["cm:userName"]})"
	}
	<#if user_has_next> , </#if>
</#list>
]
}