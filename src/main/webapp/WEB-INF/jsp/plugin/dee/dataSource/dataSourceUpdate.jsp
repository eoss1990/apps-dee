<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@ include file="/WEB-INF/jsp/plugin/dee/common/common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript" src="${path}/ajax.do?managerName=deeDataSourceManager"></script>
    <script type="text/javascript">
        function testCon(){
            var manager = new deeDataSourceManager();
            var map = {};
            if ($("#isA8Meta").attr("checked")) {
                map["a8Meta"] = "true";
            }
            map["type"] = $("#resource_template_id").val();
            map["resource_id"] = $("#resource_id").val();
            map["resource_template_id"] = $("#resource_template_id").val();
            map["driver"] = $("#driver").val();
            map["url"] = $("#url").val();
            map["user"] = $("#user").val();
            map["password"] = $("#password").val();
            map["jndi"] = $("#jndi").val();
            var ret = manager.testCon(map);
            if (ret == '0') {
                $.infor("<fmt:message key='dee.dataSource.testSuccess.label'/>");
            } else if (ret == '1') {
                $.error("<fmt:message key='dee.dataSource.testFailed.label'/>");
            } else if (ret == '2') {
                $.alert("<fmt:message key='dee.dataSource.testNotA8Meta.label'/>");
            }
        }

        function saveDS() {
            var form = document.getElementById("submitData");
            if ($.trim(form.dis_name.value) == "") {
                $.alert("请填写数据源名称！");
                return false;
            }
            if ($.trim(form.resource_template_id.value) == 5) {
                if ($.trim(form.driver.value) == "") {
                    $.alert("请选择Driver！");
                    return false;
                }
                if ($.trim(form.url.value) == "") {
                    $.alert("请填写URL！");
                    return false;
                }
                if ($.trim(form.user.value) == "") {
                    $.alert("请填写Username！");
                    return false;
                }
                if ($.trim(form.password.value) == "") {
                    $.alert("请填写Password！");
                    return false;
                }
            } else if ($.trim(form.resource_template_id.value) == 10) {
                if ($.trim(form.jndi.value) == "") {
                    $.alert("请填写JNDI内容！");
                    return false;
                }
            }
            var manager = new deeDataSourceManager();
            var map = {};
            // 是否A8元数据
            if ($("#isA8Meta").attr("checked")) {
                map["isA8Meta"] = "true";
            }
            // 是JDBC还是JNDI
            if ($("#resource_template_id").val() == "5") {
                map["driver"] = $("#driver").val();
                map["url"] = $("#url").val();
                map["user"] = $("#user").val();
                map["password"] = $("#password").val();
            } else if ($("#resource_template_id").val() == "10") {
                map["jndi"] = $("#jndi").val();
            }
            map["dis_name"] = $("#dis_name").val();
            map["resource_id"] = $("#resource_id").val();
            map["resource_template_id"] = $("#resource_template_id").val();

            var retMap = manager.updateDataSource(map);
            if (retMap) {
                if (retMap.ret_code == "2000") {
                    var options = {
                        msg: "<fmt:message key='dee.dataSource.succeed.label'/>",
                        ok_fn: function () {
                            window.location.href = _ctxPath + "/deeDataSourceController.do?method=showDataSourceDetail&id=" + $("#resource_id").val();
                        }
                    };
                    $.infor(options);
                } else if (retMap.ret_code == "2001") {
                    $.error("<fmt:message key='dee.dataSource.error.label'/>" + retMap.ret_desc);
                } else if (retMap.ret_code == "2002") {
                    $.error("<fmt:message key='dee.dataSource.error.label'/>"  + "<fmt:message key='dee.update.dataSource.tips'/>" );
                }
            }
        }

        function allDisable() {
            for (var i = 0; i < document.forms.length; i++) {
                var oForm = document.forms[i];
                for (var j = 0; j < oForm.elements.length; j++) {
                    oForm.elements[j].disabled = true;
                }
            }
        }

        function changeTemplate(type) {
            if (type == "MySQL") {
                $("#driver").val("com.mysql.jdbc.Driver");
                $("#url").val("jdbc:mysql://[host]:[port|3306]/[DB_Name]?autoReconnection=true");
            } else if (type == "Oracle") {
                $("#driver").val("oracle.jdbc.driver.OracleDriver");
                $("#url").val("jdbc:oracle:thin:@[host]:[prot|1521]:[DB_Name]");
            } else if (type == "PostgreSQL") {
                $("#driver").val("org.postgresql.Driver");
                $("#url").val("jdbc:postgresql://[host]:[port|5432]/[DB_Name]");
            } else if (type == "DB2") {
                $("#driver").val("com.ibm.db2.jcc.DB2Driver");
                $("#url").val("jdbc:db2://[host]:[port|50000]/[DB_Name]");
            } else if (type == "SQLServer") {
                $("#driver").val("net.sourceforge.jtds.jdbc.Driver");
                $("#url").val("jdbc:jtds:sqlserver://[host]:[port|1433]/[DB_Name]");
            }else if (type=="DM") {
                $("#driver").val("dm.jdbc.driver.DmDriver");
                $("#url").val("jdbc:dm://[host]:[port|5236]");
            }
        }

        function changeSourceType(obj) {
            if ($(obj).val() == 5) {
                $(".jdbc-category").show();
                $(".jndi-category").hide();
                if (!$("#url").val()) {
                    changeTemplate("MySQL");
                }
            } else if ($(obj).val() == 10) {
                $(".jdbc-category").hide();
                $(".jndi-category").show();
            }
        }

        function init() {
            var dsType = "${dsType}";
            if (dsType == 5) {
                $(".jdbc-category").show();
                $(".jndi-category").hide();
            } else if (dsType == 10) {
                $(".jdbc-category").hide();
                $(".jndi-category").show();
            }

            /*  var driverStr = '${deeResourceSubBean.driver}';
             if (driverStr != '') {    // 新建数据源时没有值直接给一个mysql的模板
             if (driverStr.indexOf('mysql') > -1) {
             $("#db option[value='MySQL']").attr("selected", 'selected');
             } else if (driverStr.indexOf('jtds') > -1) {
             $("#db option[value='SQLServer']").attr("selected", 'selected');
             } else if (driverStr.indexOf('oracle') > -1) {
             $("#db option[value='Oracle']").attr("selected", 'selected');
             } else if (driverStr.indexOf('db2') > -1) {
             $("#db option[value='DB2']").attr("selected", 'selected');
             } else if (driverStr.indexOf('postgresql') > -1) {
             $("#db option[value='PostgreSQL']").attr("selected", 'selected');
             }else if (driverStr.indexOf('dm') > -1) {
             $("#db option[value='DM']").attr("selected", 'selected');
             }
             }*/
        }

        $(function() {
            init();
        });
    </script>
