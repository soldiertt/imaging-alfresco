{
"documents" : [
<#list documents as doc>
	{
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"name":"${doc.name}",
		"hasaspectmypers":"${doc.hasAspect("fds:mypersonal")?string}",
		<#if doc.hasAspect("fds:mypersonal")>
			"wficon":"home.gif",
		<#else> 
			<#if doc.hasAspect("fds:workflow") >
				<#if doc.properties["fds:varCountCollaborative"] &gt; 0 >
					"wficon": "collaborative-16.png",
				<#else>
					"wficon": "wf-16.png",
				</#if>
			</#if>
		</#if>
		<#if doc.hasAspect("fds:mypersonal") >
			"inbox":"MY PERSONAL",
		<#elseif doc.displayPath?index_of("/Boxes/") != -1 >
			"inbox":"${doc.parent.name}",
		<#else>
			"inbox":"Repository",
		</#if>
		"doctype":"${doc.properties["fds:docType"]}",
		"doclinked":"${doc.properties["fds:docLinked"]?string("yes","no")}",
		"docsource":"${doc.properties["fds:docSource"]!}",
		"itemowner":"${doc.properties["fds:itemOwner"]!}",
		"mypersassignee":"${doc.properties["fds:mypersAssignee"]!}",
		"workexpeditor":"${doc.properties["fds:workExpeditor"]!}",
		"mypersexpeditor":"${doc.properties["fds:mypersExpeditor"]!}",
		<#if doc.properties["fds:workEntryTime"]??>
			"workentrytime":"${doc.properties["fds:workEntryTime"]?string("yyyy-MM-dd-HH:mm")}",
		<#else>
			"workentrytime":"",
		</#if>
		<#if doc.properties["fds:mypersEntrytime"]??>
			"mypersentrytime":"${doc.properties["fds:mypersEntrytime"]?string("yyyy-MM-dd-HH:mm")}"
		<#else>
			"mypersentrytime":""
		</#if>
	}
	<#if doc_has_next> , </#if>
</#list>
]
}