<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/jsp/plugin/deeSection/selectTask/formHeader.jsp"%>
<title></title>
<script type="text/javascript">
var templateId = "${param.templateId }";
var taskType = "${param.taskType}";

function sureSubmit(){
	var result = new Object();
	result.formId="-1";
	result.templateName="";
	result.templateId="-1";
	var ids = window.templeteListFrame.document.getElementsByName("id");
	for(var i=0;i<ids.length;i++){
		var idDom = ids[i];
		if(idDom.checked){
			result.formId=idDom.formappId;
			result.templateName=idDom.contentName;
			result.templateId=idDom.value;
		}
	}
	return result;
}

function showList(typeId,name){
	var flowName = encodeURI(encodeURI(name));
	taskListFrame.location.href="${deeSection}?method=taskList&taskType="+encodeURI(taskType)+"&type_id="+typeId+(name?("&flowName="+flowName):"");
}
</script>
</head>
	  <frameset cols="22%,*" id="treeFrameset" framespacing="2" frameborder="yes" border="0" bordercolor="#ececec">
	    <frame src="${deeSection}?method=taskTree" name="templeteTreeFrame" frameborder="0" scrolling="no" id="templeteTreeFrame" />
		<frame src="" name="taskListFrame" frameborder="0" scrolling="yes" id="taskListFrame" />
	  </frameset>
</html>