<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<title></title>
<script type="text/javascript">
/* function OK(){
	if(document.all.returnId.value ==""){
		alert("${ctp:i18n('form.base.relationProject.chooseItem')}");
		return "";
	}
	var returnVal = new Object();
	returnVal.taskId = $("#returnId").val();
	returnVal.taskName = $("#returnName").val();
	//$("#taskId",parent.currentTr).val($("#returnId").val());
	//$("#taskName",parent.currentTr).val($("#returnName").val());
	return returnVal;
}
function sure(){
	if(document.all.returnId.value ==""){
		alert("${ctp:i18n('form.base.relationProject.chooseItem')}");
		return "";
	}
	var returnVal = new Object();
	returnVal.taskId = $("#returnId").val();
	returnVal.taskName = $("#returnName").val();
	window.returnVal = returnVal;
	return returnVal;
} */
//dee栏目调用时返回showModalDialog数据
function surePortal(){
	if(document.all.returnId.value ==""){
		alert("${ctp:i18n('form.base.relationProject.chooseItem')}");
		return "";
	}
	var returnVal = new Object();
	returnVal.taskId = $("#returnId").val();
	returnVal.taskName = $("#returnName").val();
	window.returnValue = returnVal;
	window.close();
}
function check(){
	content.showList(document.all.type_id.value,document.all.taskName.value);
}
window.onload = function() {
	var reObj = window.dialogArguments;
	//document.all.returnId.value=reObj.taskId;
	//document.all.returnName.value=reObj.name;
} 

</script>
</head>
<body style="margin-right: 0px;padding-right: 0px">
<table border="0" style="height: 100%;width:97%">
<tr height="80">
	<td align="center"">
		<font color="blue" size="5">${ctp:i18n('form.trigger.triggerSet.taskList.label')}</font>
	</td>
	<td align="right" style="padding-bottom: 0px;margin-bottom: 0px;">
		${ctp:i18n('form.trigger.triggerSet.taskName.label')}：
		<input type="hidden" name="type_id" id="type_id" value="-1">
		<input name="taskName" id="taskName" type="text" onkeyup="if(event.keyCode==13){check();}">
		<input onclick="check();" type="button" value="${ctp:i18n('form.trigger.triggerSet.inquiry.label')}">
	</td>
</tr>
<tr><td colspan="2" style="border:1px solid #d0d0d0;">
<iframe name="content" style="width:100%;height:100%" src="${path}/genericController.do?ViewPage=plugin/deeSection/selectTask/taskListFrame&taskType=${taskType}"></iframe>
</td></tr>
 <c:if test="${taskType=='Portal栏目'}">
<tr height="30"><td colspan="2"  align="right"><input onclick="surePortal();" class="button-style" type="button" value="${ctp:i18n('form.trigger.triggerSet.confirm.label')}">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input class="button-style" type="button" value="${ctp:i18n('form.trigger.triggerSet.cancel.label')}" onclick="window.close()"></td></tr>
</c:if>
</table>
<input type="hidden" id="returnId" name="returnId" value="">
<input type="hidden" id="returnName" name="returnName" value="">
</body>
</html>