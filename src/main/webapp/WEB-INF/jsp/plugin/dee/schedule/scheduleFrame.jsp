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
    <title>定时器管理</title>
    <script type="text/javascript">
        var menus=[
            {name:"<fmt:message key='dee.pluginMainMenu.label'/>"},
            {name:"${ctp:i18n('system.menuname.schedule')}", url:"/deeScheduleController.do?method=scheduleFrame"}
        ];
        var gridObj;

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
                toolbar: [
                    {
                        id: "modify",
                        name: "${ctp:i18n('link.jsp.modify')}",
                        className: "ico16 modify_text_16",
                        click: function () {
                            modify();
                        }
                    }
                ]
            });
        }

        /**
         * 初始化搜索栏组件
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
                    var o = {};
                    var choose = $('#' + searchObj.p.id).find("option:selected").val();
                    if (choose === 'dis_name') {
                        o.dis_name = $('#dis_name').val();
                    } else if (choose === 'flow_name') {
                        o.flow_name = $('#flow_name').val();
                    }
                    var val = searchObj.g.getReturnValue();
                    if (val !== null) {
                        $("#scheduleTable").ajaxgridLoad(o);
                    }
                },
                conditions: [
                    {
                        id: 'dis_name',
                        name: 'dis_name',
                        type: 'input',
                        text: '<fmt:message key="dee.schedule.name.label" />',
                        value: 'dis_name'
                    },
                    {
                        id: 'flow_name',
                        name: 'flow_name',
                        type: 'input',
                        text: '<fmt:message key="dee.schedule.flowName.label" />',
                        value: 'flow_name'
                    }
                ]
            });
        }

        /**
         * 初始化列表组件
         */
        function initGrid() {
            var $scheduleTable = $("#scheduleTable");
            gridObj = $scheduleTable.ajaxgrid({
                colModel: [
                    {
                        display: 'id',
                        name: 'schedule_id',
                        width: '40',
                        sortable: false,
                        align: 'center',
                        type: 'checkbox',
                        isToggleHideShow: false
                    },
                    {
                        display: "<fmt:message key='dee.schedule.name.label'/>",        // 定时器名称
                        name: 'dis_name',
                        width: '27%'
                    },
                    {
                        display: "<fmt:message key='dee.schedule.flowName.label'/>",    // 任务名称
                        name: 'flow_name',
                        width: '28%'
                    },
                    {
                        display: "<fmt:message key='dee.schedule.enable.label'/>",      // 状态
                        name: 'enable',
                        width: '20%'
                    },
                    {
                        display: "<fmt:message key='dee.schedule.description.label'/>", // 描述
                        name: 'schedule_desc',
                        width: '20%'
                    }
                ],
                click: clk,
                dblclick: dblclk,
                render: rend,
                managerName: "deeScheduleManager",
                managerMethod: "findScheduleList",
                parentId: $('.layout_center').eq(0).attr('id'),
                height: 200,
                isHaveIframe: true,
                slideToggleBtn: true,
                vChange: true,
                vChangeParam: {
                    overflow: "hidden",
                    autoResize: true
                }
            });
            $scheduleTable.ajaxgridLoad({});
        }

        /**
         * 列表单击事件，查看定时器信息
         */
        function clk(data, r, c) {
            var form = document.getElementById("listForm");
            form.action = _ctxPath + "/deeScheduleController.do?method=showScheduleDetail&id=" + data.schedule_id;
            form.target = "scheSummary";
            form.submit();
            gridObj.grid.resizeGridUpDown('middle');
        }

        /**
         * 列表单击事件，修改定时器信息
         */
        function dblclk(data, r, c) {
            refreshModify(data.schedule_id);
        }

        function rend(txt, data, r, c) {
            if (c == 3) {
                if (data.enable + "" == "true") {
                    return "<fmt:message key='dee.schedule.start.label'/>";
                } else if (data.enable + "" == "false") {
                    return "<fmt:message key='dee.schedule.stop.label'/>";
                }
            }
            return txt;
        }

        /**
         * 工具栏的修改事件
         */
        function modify() {
            var rows = gridObj.grid.getSelectRows();
            if (rows.length < 1) {
                $.alert("<fmt:message key='dee.resend.error.label'/>");
                return;
            }
            if (rows.length > 1) {
                $.alert("<fmt:message key='dee.dataSource.updateError.label'/>");
                return;
            }
            refreshModify(rows[0].schedule_id);
        }

        function refreshModify(id) {
            var listForm = document.getElementById("listForm");
            listForm.action = _ctxPath + "/deeScheduleController.do?method=showScheduleUpdate&id=" + id;
            listForm.target = "scheSummary";
            listForm.submit();
            gridObj.grid.resizeGridUpDown('middle');
        }
    </script>
</head>
<body>

<div id='layout'>
    <div class="layout_north bg_color" id="north">
        <div id="toolbars"></div>
    </div>
    <div class="layout_center over_hidden" id="center">
        <table class="flexme3" id="scheduleTable"></table>
        <div id="grid_detail" class="h100b">
            <iframe id="scheSummary" name="scheSummary" width="100%" height="100%" frameborder="0" class='calendar_show_iframe' style="overflow-y:hidden"></iframe>
        </div>
    </div>
</div>
<form name="listForm" id="listForm" method="post" onsubmit="return false;">
</form>
</body>
</html>