</head>

<form id="submitData" action="" method="post">
    <div class="form_area">
        <div class="one_row">
            <table border="0" cellspacing="0" cellpadding="0">
                <tbody>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="dis_name"><font color="red">*</font><fmt:message key="dee.dataSource.dis_name.label"/>:</label>
                    </th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input readonly="readonly" type="text" id="dis_name" name="dis_name" value="${deeResource.dis_name}">
                        </div>
                    </td>
                </tr>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10"><font color="red">*</font><fmt:message key="dee.dataSource.template_name.label"/>:</label>
                    </th>
                    <td>
                        <div class="common_selectbox_wrap">
                            <select name="resource_template_id" id="resource_template_id" onchange="changeSourceType(this);">
                                <c:forEach var="sourceType" items="${sourceTypes}">
                                    <option value="${sourceType.value}" <c:if test="${dsType == sourceType.value}">selected</c:if>>${sourceType.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </td>
                </tr>
                <tr class="jdbc-category">
                    <th nowrap="nowrap">
                        <label class="margin_r_10"><font color="red">*</font>Driver:</label>
                    </th>
                    <td>
                        <div class="common_selectbox_wrap">
                            <select id="db" name="db" onchange="changeTemplate(this.value)" style="width:30%">
                                <option value="MySQL" <c:if test="${dsType=='5'}"> <c:if
                                        test="${deeResourceSubBean.driver == 'com.mysql.jdbc.Driver'}">selected</c:if> </c:if>>MySQL
                                </option>
                                <option value="SQLServer" <c:if test="${dsType=='5'}"> <c:if
                                        test="${deeResourceSubBean.driver == 'net.sourceforge.jtds.jdbc.Driver'}">selected</c:if> </c:if>>
                                    SQLServer
                                </option>
                                <option value="Oracle" <c:if test="${dsType=='5'}"> <c:if
                                        test="${deeResourceSubBean.driver == 'oracle.jdbc.driver.OracleDriver'}">selected</c:if> </c:if>>
                                    Oracle
                                </option>
                                <option value="DB2" <c:if test="${dsType=='5'}"> <c:if
                                        test="${deeResourceSubBean.driver == 'com.ibm.db2.jcc.DB2Driver'}">selected</c:if> </c:if>>DB2
                                </option>
                                <option value="PostgreSQL" <c:if test="${dsType=='5'}"> <c:if
                                        test="${deeResourceSubBean.driver == 'org.postgresql.Driver'}">selected</c:if> </c:if>>PostgreSQL
                                </option>
                                <option value="DM" <c:if test="${dsType=='5'}"> <c:if
                                        test="${deeResourceSubBean.driver == 'dm.jdbc.driver.DmDriver'}">selected</c:if> </c:if>>DM
                                </option>
                            </select>
                            <input type="text" readonly="readonly" id="driver" name="driver" size="35" class="cursor-hand input-50per" style="width:68%;"
                                    <c:if test="${dsType=='5'}"> value="${deeResourceSubBean.driver}" </c:if> />
                        </div>
                    </td>
                </tr>
                <tr class="jdbc-category">
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="url"><font color="red">*</font>&nbsp;URL:</label>
                    </th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input type="text" id="url" name="url" <c:if test="${dsType=='5'}"> value="${deeResourceSubBean.url}" </c:if> />
                        </div>
                    </td>
                </tr>
                <tr class="jdbc-category">
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="user"><font color="red">*</font>&nbsp;Username:</label>
                    </th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input type="text" id="user" name="user" <c:if test="${dsType=='5'}"> value="${deeResourceSubBean.user}" </c:if> />
                        </div>
                    </td>
                </tr>
                <tr class="jdbc-category">
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="password"><font color="red">*</font>&nbsp;Password:</label>
                    </th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input type="password" id="password" name="password" <c:if test="${dsType=='5'}"> value="${deeResourceSubBean.password}" </c:if> />
                        </div>
                    </td>
                </tr>
                <tr class="jndi-category">
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="jndi"><font color="red">*</font>&nbsp;<fmt:message key="dee.dataSource.JNDIContent.label"/>:</label>
                    </th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input type="text" id="jndi" name="jndi" <c:if test="${dsType=='10'}"> value="${deeResourceSubBean.jndi}" </c:if> />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="isA8Meta">&nbsp;<fmt:message key="dee.dataSource.is_a8meta.label"/>:</label>
                    </th>
                    <td width="100%">
                        <div class="common_selectbox_wrap">
                            <input type="checkbox" id="isA8Meta" name="isA8Meta" <c:if test="${metaFlag == 'true'}">checked</c:if> />
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="align_center">
            <input type="hidden" id="resource_id" name="resource_id" size="70" value="${deeResource.resource_id}"/>
            <input type="hidden" id="resource_desc" name="resource_desc" size="70" value="${deeResource.resource_desc}"/>
            <a href="javascript:" onclick="javascript:testCon();" class="common_button common_button_gray"><fmt:message key="dee.dataSource.TestConnection.label"/></a>
            <a href="javascript:" onclick="javascript:saveDS();" class="common_button common_button_gray"><fmt:message key='dee.dataSource.save.label'/></a>
        </div>
    </div>
</form>
</html>


