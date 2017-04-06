<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<head>
<%@ include file="dataSourceHeader.jsp"%>
</head>
<iframe width="100%" height="100%" border=0 frameborder=0 src="${urlDeeDataSource}?method=showDataSourceList" name="listFrame" scrolling="no" id="listFrame"/>
<noframes><body scroll='no'>
</body>
</noframes>
</html>