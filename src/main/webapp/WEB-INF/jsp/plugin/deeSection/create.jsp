<%@ page language="java" contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Insert title here</title>
	<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
	<%@ taglib uri="http://v3x.seeyon.com/taglib/main" prefix="main"%>
	<%@ taglib uri="http://v3x.seeyon.com/bridges/spring-portlet-html" prefix="html"%>
	<fmt:setBundle basename="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources" var="v3xCommonI18N"/>
	<fmt:setBundle basename="com.seeyon.v3x.main.resources.i18n.MainResources" var="v3xMainI18N"/>
	<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources" var="v3xDeeSectionI18N"/>
	<fmt:setBundle basename="com.seeyon.v3x.system.resources.i18n.SysMgrResources" />
	<fmt:message key="common.datetime.pattern" var="datetimePattern" bundle="${v3xCommonI18N}"/>
	<fmt:message key="common.date.pattern" var="datePattern" bundle="${v3xCommonI18N}"/>
	<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/default.css${v3x:resSuffix()}" />">
	<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
	<link rel="stylesheet" type="text/css" href="<c:url value="/apps_res/systemmanager/css/css.css${v3x:resSuffix()}" />">
	<link rel="stylesheet" type="text/css" href="<c:url value="/common/js/xtree/xtree.css${v3x:resSuffix()}" />">
	<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/V3X.js${v3x:resSuffix()}" />"></script>
	${v3x:skin()}
	<script type="text/javascript">
		var v3x = new V3X();
		v3x.init("${pageContext.request.contextPath}", "${v3x:getLanguage(pageContext.request)}");
		v3x.loadLanguage("/apps_res/systemmanager/js/i18n");
		_ = v3x.getMessage;
		var deeSectionURL = "<html:link renderURL='/deeSectionController.do'/>";
	</script>
	<%-- <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/menu/xmenu.js${v3x:resSuffix()}" />"></script>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/xtree/xtree.js${v3x:resSuffix()}" />"></script>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/systemmanager/js/space.js${v3x:resSuffix()}" />"></script>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script> --%>
	<c:set value="${v3x:parseElements(sectionSecurities, 'entityId', 'entityType')}" var="selectPeopleStr" />
	<c:set value="${v3x:showOrgEntities(sectionSecurities, 'entityId', 'entityType', pageContext)}" var="authStr" />
	<script type="text/javascript"><!--

	function selectPeople(obj){
		var objValue = $(obj).next().val();
		var objTxt = $(obj).val();
		var panels = 'Account,Department,Team,Post,Level';
		var selectType = 'Member,Department,Account,Team,Post,Level';
		var thisAccount = false;
		var showMe = true;
		$.selectPeople({
			mode:'open',
			minSize:0,
			panels:panels,
			selectType:selectType,
			showFlowTypeRadio: false,
			showMe: showMe,
			onlyLoginAccount : thisAccount,
			params : {
				text : objTxt,
				value : objValue
			},
			callback : function(ret) {
				$(obj).next().val(ret.value);
				$(obj).val(ret.text);
			}
		});
	}

	function chooseSource(){
		/*
		 var returnval = v3x.openWindow({
		 url : deeSectionURL + "?method=getDataSourceFrame",
		 width : "800",
		 height : "550",
		 scrollbars : "false"
		 });*/
		//var returnval = window.showModalDialog(deeSectionURL + "?method=getDataSourceFrame","","dialogWidth:800px;dialogHeight:550px");
		var returnval;
		var dialog = $.dialog({
			targetWindow:window.top,
			isDrag:true,
			width:800,
			height:500,
			scrolling:false,
			url:"${path}/dee/deeTrigger.do?method=triggerDEETask&&taskType=data",
			title : "<fmt:message key='deeSection.dialog.title' bundle='${v3xDeeSectionI18N}' />",
			buttons : [ {
				text : "<fmt:message key='deeSection.button.sure' bundle='${v3xDeeSectionI18N}' />",
				id:"sure",
				handler : function() {
					var returnval = dialog.getReturnValue();
					if(returnval){
						document.getElementById("flowDisName").value = returnval.taskName;
						document.getElementById("flowId").value = returnval.taskId;
						var requestCaller = new XMLHttpRequestCaller(this, "ajaxDeeSectionManager", "getShowField", false);
						requestCaller.addParameter(1, "String", returnval.taskId);
						requestCaller.needCheckLogin = true;
						var result = requestCaller.serviceRequest();
						if(result){
							eval(result);
							var str = new StringBuffer();
							str.append("<table class=\"sort ellipsis\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
							str.append("<THEAD class=\"mxt-grid-thead\">");

							str.append("<tr class=\"sort\">");
							str.append("<td align=\"center\" width=\"20%\"><input type=\"checkbox\" id=\"selectAll\" name=\"selectAll\" checked=\"checked\" onclick=\"javascript:checkALL();\"/></td>");
							str.append("<td align=\"center\" type=\"String\" width=\"60%\"><fmt:message key="common.name.label" bundle="${v3xCommonI18N}" /></td>");
							str.append("<td align=\"center\" type=\"String\" width=\"20%\"><fmt:message key="common.sort.label" bundle="${v3xCommonI18N}" /></td>");
							str.append("</tr>");

							str.append("</THEAD>");

							str.append("<TBODY class=\"mxt-grid-tbody\">");

							for(var i=0; i<data.length;i++){
								var j = i+1;
								str.append("<tr class=\"sort erow\">");
								str.append("<td align=\"center\" class=\"sort\" width=\"20%\"><input type=\"checkbox\" checked=\"checked\" name=\"showFieldKey\"  meta=\""+data[i][2]+"\" value=\""+data[i][0]+"\"/></td>");
								str.append("<td align=\"center\" class=\"sort\" width=\"60%\">"+data[i][1]+"</td>");
								str.append("<td align=\"center\" class=\"sort\" width=\"20%\"><input type=\"text\" escapeXml=\"true\" maxlength=\"2\" size=\"4\" inputName=\"<fmt:message key="common.sort.label" bundle="${v3xCommonI18N}" />\"   validate=\"maxLength,isInteger,notNull\" name=\"sort_"+data[i][0]+"\" value=\""+j+"\"/></td>");
								str.append("</tr>");
							}


							//for(var key in data){
							//	str.append("<tr class=\"sort erow\">");
							//	str.append("<td align=\"center\" class=\"sort\" width=\"20%\"><input type=\"checkbox\" checked=\"checked\" name=\"showFieldKey\"  value=\""+key+"\"/></td>");
							//	str.append("<td align=\"center\" class=\"sort\" width=\"80%\">"+data[key]["displayName"]+"</td>");
							//	str.append("</tr>");
							//}
							str.append("</TBODY>");
							str.append("</table>");
							document.getElementById("showField").innerHTML=str.toString();
						}else{
							$.alert("<fmt:message key='deeSection.error.column.notNull' bundle='${v3xDeeSectionI18N}' />");
						}
						dialog.close();
					}
				}
			}, {
				text: "<fmt:message key='deeSection.button.exit' bundle='${v3xDeeSectionI18N}' />",
				id:"exit",
				handler : function() {
					dialog.close();
				}
			} ]
		});
	}
	function checkALL(){
		var checkValue = document.getElementById("selectAll").checked;
		var fields = document.getElementsByName("showFieldKey");
		for(var i=0; i<fields.length; i++){
			fields[i].checked = checkValue;
		}
	}

	function notNull(){

		if($("input[name='deeSectionName']").val()==""||$("input[name='deeSectionName']").val()==null)
		{
			$.alert("<fmt:message key='deeSection.error.portalName.notNull' bundle='${v3xDeeSectionI18N}' />");
			return false;
		}

		if($("input[name='deeSectionName']").val().length>120)
		{
			$.alert("<fmt:message key='deeSection.error.portalName.long' bundle='${v3xDeeSectionI18N}' />");
			return false;
		}

		if($("#flowDisName").val()==""||$("#flowDisName").val()==null)
		{
			$.alert("<fmt:message key='deeSection.error.dataSource.notNull' bundle='${v3xDeeSectionI18N}' />");
			return false;
		}

		var flag = 0;
		$("input[name^='sort_']",$("#showField")).each(function () {
			if($(this).val()==""||$(this).val()==null){
				flag =1;
			}
		});
		if(flag == 1)
		{
			$.alert("<fmt:message key='deeSection.error.sort.notNull' bundle='${v3xDeeSectionI18N}' />");
			return false;
		}
		return true;
	}

	function checkSectionName(){
		var deeSectionName = document.getElementsByName("deeSectionName")[0].value;
		var id = document.getElementsByName("id")[0].value;
		var requestCaller = new XMLHttpRequestCaller(this, "ajaxDeeSectionManager", "hasCurrentSectionName", false);
		requestCaller.addParameter(1, "String", deeSectionName);
		requestCaller.addParameter(2, "String", id);
		requestCaller.needCheckLogin = true;
		var result = requestCaller.serviceRequest();
		if(result){
			if(result == 'true'){
				$.alert("<fmt:message key='deeSection.panel.sameName' bundle='${v3xDeeSectionI18N}' />");
				return false;
			}else if(result == 'false'){
				return true;
			}
		}
	}
	function init(){
		document.body.style.overflow='auto';
	}

	function portalStyleChange()
	{
		if($("#portalStyle").val() == "1")
		{
			$("#deeChart").show();
		}
		else
		{
			$("#deeChart").hide();
		}
	}

	function checkCheckBox()
	{
		var flag = 0;
		$("input[name='showFieldKey']:checkbox").each(function () {
			if ($(this).attr("checked")) {
				flag =1;
			}
		});
		if(flag == 0)
		{
			$.alert("<fmt:message key='deeSection.error.checkbox.notNull' bundle='${v3xDeeSectionI18N}' />");
			return false;
		}
		return true;
	}

	function checkChartMeta()
	{
		if(document.getElementById("portalStyle").value=="1")
		{
			var fields = document.getElementsByName("showFieldKey");
			for(var i=0; i<fields.length; i++){
				if(fields[i].checked)
				{
					if(fields[i].attributes["meta"].value != "decimal")
					{
						$.alert("<fmt:message key='deeSection.error.meta.notDecimal' bundle='${v3xDeeSectionI18N}' />");
						return false;
					}

				}
			}
		}
		return true;
	}
	--></script>
</head>
<body onload="init()" style="overflow-x: hidden">
<fmt:message key="deeSection.source.pageHeight" bundle="${v3xDeeSectionI18N}" var="pageHeightLabel"/>
<c:set value="${param.type=='view'? 'disabled':''}" var="isDisableStr"/>
<c:set value="${param.id==null?'create':(param.type=='view'?'view':'edit')}" var="operationType"/>
<form id="deePortalForm" method="post" action="<c:url value="${deeSectionURL}?method=save" />" onsubmit="return notNull()&&checkSectionName()&&checkChartMeta()&&checkCheckBox()">
	<input name="id" type="hidden" value="${deeSection.id}">
	<table border="0" cellpadding="0" cellspacing="0" width="100%" height="98%" align="center" class="page-list-border">
		<tr>
			<td align="center" height="100%">
				<div class="categorySet-body" style="padding-top:0;padding-bottom:0;">
					<%-- 栏目定义 --%>
					<fieldset style="width: 600px;margin-top:5px;">
						<legend><fmt:message key="space.section.definition.label" /></legend>
						<table width="500" border="0" cellspacing="0" cellpadding="0" align="center">
							<tr>
								<td class="bg-gray" width="30%" nowrap><font color="red">*</font><fmt:message key="common.name.label" bundle="${v3xCommonI18N}"/>:</td>
								<td class="new-column" width="70%">
									<fmt:message key="common.default.name.value" var="defName" bundle="${v3xCommonI18N}" />
									<input name="deeSectionName" type="text" id="name" class="input-300px" value="<c:out value="${deeSection.deeSectionName}"/>" ${isDisableStr}>
									<input type="hidden" name="state" value="0">
								</td>
							</tr>
							<fmt:message key="common.default.selectPeople.value" var="defaultSP" bundle="${v3xCommonI18N}"/>

							<tr>
								<td class="bg-gray" nowrap><fmt:message key="space.section.auth.label" />:</td>
								<td class="new-column" nowrap>
									<input name='aa' id="authName"
										   readonly class="cursor-hand input-300px" onclick="selectPeople(this)" value="${authStr}" >
									<input type="hidden" value="${selectPeopleStr}" name="selectPeopleStr" id="selectPeopleStr">
								</td>
							</tr>
							<tr>
								<td colspan="2" class="new-column description-lable" style="padding-top: 12px;">
									<fmt:message key="space.section.auth.description" />
								</td>
							</tr>
						</table>
					</fieldset>
					<fieldset style="width: 600px;margin-top:5px;margin-bottom:5px;">
						<%-- 栏目参数 --%>
						<legend><fmt:message key="space.section.param.label" /></legend>
						<table width="500" border="0" cellspacing="0" cellpadding="0" align="center" >
							<tr>
								<td class="bg-gray" nowrap width="30%">
									<fmt:message key="deeSection.source.label" bundle="${v3xDeeSectionI18N}"/>:
								</td>
								<td class="new-column">
									<fmt:message key='deeSection.source.inputName' bundle='${v3xDeeSectionI18N}' var="sourceDef"/>
									<input type="text" name="flowDisName" id="flowDisName" class="input-300px" readonly value="<c:out value='${deeSection.flowDisName}'/>" ${isDisableStr} onclick="javaScript:chooseSource();" />
									<input type="hidden" name="flowId" id="flowId" value="${deeSection.flowId}"/>
								</td>
							</tr>
							<tr>
								<td class="bg-gray" nowrap width="30%">
									<fmt:message key="deeSection.create.portalStyle" bundle="${v3xDeeSectionI18N}"/>:
								</td>
								<td class="new-column">
									<select id="portalStyle" name="portalStyle" class="input-300px" onchange="portalStyleChange();" ${isDisableStr}>
										<option value="0" ${deeSection.portalStyle=='0'?'selected':''}><fmt:message key="deeSection.create.portalStyle.list" bundle="${v3xDeeSectionI18N}"/></option>
										<option value="1" ${deeSection.portalStyle=='1'?'selected':''}><fmt:message key="deeSection.create.portalStyle.listChart" bundle="${v3xDeeSectionI18N}"/></option>
									</select>
								</td>
							</tr>
							<tr id="deeChart" ${deeSection.portalStyle=='1'?"":"style='display:none;'"}>
								<td class="bg-gray" nowrap width="30%">
									<fmt:message key="deeSection.create.chartStyle" bundle="${v3xDeeSectionI18N}"/>:
								</td>
								<td class="new-column">
									<select id="chartStyle" name="chartStyle" class="input-300px" ${isDisableStr}>
										<option value="0" ${deeSection.chartStyle=='0'?'selected':''}><fmt:message key="deeSection.create.chartStyle.line" bundle="${v3xDeeSectionI18N}"/></option>
										<option value="1" ${deeSection.chartStyle=='1'?'selected':''}><fmt:message key="deeSection.create.chartStyle.column" bundle="${v3xDeeSectionI18N}"/></option>
									</select>
								</td>
							</tr>
							<tr>
								<td class="bg-gray" nowrap><fmt:message key="deeSection.source.showField" bundle="${v3xDeeSectionI18N}" />:</td>
								<td class="new-column">
									<div id="showField">
										<table class="sort ellipsis" width="100%" cellpadding="0" cellspacing="0" dragable="false">
											<THEAD class="mxt-grid-thead">
											<tr class="sort">
												<td align="center" width="20%"><input type="checkbox" id="selectAll" name="selectAll" onclick="javascript:checkALL();" ${isDisableStr}/></td>
												<td align="center" type="String" width="60%"><fmt:message key="common.name.label" bundle="${v3xCommonI18N}" /></td>
												<td align="center" type="String" width="20%"><fmt:message key="common.sort.label" bundle="${v3xCommonI18N}" /></td>
											</tr>
											</THEAD>
											<TBODY class="mxt-grid-tbody">
											<c:forEach items="${deeSectionProps}" var="prop">
												<tr class="sort erow">
													<c:set value="${prop.isShow==0?'checked':''}" var="checkedStr"></c:set>
													<td align=center class="sort" width="20%"><input type="checkbox" ${checkedStr} name="showFieldKey" value="${prop.propName}" meta="${prop.propMeta}" ${isDisableStr}/></td>
													<td align=center class="sort" width="60%">${prop.propValue}</td>
													<td align=center class="sort" width="20%"><input type="text" name="sort_${prop.propName}" inputName="<fmt:message key="common.sort.label" bundle="${v3xCommonI18N}" />"  validate="maxLength,isInteger,notNull" ${isDisableStr} escapeXml="true" maxlength="2" size="4" value="${prop.sort}"></td>
												</tr>
											</c:forEach>
											</TBODY>
										</table>
									</div>
								</td>
							</tr>
						</table>
					</fieldset>
				</div>
			</td>
		</tr>
		<tr>
			<td height="30" align="center" class="bg-advance-bottom">
				<input type="submit" value="<fmt:message key='common.button.ok.label' bundle="${v3xCommonI18N}" />" class="common_button common_button_emphasize margin_r_10" ${isDisableStr}>&nbsp;
				<input type="button" onclick="parent.location.reload()" value="<fmt:message key='common.button.cancel.label' bundle="${v3xCommonI18N}" />" class="common_button common_button_gray">
			</td>
		</tr>
	</table>
</form>
</body>
</html>