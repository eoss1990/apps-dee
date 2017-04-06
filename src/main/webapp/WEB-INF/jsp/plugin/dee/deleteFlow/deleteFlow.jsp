<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@ include file="/WEB-INF/jsp/plugin/dee/common/common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources"/>
<fmt:setBundle basename="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources" var="v3xCommonI18N"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>DEE任务绑定</title>
    <script type="text/javascript" src="${path}/ajax.do?managerName=deeDeleteManager"></script>
</head>
<body>
<div id='layout' class="comp" comp="type:'layout'">
    <div class="layout_west" layout="width:200,minWidth:50,maxWidth:300">
        <div id="deeTree"></div>
    </div>
    <div class="layout_center" layout="border:false">
        <div id="toolbars"></div>
        <table id="deeTable" class="flexme3" style="display: none;"></table>
    </div>
</div>
<%@ include file="deleteFlow.js.jsp" %>
</body>
</html>