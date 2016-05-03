{
"allusers" : [
<#list boxusers as user>
	{
		"userid":"${user.getShortName()}",
		"displayname":"${user.getFullName()} (${user.getShortName()})"
	}
	<#if user_has_next> , </#if>
</#list>
]
}