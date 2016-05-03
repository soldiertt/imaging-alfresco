{
"contextlist" : [
	<#list contextlist as context>
	{
	"sessionid":${context.sessionId?c},
	"username":"${context.userName}",
	"refscreen":<#if context.refScreen??>${context.refScreen?c}<#else>null</#if>,
	"refdossier":<#if context.refDossier??>${context.refDossier?c}<#else>null</#if>,
	"map":"${context.map!}",
	"refemployer":<#if context.refEmployer??>${context.refEmployer?c}<#else>null</#if>,
	"refworker":<#if context.refWorker??>${context.refWorker?c}<#else>null</#if>,
	"refperson":<#if context.refPerson??>${context.refPerson?c}<#else>null</#if>,
	"refkeyword1":<#if context.refKeyword1??>${context.refKeyword1?c}<#else>null</#if>,
	"refkeyword2":<#if context.refKeyword2??>${context.refKeyword2?c}<#else>null</#if>,
	"employer":<#if context.refEmployer??>"${context.employerName?trim}"<#else>null</#if>,
	"worker":<#if context.refWorker??>"${context.workerName?trim}"<#else>null</#if>,
	"person":<#if context.refPerson??>"${context.personName?trim}"<#else>null</#if>,
	"keyword1":<#if context.refKeyword1??>"${context.keyword1Name}"<#else>null</#if>,
	"keyword2":<#if context.refKeyword2??>"${context.keyword2Name}"<#else>null</#if>,
	"specialjp":<#if context.specialjp??>"${context.specialjp?string("yes","no")}"<#else>null</#if>,
    "refgajur":<#if context.refGajur??>${context.refGajur?c}<#else>null</#if>
	}
	<#if context_has_next> , </#if>
	</#list>
]
}
	