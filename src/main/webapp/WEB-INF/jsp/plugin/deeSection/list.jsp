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
    <title>交换引擎栏目</title>
<script type="text/javascript">
var menus=[
           {name:"<fmt:message key='deeSection.inforIntegration'/>"},
           {name:"<fmt:message key='deeSection.name'/>", url:"/deeSectionController.do?method=main"}
       ];
updateCurrentLocation(menus);

var listTalbe;
$(document).ready(function () {
    new MxtLayout({
        'id': 'layout',
        'northArea': {
            'id': 'north',
            'height': 40,
            'sprit': false,
            'border': false
        },
        'centerArea': {
            'id': 'center',
            'border': false,
            'minHeight': 20
        }
    });

    $("#toolbars").toolbar({
        toolbar: [
        {
     	      id: "add",
     	      name: "${ctp:i18n('common.toolbar.new.label')}",
     	      className: "ico16",
     	      click: function() {
     	    	  add();
     	       	}
     	},{
             id: "modify",
             name: "${ctp:i18n('link.jsp.modify')}",
             className: "ico16 modify_text_16",
             click: function () {
                 modify();
             }
         },{
     		id: "delete",
             name: "${ctp:i18n('common.button.delete.label')}",
             className: "ico16 del_16",
             click: function() {
            	 deleteSection();
 	       	}
         }
    ]
    });

    var topSearchSize = 7;
    if($.browser.msie && $.browser.version=='6.0'){
        topSearchSize = 5;
    }
    var searchobj = $.searchCondition({
        top:topSearchSize,
        right:10,
        searchHandler: function(){
            var o = new Object();
            var choose = $('#' + searchobj.p.id).find("option:selected").val();
            if (choose === 'deeSectionName') {
                o.deeSectionName = $('#deeSectionName').val();
            }
            var val = searchobj.g.getReturnValue();
            if (val !== null) {
                $("#listDeePortalTable").ajaxgridLoad(o);
            }
        },
        conditions: [{
            id: 'deeSectionName',
            name: 'deeSectionName',
            type: 'input',
            text: "<fmt:message key='deeSection.portalName'/>",
            value: 'deeSectionName'
        }]
    }); 

     listTable = $("#listDeePortalTable").ajaxgrid({
        colModel: [
            {
                display: 'id',
                name: 'id',
                width: '5%',
                sortable: false,
                align: 'center',
                type: 'checkbox',
                isToggleHideShow: false
            },
            {
                display: "<fmt:message key='deeSection.portalName'/>",        // 栏目名称
                name: 'deeSectionName',
                width: '20%'
            },
            {
                display: "<fmt:message key='deeSection.authorization'/>",    // 授权
                name: 'org',
                width: '30%'
            },
            {
                display: "<fmt:message key='deeSection.source.label'/>",      // 交换数据来源
                name: 'flowDisName',
                width: '45%'
            }
        ],
        click: clk,
        dblclick: dblclk,
        //render: rend,
        managerName: "deeSectionManager",
        managerMethod: "deeSectionList",
        parentId: $('.layout_center').eq(0).attr('id'),
        height: 200,
        isHaveIframe:true,
        slideToggleBtn:true,
        vChange: true,
        vChangeParam: {
            overflow: "hidden",
            autoResize:true
        }
    });
    var o = {};
    $("#listDeePortalTable").ajaxgridLoad(o);   
});

function clk(data, r, c) {
    $("#grid_detail").resetValidate();
    var form = document.getElementById("listForm");
    form.action= _ctxPath + "/deeSectionController.do?method=create&id=" + data.id + "&type=view";
    form.target = "deePortalDetail";
    form.submit();
}

function dblclk(data, r, c) {
    var listForm = document.getElementById("listForm");
    listForm.action = _ctxPath + "/deeSectionController.do?method=create&id=" + data.id;
    listForm.target = "deePortalDetail";
    listForm.submit();
}

function rend(txt, data, r, c) {
    if (c == 2) {
/*         if(data.enable+"" == "true"){
            return "<fmt:message key='dee.schedule.start.label'/>";
        } else if(data.enable+"" == "false"){
            return "<fmt:message key='dee.schedule.stop.label'/>";
        } */
    }
    return txt;
}

function modify(){
    var hasChecked = $("input:checked", $("#listDeePortalTable"));;
    if(hasChecked.length == 0){
        $.alert("<fmt:message key='dee.resend.error.label'/>");
        return;
    }
    if(hasChecked.length > 1){
        $.alert("<fmt:message key='dee.dataSource.updateError.label'/>");
        return;
    }
    var id = "";
    for(var i = 0; i < hasChecked.length; i++){
        id = hasChecked[i].value;
    }
    var listForm = document.getElementById("listForm");
    listForm.action = _ctxPath + "/deeSectionController.do?method=create&id=" +id;
    listForm.target = "deePortalDetail";
    listForm.submit();
    listTable.grid.resizeGridUpDown('middle');
}

function add(){
	var listForm = document.getElementById("listForm");
    listForm.action = _ctxPath + "/deeSectionController.do?method=create";
    listForm.target = "deePortalDetail";
    listForm.submit();
    listTable.grid.resizeGridUpDown('middle');
}

function deleteSection(){
	var hasChecked = $("input:checked", $("#listDeePortalTable"));
	if(hasChecked.length>0)
		{
/* 			if(confirm("是否确定删除？")){
				var id = "";
			    for(var i = 0; i < hasChecked.length; i++){
			    	if(i>0)
			    		{
			    			id+=",";
			    		}
			        id += hasChecked[i].value;
			    }
			    var listForm = document.getElementById("listForm");
			    listForm.action = _ctxPath + "/deeSectionController.do?method=delete&ids="+id;
			    listForm.submit();
			} */
			
			var confirm = $.confirm({
			    'msg': "<fmt:message key='deeSection.infor.delete'/>",
			    ok_fn: function () { 
			    	var id = "";
				    for(var i = 0; i < hasChecked.length; i++){
				    	if(i>0)
				    		{
				    			id+=",";
				    		}
				        id += hasChecked[i].value;
				    }
				    var listForm = document.getElementById("listForm");
				    listForm.action = _ctxPath + "/deeSectionController.do?method=delete&ids="+id;
				    listForm.submit();
			    },
				cancel_fn:function(){}
			});
		}
	else{
        $.alert("<fmt:message key='dee.resend.error.label'/>");
        return;
    }
}
	
</script>

</head>
<body>

<div id='layout'>
    <div class="layout_north bg_color" id="north">
        <div id="toolbars"></div>
    </div>
    <div class="layout_center over_hidden" id="center">
        <table  class="flexme3 " id="listDeePortalTable"></table>
        <div id="grid_detail" class="h100b">
            <iframe id="deePortalDetail" name="deePortalDetail" width="100%" height="100%" frameborder="0" class='calendar_show_iframe' style="overflow-y:hidden"></iframe>
        </div>
    </div>
</div>
<form name="listForm" id="listForm" method="post" onsubmit="return false">
</form>
</body>
</html>