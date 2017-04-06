<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@ include file="/WEB-INF/jsp/plugin/dee/common/common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
        function allDisable() {
            for (var i = 0; i < document.forms.length; i++) {
                var oForm = document.forms[i];
                for (var j = 0; j < oForm.elements.length; j++) {
                    oForm.elements[j].disabled = true;
                }
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
            /*var driverStr = '${jdbcsubbean.driver}';
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
            allDisable();
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
        </div>
    </div>
</form>
</html>


