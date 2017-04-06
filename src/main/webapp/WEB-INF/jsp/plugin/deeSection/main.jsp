<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<html class="w100b h100b">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources"/>
<title></title>
<script type="text/javascript">
$(document).ready(function () {
	var msg = "${reMsg}";
	if(msg=="true")
		{
			$.messageBox({
			    'type' : 0,
			    'imgType':0,
			    'msg' : "<fmt:message key='deeSection.operation.success'/>"
			  });
		}
	else if(msg=="false")
	{	
		$.messageBox({
		    'type' : 0,
		    'imgType':1,
		    'msg' : "<fmt:message key='deeSection.operation.failed'/>"
		  });
	}
});
</script>
</head>
<body class="w100b h100b">
<iframe width="100%" height="100%" border=0 frameborder=0 src="<c:url value="/deeSectionController.do?method=list" />" name="listFrame" scrolling="no" id="listFrame"/></iframe>
</body>
</html>

