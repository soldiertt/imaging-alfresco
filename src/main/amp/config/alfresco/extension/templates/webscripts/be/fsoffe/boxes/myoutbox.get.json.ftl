{
"outboxes" : [
<#list outboxes as outbox>
	{
		"nodeRef":"workspace://SpacesStore/${outbox[0].properties["fds:statIdDoc"]}",
		"name":"${outbox[0].properties["fds:statDocName"]}",
		"doctype":"${outbox[0].properties["fds:statDocType"]}",
		"from":"${outbox[0].properties["fds:statDocFrom"]}",
		"to":"${outbox[0].properties["fds:statDocTo"]}",
		"senttime":"${outbox[0].properties["fds:statActorEntryTime"]?string("yyyy-MM-dd-HH:mm")}",
		"inbox":"${outbox[1].parent.name}",
		<#if outbox[1].hasAspect("fds:workflow") || outbox[1].hasAspect("fds:workitem") || outbox[1].hasAspect("fds:mypersonal") >
			"checkbox" : "no"
		<#else>
			"checkbox" : "yes"
		</#if>
	}
	<#if outbox_has_next> , </#if>
</#list>
]
}