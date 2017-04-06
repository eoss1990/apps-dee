<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<%@ include file="/WEB-INF/jsp/plugin/dee/common/common.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle
	basename="com.seeyon.apps.dee.resources.i18n.DeeResources" />
<fmt:setBundle
	basename="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources"
	var="v3xCommonI18N" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>任务监控</title>
<script type="text/javascript" src="${path}/ajax.do?managerName=deeSynchronLogManager"></script>
<script type="text/javascript">
    var menus = [ {
        name : "<fmt:message key='dee.pluginMainMenu.label'/>"
    }, {
        name : "${ctp:i18n('system.menuname.redo')}",
        url : "/deeSynchronLogController.do?method=synchronLogFrame"
    } ];
    var gridObj;

    $(document).ready(function() {
        new MxtLayout({
            'id' : 'layout',
            'northArea' : {
                'id' : 'north',
                'height' : 40,
                'sprit' : false,
                'border' : false
            },
            'centerArea' : {
                'id' : 'center',
                'border' : false,
                'minHeight' : 20
            }
        });
        updateCurrentLocation(menus);
        initToolbar();
        initSearchObj();
        initGrid();
        
    });

    /**
     * 初始化工具栏组件
     */
    function initToolbar() {
        $("#toolbars").toolbar({
            toolbar : [ {
                id : "delete",
                name : "<fmt:message key='dee.dataSource.delete.label'/>",
                className : "ico16 modify_text_16",
                click : function() {
                    deleteSycn();
                }
            },{
            	id:"resend",
                name:"<fmt:message key='dee.resend.label'/>",
                className: "ico16 send_sms_16",
                click: function (){
                    resendSycn();
                }
            } ]
        });
    }
    
    /**
     * 初始化搜索框
     */
    function initSearchObj() {
        var topSearchSize = 7;
        if ($.browser.msie && $.browser.version == '6.0') {
            topSearchSize = 5;
        }
        var searchObj = $.searchCondition({
            top: topSearchSize,
            right: 10,
            searchHandler: function () {
                var choose = $('#' + searchObj.p.id).find("option:selected").val();
                wholeObj = searchObj;
                var so = searchObj.g.getReturnValue();
    			var o = {};
    			if(choose == "name"){
    				o.name = so.value;
    			}else if(choose == "state"){
    				o.state = so.value;
    			}else if(choose == "time"){
    				o.time = so.value;
    			}
    			var $synchronLog = $("#synchronLog");
    			$synchronLog.ajaxgridLoad(o);
            },
            conditions: [{
                id: 'name',
                name: 'name',
                type: 'input',
                text: '<fmt:message key='dee.taskName.label'/>',
                value: 'name'
            },{
                id: 'state',
                name: 'state',
                type: 'select',
                text: '<fmt:message key='dee.schedule.enable.label'/>',
                value: 'state',
                items: [{
    	            text: '<fmt:message key='dee.state.success'/>',
    	            value: '1'
    	        }, {
    	            text: '<fmt:message key='dee.state.failure'/>',
    	            value: '0'
    	        }]
            },{
                id: 'time',
                name: 'time',
                type: 'datemulti',
                text: '<fmt:message key='dee.time.label'/>',
                value: 'time'
            }]
        });
    }

    function deleteSycn() {
      debugger;
        var rows = gridObj.grid.getSelectRows();
        if (rows.length < 1) {
            var alertObject =$.alert("<fmt:message key='dee.resend.error.label'/>");
            return;
        }
        var syncid="";
        for (var i = 0; i < rows.length; i++) {
            syncid += rows[i].sync_id+",";
            
        }
        var flowid="";
        for (var i = 0; i < rows.length; i++) {
        	flowid += rows[i].flow_id+",";
            
        }
        var confirm = $
                .confirm({
                    'msg': '${ctp:i18n("metadata.delete.alert.message.label")}',
                    ok_fn : function() {
                       
                        var r = new deeSynchronLogManager();
                        var retMap = r.delAllBySyncId(syncid,flowid);
                        var $synchronLog = $("#synchronLog");
                        if (retMap) {
                         
                            if (retMap.ret_code == "2000") {
                                $.infor("<fmt:message key='dee.synchronLog.delSuccess.label'/>");
                                
                            } else if (retMap.ret_code == "2001") {
                                $.error("<fmt:message key='dee.dataSource.error.label'/>" + retMap.ret_desc);
                            }
                        }
                        $synchronLog.ajaxgridLoad();
                        if($("#scheSummary").is(':visible') == false){
                            document.getElementById("scheSummary").contentWindow.refresh();
                        }
                    },
                    cancel_fn : function() {
                        confirm.close()
                    }
                });

    }

    function resendSycn() {
    	var rows = gridObj.grid.getSelectRows();
		if(rows.length == 0){
		    var alertObject =$.alert("<fmt:message key='dee.resend.error.label'/>");
			return;
		}
		var redosId = "";
		for(var i = 0; i < rows.length; i++){
			if(rows[i].sync_state == 1){
				var alertObject =$.alert("请选择失败任务！");
				return;
			}
			if(i == 0){
				redosId = rows[i].redo_id;
			}else{
				redosId = redosId + "," + rows[i].redo_id;
			}
		}
		var r = new deeSynchronLogManager();
        var retMap = r.deeRedoOrIgnoreMore(redosId);
        var $synchronLog = $("#synchronLog");
        if (retMap) {
            if (retMap.ret_code == "2000") {
                $.infor(retMap.ret_desc);
            } else if (retMap.ret_code == "2001") {
                $.error(retMap.ret_desc);
            }
        }
        $synchronLog.ajaxgridLoad();
    }
  

    /**
     * 初始化列表组件
     */
    function initGrid() {
        var $synchronLog = $("#synchronLog");
        gridObj = $synchronLog.ajaxgrid({
            colModel : [ {
                display : 'id',
                name : 'sync_id',
                width : '40',
                sortable : false,
                align : 'center',
                type : 'checkbox',
                isToggleHideShow : false
            }, {
                display : "<fmt:message key='dee.taskName.label'/>", // dee任务名称
                name : 'flow_dis_name',
                width : '20%'
            }, {
                display : "<fmt:message key='dee.schedule.enable.label'/>", // 状态
                name : 'sync_state',
                width : '10%'
            }, {
                display : "<fmt:message key='dee.time.label'/>", // 时间
                name : 'sync_time',
                width : '20%'
            }, {
                display : "<fmt:message key='dee.flow.exectime'/>", //执行时间
                name : 'exec_time',
                width : '10%'
            }, {
				display : "<fmt:message key='dee.resend.time.label'/>", // 重发次数
				name : 'counter',
				width : '10%'
			}, {
                display : "流程名或模块名", // 模块名称
                name : 'form_flow_name',
                width : '20%'
            }, {
                display : "操作人", // 操作人
                name : 'operate_person',
                width : '10%'
            }, {
                display : "操作", // 操作
                name : 'flow_action',
                width : '10%'
            }
            ],
            click : clk,
            render : rend,
            managerName : "deeSynchronLogManager",
            managerMethod : "findSynchronLogByConditions",
            parentId : $('.layout_center').eq(0).attr('id'),
            height : 200,
            isHaveIframe : true,
            slideToggleBtn : true,
            vChange : true,
            vChangeParam : {
                overflow : "hidden",
                autoResize : true
            }
        });
        var o = {};
        $synchronLog.ajaxgridLoad(o);
    }

    /**
     * 列表单击事件，查看异常信息
     */
    function clk(data, r, c) {

        var form = document.getElementById("listForm");
        form.action = _ctxPath
                + "/deeSynchronLogController.do?method=showDeeExceptionDetail&syncId='"
                + data.sync_id + "'&flowId='" + data.flow_id + "'&flowName='" + data.flow_dis_name + "'";

        form.target = "scheSummary";
        form.submit();
        gridObj.grid.resizeGridUpDown('middle');
    }

    function rend(txt, data, r, c) {
		
        if (c == 2) {
            if (data.sync_state == 1) {
                return "<fmt:message key='dee.status.success.label'/>";
            } else if (data.sync_state == 2) {
                return "<fmt:message key='dee.status.partFail.label'/>";
            } else if (data.sync_state == 0) {
                return "<font color='blue'><fmt:message key='dee.status.fail.label'/></font>";
            }
        }
        else if( c == 1){
            if(data.sync_state == 0 ){
                return "<font color='blue'>"+ data.flow_dis_name +"</font>";
            }
        }
        else if( c == 3){
            if(data.sync_state == 0 ){
                return "<font color='blue'>"+ data.sync_time +"</font>";
            }
        }
        else if( c==4 ){
			return data.exec_time+"ms";
		}
        return txt;
    }

   
</script>
</head>
<body>

	<div id='layout'>
		<div class="layout_north bg_color" id="north">
			<div id="toolbars"></div>
		</div>
		<div class="layout_center over_hidden" id="center">
			<table class="flexme3" id="synchronLog"></table>
			<div id="grid_detail" class="h100b">
				<iframe id="scheSummary" name="scheSummary" width="100%"
					height="100%" frameborder="0" class='calendar_show_iframe'
					style="overflow-y:hidden"></iframe>
			</div>
		</div>
	</div>
	<form name="listForm" id="listForm" method="post"
		onsubmit="return false;">
	</form>
</body>
</html>