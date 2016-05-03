{"files": [
	{
	<#if errormessage?? >
	"error" : "${errormessage}",
	</#if>
	"name" : "${upload.name}",
	"size": "${upload.size}",
	"url": "http:\/\/example.org\/files\/picture1.jpg",
	"deleteUrl": "http:\/\/example.org\/files\/picture1.jpg",
	"deleteType": "DELETE"
	}
]}