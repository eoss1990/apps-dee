<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<script type="text/javascript">
    var menus=[
        {name:"<fmt:message key='dee.pluginMainMenu.label'/>"},
        {name:"${ctp:i18n('system.menuname.delete')}", url:"/deeDeleteController.do?method=getFlowFrame"}
    ];
    var gridObj;

    $(document).ready(function () {
        updateCurrentLocation(menus);
        initTree();
        initToolbar();
        initSearchObj();
        initTable();
    });

    /**
     * 初始化任务分类树
     */
    function initTree() {
        var $deeTree = $("#deeTree");
        $deeTree.tree({
            idKey: "id",
            pIdKey: "parentId",
            nameKey: "name",
            managerName: "deeTriggerDesignManager",
            managerMethod: "getFlowListTree",
            onClick: nodeClk,
            nodeHandler: function (n) {
                n.open = true;
            }
        });
        $deeTree.empty();
        $deeTree.treeObj().reAsyncChildNodes(null, "refresh");
    }

    function nodeClk(e, treeId, node) {
        var o = {};
        o.flowType = node.id;
        $("#deeTable").ajaxgridLoad(o);
    }

    /**
     * 初始化工具栏组件
     */
    function initToolbar() {
        $("#toolbars").toolbar({
            toolbar: [
                {
                    id: "modify",
                    name: "${ctp:i18n('link.jsp.del')}",
                    className: "ico16 del",
                    click: function() {
                        var rows = gridObj.grid.getSelectRows();
                        if (rows.length < 1) {
                            $.alert("<fmt:message key='dee.deltask.deltip.nochoose'/>");
                            return;
                        }
                        var dialog = $.confirm({
                            'msg': '${ctp:i18n("metadata.delete.alert.message.label")}',
                            ok_fn: function () {
                                var manager = new deeDeleteManager();
                                var flowIds = [];
                                var rows = gridObj.grid.getSelectRows();
                                for (var i=0; i<rows.length;i++) {
                                    flowIds[i] = rows[i].fLOW_ID;
                                }
                                var result = manager.deleteFlow(flowIds.join(","));
                                var options = {
                                    msg : result.info,
                                    ok_fn : function() {
                                        $("#deeTable").ajaxgridLoad();
                                    }
                                };
                                if (result.ret_code == '2000') {
                                    $.infor(options);
                                } else if (result.ret_code == '2001') {
                                    $.alert(options);
                                } else if (result.ret_code == '2002') {
                                    $.error(options);
                                }
                            },
                            cancel_fn:function(){dialog.close();}
                        });
                    }
                }
            ]
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
                var o = {};
                var choose = $('#' + searchObj.p.id).find("option:selected").val();
                if (choose === 'flowName') {
                    o.flowName = $('#flowName').val();
                }
                var val = searchObj.g.getReturnValue();
                if (val !== null) {
                    $("#deeTable").ajaxgridLoad(o);
                }
            },
            conditions: [{
                id: 'flowName',
                name: 'flowName',
                type: 'input',
                text: '${ctp:i18n('form.trigger.triggerSet.taskName.label')}',
                value: 'flowName'
            }]
        });
    }

    /**
     * 初始化任务表格
     */
    function initTable() {
        var $deeTable = $("#deeTable");
        gridObj = $deeTable.ajaxgrid({
            colModel: [
                {
                    display: '${ctp:i18n("form.trigger.triggerSet.select.label")}',
                    name: 'fLOW_ID',
                    width: '7%',
                    sortable: false,
                    align: 'center',
                    type: 'checkbox',
                    isToggleHideShow: false
                },
                {
                    display: "${ctp:i18n("form.trigger.triggerSet.collectionDataName.label")}",
                    name: 'dIS_NAME',
                    width: '20%'
                },
                {
                    display: "${ctp:i18n("form.trigger.triggerSet.taskClassification.label")}",
                    name: 'fLOW_TYPE_NAME',
                    width: '20%'
                },
                {
                    display: "${ctp:i18n("form.base.modifytime")}",
                    name: 'cREATE_TIME',
                    width: '20%'
                },
                {
                    display: "数据源",
                    name: 'eXT3',
                    width: '30%'
                }
            ],
            managerName: "deeTriggerDesignManager",
            managerMethod: "getFlowList",
            parentId: $('.layout_east').eq(0).attr('id'),
            usepager: true,
            height: $(document).height() - 70,
            isHaveIframe: false,
            slideToggleBtn: false,
            vChange: false,
            vChangeParam: {
                overflow: "hidden",
                autoResize: false
            }
        });
        $deeTable.ajaxgridLoad({});

        $(".vGrip").hide();
    }
</script>