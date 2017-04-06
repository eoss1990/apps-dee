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
    <title></title>
<script type="text/javascript">
getA8Top().showMoreSectionLocation("${sectionName}");
var listTalbe;
$(document).ready(function () {
    new MxtLayout({
        'id': 'layout',
        'northArea': {
            'id': 'north',
            'height': 30,
            'sprit': false,
            'border': false
        },
        'centerArea': {
            'id': 'center',
            'border': false,
            'minHeight': 20
        }
    });

    var topSearchSize = 2;
    if ($.browser.msie && $.browser.version == '6.0') {
        topSearchSize = 5;
    }
    var searchobj = $.searchCondition({
        top:topSearchSize,
        right:10,
        searchHandler: function(){
            var o = new Object();
            var choose = $('#' + searchobj.p.id).find("option:selected").val();
            o.deeSearchKey = choose;
            o.deeSearchValue = $("#"+choose).val();
            o.sectionDefineId = $("#sectionDefineId").val();
            o.entityId = $("#entityId").val();
            o.ordinal = $("#ordinal").val();
            var val = searchobj.g.getReturnValue();
            if (val !== null) {
                $("#listDeeSectionMore").ajaxgridLoad(o);
            }
        },
        conditions: ${searchCd}
    }); 

     listTable = $("#listDeeSectionMore").ajaxgrid({
        colModel: ${fieldCd},
        managerName: "deeSectionManager",
        managerMethod: "deeSectionMoreList",
        parentId: $('.layout_center').eq(0).attr('id')
    });
     var o = new Object();
     o.sectionDefineId = $("#sectionDefineId").val();
     o.entityId = $("#entityId").val();
     o.ordinal = $("#ordinal").val();
    $("#listDeeSectionMore").ajaxgridLoad(o);
});
</script>

</head>
<body>
<input type="hidden" name="sectionDefineId" id="sectionDefineId" value="${sectionDefineId}" />
<input type="hidden" name="entityId" id="entityId" value="${entityId}" />
<input type="hidden" name="ordinal" id="ordinal" value="${ordinal}" />
<%-- 	<div id="nowLocation" class="layout_location" style="display: block;">
		<span class="nowLocation_ico"><img src="/seeyon/main/skin/frame/harmony/menuIcon/moresectionicon.png"></span>
		<span class="nowLocation_content"><a style="color:#888;">${sectionName}</a></span>
	</div> --%>
<div id='layout'>
	<div class="layout_north bg_color" id="north"></div>
    <div class="layout_center over_hidden" id="center">
        <table  class="flexme3 " id="listDeeSectionMore"></table>
    </div>
</div>
</body>
</html>