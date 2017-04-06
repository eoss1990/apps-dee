<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://v3x.seeyon.com/taglib/core" prefix="v3x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://v3x.seeyon.com/bridges/spring-portlet-html" prefix="html"%>
<%@ taglib uri="http://www.seeyon.com/ctp" prefix="ctp"%>

<fmt:setBundle basename="com.seeyon.v3x.plugin.dee.resources.i18n.DeeResources" var="v3xMainI18N"/>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/default.css" />">
<link rel="STYLESHEET" type="text/css" href="<c:url value="/apps_res/form/css/form.css" />">    
<script type="text/javascript" src="<c:url value="/common/js/V3X.js" />"></script>
<script type="text/javascript" src="<c:url value="/common/js/prototype.js" />"></script>
<script type="text/javascript" src="<c:url value="/common/js/menu/xmenu.js" />"></script>
<script type="text/javascript" src="<c:url value="/apps_res/plugin/dee/dee.js" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script>
<link href="<c:url value="${v3x:getSkin()}/skin.css" />" type="text/css" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="<c:url value="/apps_res/hr/css/default.css" />">
<c:set var="path" value="${pageContext.request.contextPath}" />
<script type="text/javascript">
var v3x = new V3X();
v3x.init("${pageContext.request.contextPath}", "${v3x:getLanguage(pageContext.request)}");
_ = v3x.getMessage;
</script>
<html:link renderURL="/deeSynchronLogController.do" var="urlDeeSynchronLog" /> 
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources"/>
<fmt:setBundle basename="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources" var="v3xCommonI18N"/>