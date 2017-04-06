<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://v3x.seeyon.com/taglib/core" prefix="v3x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://v3x.seeyon.com/bridges/spring-portlet-html"
	prefix="html"%>
<fmt:setBundle basename="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources" var="v3xCommonI18N" />
<fmt:setBundle basename="com.seeyon.v3x.main.resources.i18n.MainResources" var="v3xMainI18N"/>
<fmt:setBundle basename="com.seeyon.ctp.form.i18n.FormResources" />
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources" var="v3xDeeSectionI18N"/>
<fmt:message key="common.datetime.pattern" var="datetimePattern" bundle="${v3xCommonI18N}" />
<fmt:message key="common.date.pattern" var="datePattern" bundle="${v3xCommonI18N}" />
<fmt:message key='deeSection.source.createTime' var="deeFlowCreateTime" bundle='${v3xDeeSectionI18N}'/>

<%-- <html:link renderURL="/form.do" var="formURL" />
<html:link renderURL="/formquery.do" var="formqueryURL" />
<html:link renderURL="/formreport.do" var ="formreportURL"/>
<html:link renderURL="/bindForm.do" var="bindFormURL" />
<html:link renderURL="/menubind.do" var="menubindURL"/>
<html:link renderURL="/formenum.do" var="formenumURL" />
<html:link renderURL="/formserialNumber.do" var="formserialNumberURL" />
<html:link renderURL="/collaboration.do" var="detailURL" />
<html:link renderURL='/genericController.do' var="genericController" />
<html:link renderURL='/formappMgrController.do' var="formappMgrController" />
<html:link renderURL="/metadata.do" var="metadataMgrURL" />
<html:link renderURL='/templete.do' var="templeteURL" />
<html:link renderURL='/appFormController.do' var="appFormController" />
<html:link renderURL="/doc.do" var="docURL" />
<html:link renderURL="/form/triggerDesign.do" var="triggerURL" /> --%>
<html:link renderURL="/deeSectionController.do" var="deeSection"/>

<link rel="STYLESHEET" type="text/css" href="<c:url value="/apps_res/form/css/form.css${v3x:resSuffix()}" />">
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/default.css${v3x:resSuffix()}" />">
<link rel="STYLESHEET" type="text/css" href="<c:url value="/apps_res/form/css/tableselect.css${v3x:resSuffix()}" />">
<script type="text/javascript" src="<c:url value="/apps_res/form/css/tableselect.js${v3x:resSuffix()}" />"></script>
<link rel="stylesheet" type="text/css" href="<c:url value="/apps_res/collaboration/css/collaboration.css${v3x:resSuffix()}" />">
<script type="text/javascript" src="<c:url value="/common/js/menu/xmenu.js${v3x:resSuffix()}" />"></script>
${v3x:skin()}
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/V3X.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/collaboration/js/templete.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/form/js/appform/appform.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery-ui.custom.js${v3x:resSuffix()}" />"></script>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/jquery-ui.custom.css${v3x:resSuffix()}" />">
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.plugin.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript">
///form/triggerDesign.do
<!--
var v3x = new V3X();
v3x.init("${pageContext.request.contextPath}", "${v3x:getLanguage(pageContext.request)}");
v3x.loadLanguage("/cfgHome/i18nform/i18n");
_ = v3x.getMessage;
/* var formURL = "${formURL}";
var formqueryURL = "${formqueryURL}";
var formreportURL = "${formreportURL}";
var bindFormURL = "${bindFormURL}";
var menubindURL = "${menubindURL}";
var formenumURL = "${formenumURL}";
var formserialNumberURL = "${formserialNumberURL}";
var genericControllerURL = "${genericController}?ViewPage=";
var formappMgrController = "${formappMgrController}";
var metadataURL = "${metadataMgrURL}";
var templeteURL = "${templeteURL}";
var appFormController = "${appFormController}" ;
var docURL = "${docURL}";
var genericURL = '${detailURL}';
var triggerURL = "${triggerURL}"; */
//-->
</script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/xtree/xtree.js${v3x:resSuffix()}"/>"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/common/js/xtree/xtree.css${v3x:resSuffix()}" />">
