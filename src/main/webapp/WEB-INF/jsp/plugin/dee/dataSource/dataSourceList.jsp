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
    <title><fmt:message key='dee.dataSource.manager'/></title>
    <script type="text/javascript">
        var menus = [
            {name: "<fmt:message key='dee.pluginMainMenu.label'/>"},
            {
                name: "${ctp:i18n('system.menuname.dataSource')}",
                url: "/deeDataSourceController.do?method=dataSourceFrame"
            }
        ];
        var listTable;
		 
        updateCurrentLocation(menus);

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
                        id: "modify",
                        name: "${ctp:i18n('link.jsp.modify')}",
                        className: "ico16 modify_text_16",
                        click: function () {
                            modify();
                        }
                    }, {
                        id: "delete",
                        name: "<fmt:message key='dee.button.delete'/>", 
                        className: "ico16 del",
                        click: function () {
                            var hasChecked = $("input:checked", $("#listDataSourceTable"));

                            if (hasChecked.length == 0) {
                                $.alert("<fmt:message key='dee.resend.error.label'/>");
                                return;
                            }
                            var dialog = $.confirm({
                                'msg': '${ctp:i18n("metadata.delete.alert.message.label")}',
                                ok_fn: function () {
                                    var id = "";
                                    for (var i = 0; i < hasChecked.length; i++) {
                                        id += hasChecked[i].value + ":";
                                    }
                                    var newId = id.substring(0, id.length - 1);
                                    var dialogUrl = _ctxPath + "/deeDataSourceController.do?method=dataSourceDelete&id=" + newId;
                                    $.ajax({
                                        type: "POST",
                                        url: dialogUrl,
                                        dataType: "text",
                                        error: function () {
                                            alert( "<fmt:message key='dee.dataSource.net.error'/>"+"！");
                                        },
                                        success: function (responseText) {
										//	alert(responseText);
										//	debugger;
                                            if (responseText == "success") {
                                                $.infor( "<fmt:message key='dee.dataSource.delete.ok'/>" + "！")
                                   
													$("#listDataSourceTable").ajaxgridLoad();
                                            } else {
                                                $.infor("<fmt:message key='dee.dataSource.name'/>" + responseText + "<fmt:message key='dee.dataSource.no.del'/>" + "！");
                                                //刷新父页面 上半部分
												$("#listDataSourceTable").ajaxgridLoad();
                                                //如果子页面打开了就刷新子页面 下半部分
										
                                              if (document.getElementById("dataSourceDetail").contentWindow.document.getElementById("dataSource") != null) {
                                                    var id = newId.split(":")[0];
                                                    var form = document.getElementById("listForm");
                                                    form.action = _ctxPath
                                                    + "/deeDataSourceController.do?method=showDataSourceDetail&id=" + id;
                                                    listForm.target = "dataSourceDetail";
                                                    form.submit();
                                                    listTable.grid.resizeGridUpDown('middle');
                                                }
												 
                                            }
                                        }
                                    });
                                },
                                cancel_fn: function () {
                                    dialog.close();
                                }
                            });
                        }
                    }
                ]

            });

            var topSearchSize = 7;
            if ($.browser.msie && $.browser.version == '6.0') {
                topSearchSize = 5;
            }
            var searchobj = $.searchCondition({
                top: topSearchSize,
                right: 10,
                searchHandler: function () {
                    var o = new Object();
                    var choose = $('#' + searchobj.p.id).find("option:selected").val();
                    if (choose === 'dis_name') {
                        o.dis_name = $('#dis_name').val();
                    }
                    var val = searchobj.g.getReturnValue();
                    if (val !== null) {
                        $("#listDataSourceTable").ajaxgridLoad(o);
                    }
                },
                conditions: [{
                    id: 'dis_name',
                    name: 'dis_name',
                    type: 'input',
                    text: '<fmt:message key="dee.dataSource.dis_name.label"  />',
                    value: 'dis_name'
                }]
            });
            
            listTable = $("#listDataSourceTable").ajaxgrid({
                colModel: [
                {
                        display: 'id',
                        name: 'resource_id',
                        width: '5%',
                        sortable: false,
                        align: 'center',
                        type: 'checkbox',
                        isToggleHideShow: false
                    },
                    {
                        display: "<fmt:message key='dee.dataSource.dis_name.label'/>",        // 数据源名称
                        name: 'dis_name',
                        width: '27%'
                    },
                    {
                        display: "<fmt:message key='dee.dataSource.template_name.label'/>",    // 数据源类型
                        name: 'resource_template_id',
                        width: '28%'
                    },
                    {
                        display: "<fmt:message key='dee.dataSource.is_a8meta.label'/>",      // A8元数据
                        name: 'resource_template_name',
                        width: '20%'
                    },
                    {
                        display: "<fmt:message key='dee.dataSource.create_time.label'/>", // 修改时间
                        name: 'create_time',
                        width: '20%'
                    }
                ],
                click: clk,
                dblclick: dblclk,
                render: rend,
                managerName: "deeDataSourceManager",
                managerMethod: "dataSourceList",
                parentId: $('.layout_center').eq(0).attr('id'),
                height: 200,
                isHaveIframe: true,
                slideToggleBtn: true,
                customize:false,
                vChange: true,
                vChangeParam: {
                    overflow: "hidden",
                    autoResize: true
                }
            });
            /*             var o = new Object();
             $("#listDataSourceTable").ajaxgridLoad(o); */
        });

        function clk(data, r, c) {
            $("#grid_detail").resetValidate();
            var form = document.getElementById("listForm");
            form.action = _ctxPath + "/deeDataSourceController.do?method=showDataSourceDetail&id=" + data.resource_id;
            form.target = "dataSourceDetail";
            form.submit();
            /*             listTable.grid.resizeGridUpDown('middle');
             $(".bDiv","#center").css("height","185px"); */
        }

        function dblclk(data, r, c) {
            var listForm = document.getElementById("listForm");
            listForm.action = _ctxPath + "/deeDataSourceController.do?method=showDataSourceUpdate&id=" + data.resource_id;
            listForm.target = "dataSourceDetail";
            listForm.submit();
        }

        function rend(txt, data, r, c) {
			//debugger;
            if (c == 2) {
                if (data.resource_template_id == 5 || (data.resource_template_id == 33 && data.dr.jndi == '')) {
                    return "JDBC";
                } else if (data.resource_template_id == 10 || (data.resource_template_id == 33 && data.dr.jndi != '')) {
                    return "JNDI";
                }
            }
            else if (c == 3) {
              /*  if (data.resource_template_id == 5 || data.resource_template_id == 10) {
                    return "否";
                } else if (data.resource_template_id == 33) {
                    return "是";
                }*/
				if( data.resource_template_name == "A8MetaDatasource"){
					return "是";
				}else{
					 return "否";
				}
            }
            return txt;
        }

        function modify() {
            var hasChecked = $("input:checked", $("#listDataSourceTable"));
            if (hasChecked.length == 0) {
                $.alert("<fmt:message key='dee.resend.error.label'/>");
                return;
            }
            if (hasChecked.length > 1) {
                $.alert("<fmt:message key='dee.dataSource.updateError.label'/>");
                return;
            }
            var id = "";
            for (var i = 0; i < hasChecked.length; i++) {
                id = hasChecked[i].value;
            }
            var listForm = document.getElementById("listForm");
            listForm.action = _ctxPath + "/deeDataSourceController.do?method=showDataSourceUpdate&id=" + id;
            listForm.target = "dataSourceDetail";
            listForm.submit();
            listTable.grid.resizeGridUpDown('middle');
        }
    </script>
</head>
<body>

<div id='layout'>
    <div class="layout_north bg_color" id="north">
        <div id="toolbars"></div>
    </div>
    <div class="layout_center over_hidden" id="center">
        <table class="flexme3 " id="listDataSourceTable"></table>
        <div id="grid_detail" class="h100b">
            <iframe id="dataSourceDetail" name="dataSourceDetail" width="100%" height="100%" frameborder="0"
                    class='calendar_show_iframe' style="overflow-y:hidden"></iframe>
        </div>
    </div>
</div>
<form name="listForm" id="listForm" method="post" onsubmit="return false">
</form>
</body>
</html>