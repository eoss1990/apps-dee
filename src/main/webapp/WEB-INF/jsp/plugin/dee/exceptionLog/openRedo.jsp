<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<head>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>
</title>

</head>
<script type="text/javascript">
//弹出操作返回信息
/* var retMsg = "${retMsg}";
if(retMsg != ""){
	alert(retMsg);
  window.close();
} */
function OK() {
  // $("#submitData").submit();
  var form=document.getElementById("submitData");
	form.submit();
    return "ture";
   
}
</script>

<body>
	 <form action="${urlDeeSynchronLog}?method=exceptionDetaiOpeanUpdate" id="submitData" method="post" tagert="">
		<table width="40%" border="0" cellspacing="0" cellpadding="0"
			align="center">
			<tr>
			<%-- 	<td class="bg-gray" width="30%" nowrap="NOWRAP"><label
					for="name"><font color="red"></font><fmt:message key="dee.synchronLog.taskName.label"/>:</td> --%>
				<td class="new-column" width="70%" nowrap="nowrap"><input style="width:800px;"
					readonly="readonly" type="text" id="dis_name" name="dis_name"
					deaultValue="" inputName="" validate="notNull"
					class="cursor-hand input-80per" value="${flowRealName}" /></td>
			</tr>
			<tr>
				<%-- <td class="bg-gray" width="30%" nowrap="NOWRAP"><label
					for="name"><font color="red"></font><fmt:message key="dee.synchronLog.content.label"/>:</td> --%>
				<td class="new-column" width="70%" nowrap="nowrap"><textarea
						style="width:800px; height: 500px;" name="doc_code">${bean.doc_code}</textarea></td>
			</tr>
			<td colspan="2" align="center">
			<input type="hidden" name="redo_id" value="${bean.redo_id}">
			 <%-- <input type="submit" id="submitButton" name="submitButton"  value="<fmt:message key="dee.dataSource.save.label"/>"	class="button-default-2">&nbsp; --%> 
			<%--<input type="button" onclick="window.close();" value="<fmt:message key="dee.synchronLog.cancel.label"/>" class="button-default-2"> --%>
			</td>
			</tr>
		</table>
	</form> 

</body>
</html>


