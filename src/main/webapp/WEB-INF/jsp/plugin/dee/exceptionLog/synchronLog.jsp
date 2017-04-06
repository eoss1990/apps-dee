<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>ncBusinessLog</title>
<%@include file="synchronLogHeader.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">

<script type="text/javascript">
        /** 所在位置 */
        var html = "<span class='margin_r_10'>${ctp:i18n('seeyon.top.nowLocation.label')}</span>";
        var items = [];
        items.push("<span class='color_gray2_nohover'><fmt:message key='dee.pluginMainMenu.label'/></span>");
        items.push("<a class=\"hand\" onclick=\"showMenu('${path}/deeSynchronLogController.do?method=synchronLogFrame');\"><fmt:message key='dee.deeLog.label' /></a>");
        html += items.join('<span class="common_crumbs_next margin_lr_5">-</span>');
        getA8Top().showLocation(html);
		//getA8Top().showLocation(null, "<fmt:message key='dee.pluginMainMenu.label'/>", "<fmt:message key='dee.deeLog.label' />");
	   function showRedoList(syncId,flowName){
			var form = document.getElementById("listForm");
			form.action="${urlDeeSynchronLog}?method=showDeeExceptionDetail&syncId="+syncId+"&flowName="+flowName;
			form.target = "detailFrame";
			form.submit();
	   }
	   
	   function deleteSycn(){
		   var hasChecked = $("input[name='id']:checked");
		   if(hasChecked.length == 0){
			   alert("<fmt:message key='dee.resend.error.label'/>");
			   return;
		   }
		   if (!confirm("<fmt:message key='dee.delMsg.label'/>"))  {
			   return false;
		   }
		   var listForm = document.getElementById("listForm");
    	   listForm.action = "${urlDeeSynchronLog}?method=synchronLogDelete";
    	   listForm.target = "listFrame";
		   listForm.submit();
		}	
</script>
</head>
<body scroll="no">
<div class="main_div_row2">
  <div class="right_div_row2">
    <div class="top_div_row2">

				<table height="100%" width="100%" border="0" cellspacing="0"
					cellpadding="0">
					<tr>
						<td height="22" class="webfx-menu-bar border-top">
							<script type="text/javascript">
								var myBar = new WebFXMenuBar("${pageContext.request.contextPath}", "");
								myBar.add(new WebFXMenuButton("update","<fmt:message key='dee.dataSource.delete.label'/>","deleteSycn()", [ 1, 2 ], "", null));
								document.write(myBar);
								document.close();
							</script>
						</td>
					</tr>
				</table>

			</div>
    <div class="center_div_row2" id="scrollListDiv">
      <form name="listForm" id="listForm" method="post">
        <v3x:table htmlId="ncBusinessLog" data="resultFlowBean" var="synchronLog" className="sort ellipsis" isChangeTRColor="false">
        	<v3x:column width="4%" align="center"
							label="<input type='checkbox' id='allCheckbox' onclick='selectAll(this, \"id\")'/>">
							<input type="checkbox" name="id" flowId="${synchronLog.sync_id}" value="${synchronLog.sync_id}">
			</v3x:column>
        	<c:choose>
        		<c:when test="${synchronLog.sync_state != 1}">
        			<v3x:column width="36%" align="left" type="String" label="dee.taskName.label" className="cursor-hand sort like-a" value="${synchronLog.flow_dis_name}" alt="${synchronLog.flow_dis_name}" onClick="showRedoList('${synchronLog.sync_id}','${synchronLog.flow_dis_name}')"/>
        		</c:when>
        		<c:otherwise>
        			<v3x:column width="36%" align="left" type="String" label="dee.taskName.label" className="cursor-hand sort" value="${synchronLog.flow_dis_name}" alt="${synchronLog.flow_dis_name}"/>
        		</c:otherwise>
        	</c:choose>
			<v3x:column width="40%" align="center" type="String" label="dee.time.label" className="sort" value="${synchronLog.sync_time}"/>
			<v3x:column width="19%" align="center" type="String" label="dee.status.label">
		 		<c:if test="${synchronLog.sync_state == 0}">
		 			<fmt:message key="dee.status.fail.label"/>
		 		</c:if>
		 		<c:if test="${synchronLog.sync_state == 1}">
		 			<fmt:message key="dee.status.success.label"/>
		 		</c:if>
		 		<c:if test="${synchronLog.sync_state == 2}">
		 			<fmt:message key="dee.status.partFail.label"/>
		 		</c:if>
		 </v3x:column>
        </v3x:table>
      </form>
    </div>
  </div>
</div>
</body>
<script type="text/javascript">
	showDetailPageBaseInfo("detailFrame","<fmt:message key='dee.pluginChildrenMenu.synchronousLog.label'/>", [1,3], pageQueryMap.get('count'), "");
</script>
</html>