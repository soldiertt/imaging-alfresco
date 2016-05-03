{
"documents" : [
<#list documents as doc>
	{
		"nodeRef":"workspace://SpacesStore/${doc.id}",
		"hasaspectmypers":"${doc.hasAspect("fds:mypersonal")?string}",
		"name":"${doc.name}",
		<#if doc.hasAspect("fds:mypersonal")>
			"wficon":"home.gif",
		<#else> 
			<#if doc.hasAspect("fds:workflow") >
                  "wficon": "wf-16.png",
				<#if doc.properties["fds:varCountCollaborative"] &gt; 0 >
                    <#if doc.properties["fds:varStandardWorkset"] != doc.properties["fds:varNextWorkset"] >
					    "wficon": "collaborative-16.png",
					</#if>  
				</#if>	
			</#if>
		</#if>
		<#if doc.displayPath?index_of("/Boxes/") != -1 >
			"inbox":"${doc.parent.name}",
		<#else>
			"inbox":"Repository",
		</#if>
		"doctype":"${doc.properties["fds:docType"]}",
		<#if doc.properties["fds:workDossierStatus"]??>
			"docstatus":${doc.properties["fds:workDossierStatus"]?c},
		</#if>	
		"docdossiernr":"${doc.properties["fds:docDossierNr"]!}",
		"doclinked":"${doc.properties["fds:docLinked"]?string("yes","no")}",
		"docsource":"${doc.properties["fds:docSource"]!}",
		<#if doc.properties["fds:docLetterType"]?? && doc.properties["fds:docLetterType"] != 'NONE' >
			"doclettertype":"${doc.properties["fds:docLetterType"]}",
		<#else>
			"doclettertype":"",
		</#if>
		"itemowner":"${doc.properties["fds:itemOwner"]!}",
		"workexpeditor":"${doc.properties["fds:workExpeditor"]!}",
		<#if doc.properties["fds:workEntryTime"]??>
			"workentrytime":"${doc.properties["fds:workEntryTime"]?string("yyyy-MM-dd-HH.mm.ss")}",
		<#else>
			"workentrytime":"",
		</#if>
		"workassignee":"${doc.properties["fds:workAssignee"]!}"
	}
	
	<#if doc_has_next> , </#if>
</#list>
]
}