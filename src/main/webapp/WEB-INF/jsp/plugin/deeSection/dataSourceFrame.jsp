<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="header.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<frameset id="treeandlist" rows="*" cols="20%,*" framespacing="5" frameborder="yes"  bordercolor="#ececec">
	<frame frameborder="0" src="${deeSectionURL}?method=selectDataSource" name="treeFrame" scrolling="no" id="treeFrame"/>
    <frame src="${deeSectionURL}?method=getFlowList" id="listFrame" name="listFrame" frameborder="no" scrolling="auto" style="width: 100%; height: 100%;" />
</frameset>
<noframes>
<body>
</body>
</noframes>

</html>