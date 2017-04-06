<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@ include file="/WEB-INF/jsp/plugin/dee/common/common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="com.seeyon.apps.dee.resources.i18n.DeeResources"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
        function parseQuartzCode(code) {
            if (code) {
                var arr = code.split(',');
                // 兼容以前版本
                if (arr.length == 2) {
                    code = '0,' + code;
                    arr = code.split(',');
                }
                // 解析QuartzCode
                if (arr.length > 2) {
                    if (arr[0] == '0') {
                        document.getElementById('quartz_cnt').value = arr[1];
                        document.getElementById('quartz_qty').value = arr[2];
                    }
                    else {
                        document.getElementById('quartz_mt').value = arr[1];
                        document.getElementById('quartz_day').value = arr[2];
                        document.getElementById('quartz_week').value = arr[2];
                        document.getElementById('quartz_hours').value = arr[3];
                        document.getElementById('quartz_mint').value = arr[4];
                    }
                    document.getElementById("quartz_type").value = arr[0];
                    chgQuartzType();
                }
            }
        }

        function buildQuartzCode() {
            var code = '';
            if ($("#quartz_type").val() == '0') {
                code = '0,' + $("#quartz_cnt").val() + ',' + $("#quartz_qty").val();
            } else {
                var qValue = '0';
                if ($("#quartz_mt").val() == '3') {
                    qValue = $("#quartz_day").val();
                } else if ($("#quartz_mt").val() == '2') {
                    qValue = $("#quartz_week").val();
                }
                code = '1,' + $("#quartz_mt").val() + ',' + qValue + ',' +
                        $("#quartz_hours").val() + ',' + $("#quartz_mint").val();
            }
            document.getElementById('quartz_code').value = code;
        }

        function getFlowDetail() {
            // add by dkywolf 20120319 begin
            var paramStr = '';
            var flowId = document.getElementById('flow_id').value;
            if(flowId != null && flowId != undefined){
                paramStr = '&flow_id='+flowId;
            }
            var flowInfo = window.showModalDialog(ctx + '/servlet/FlowServlet?method=query4Schedule' + paramStr, '','dialogWidth:900px;dialogHeight:550px;center:yes');
            // add by dkywolf 20120319 end
            if(null!=flowInfo) {
                var flow = flowInfo.split(',');
                document.getElementById('flow_id').value = flow[0];
                document.getElementById('flow_name').value = flow[1];
            }
        }

        function checkform(){
            if ($.trim(document.forms[0].dis_name.value) == "") {
                $.alert("请填写定时器名称！");
                return false;
            }
            if ($.trim(document.forms[0].dis_name.value).length > 50) {
                $.alert("定时器名称名称不能超过50个字符！");
                return false;
            }
            if ($.trim(document.forms[0].resource_desc.value).length > 200) {
                $.alert("定时器描述不能超过200个字符！");
                return false;
            }
            if ($.trim(document.forms[0].flow_name.value) == "") {
                $.alert("请选择交换任务！");
                return false;
            }
            return true;
        }

        // 方式切换
        function chgQuartzType(){
            var type=$("#quartz_type").val();
            if(type=='0'){
                $("#fixedCtl").hide();
                $("#intervalCtl").show();
            }else{
                $("#intervalCtl").hide();
                $("#fixedCtl").show();
                if($("#quartz_mt").val() == '3'){
                    $("#fixedInnerCtl").show();
                    $("#fixedInnerOth").hide();
                }
                else if($("#quartz_mt").val() == '2'){
                    $("#fixedInnerCtl").hide();
                    $("#fixedInnerOth").show();
                }
                else{
                    $("#fixedInnerCtl").hide();
                    $("#fixedInnerOth").hide();
                }
            }
        }

        function chgInnerType(){
            if($("#quartz_mt").val() == '3'){
                $("#fixedInnerCtl").show();
                $("#fixedInnerOth").hide();
                document.getElementById('quartz_day').value  = '1';
            }
            else if($("#quartz_mt").val() == '2'){
                $("#fixedInnerCtl").hide();
                $("#fixedInnerOth").show();
                document.getElementById('quartz_week').value  = '1';
            }
            else{
                $("#fixedInnerCtl").hide();
                $("#fixedInnerOth").hide();
            }
        }

        function save() {
            buildQuartzCode();
            if (!checkform()) {
                return false;
            }
            var form = document.getElementById("submitData");
            form.action = _ctxPath + "/deeScheduleController.do?method=scheduleUpdate&id=";
            form.submit();
        }

        $(document).ready(function () {
            parseQuartzCode('${bean.quartz_code}');
        });
    </script>
</head>
<body>

<form id="submitData" action="" method="post">
    <div class="form_area">
        <div class="one_row">
            <table border="0" cellspacing="0" cellpadding="0">
                <tbody>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="dis_name"><font color="red">*</font><fmt:message key='dee.schedule.name.label'/>:</label></th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input type="text" id="dis_name" name="dis_name" value="${bean.dis_name}">
                        </div>
                    </td>
                </tr>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10"><font color="red">*</font><fmt:message key='dee.schedule.runMode.label'/>:</label></th>
                    <td>
                        <div class="common_txtbox_wrap">
                            <input name="quartz_code" id="quartz_code" value="${bean.quartz_code}" type="hidden"/>
                            <select style="width:80px;" id="quartz_type" name="quartz_type" onchange="chgQuartzType()">
                                <option value="0"><fmt:message key='dee.schedule.timeLag.label'/></option>
                                <option value="1"><fmt:message key='dee.schedule.setTime.label'/></option>
                            </select>
                            <span id="intervalCtl" style="<c:if test="${retFixed == 1}"> display:none;</c:if>" >
                                <select style="width:165px" id="quartz_cnt" name="qaertz_cnt">
                                    <c:forEach var="i" begin="1" end="60" step="1">
                                        <option value="${i}">${i}</option>
                                    </c:forEach>
                                </select>
                                <select style="width:60px" id="quartz_qty" name="quartz_qty">
                                    <option value="1"><fmt:message key='dee.schedule.minute.label'/></option>
                                    <option value="2"><fmt:message key='dee.schedule.hour.label'/></option>
                                    <option value="3"><fmt:message key='dee.schedule.day.label'/></option>
                                    <option value="4"><fmt:message key='dee.schedule.week.label'/></option>
                                    <option value="5"><fmt:message key='dee.schedule.month.label'/></option>
                                </select>
                            </span>
                            <span id="fixedCtl" style="<c:if test="${retFixed == 0}"> display:none;</c:if>">
                                <fmt:message key='dee.schedule.every.label'/>
                                <select style="width:40px" id="quartz_mt" name="quartz_mt" onchange="chgInnerType()">
                                    <option value="1"><fmt:message key='dee.schedule.day.label'/></option>
                                    <option value="2"><fmt:message key='dee.schedule.week.label'/></option>
                                    <option value="3"><fmt:message key='dee.schedule.month.label'/></option>
                                </select>
                                <span id="fixedInnerCtl">
                                    <select style="width:40px" id="quartz_day" name="quartz_day">
                                        <c:forEach var="i" begin="1" end="31" step="1">
                                            <option value="${i}">${i}</option>
                                        </c:forEach>
                                    </select>
                                    <fmt:message key='dee.schedule.daytime.label'/>
                                </span>
                                <span id="fixedInnerOth">
                                    <select style="width:55px" id="quartz_week" name="quartz_week">
                                        <option value="1"><fmt:message key='dee.schedule.monday.label'/></option>
                                        <option value="2"><fmt:message key='dee.schedule.tuesday.label'/></option>
                                        <option value="3"><fmt:message key='dee.schedule.wednesday.label'/></option>
                                        <option value="4"><fmt:message key='dee.schedule.thursday.label'/></option>
                                        <option value="5"><fmt:message key='dee.schedule.friday.label'/></option>
                                        <option value="6"><fmt:message key='dee.schedule.saturday.label'/></option>
                                        <option value="7"><fmt:message key='dee.schedule.sunday.label'/></option>
                                    </select>
                                </span>
                                <select type="text" style="width:40px" id="quartz_hours" name="quartz_hours">
                                    <c:forEach var="i" begin="0" end="23" step="1">
                                        <option value="${i}">${i}</option>
                                    </c:forEach>
                                </select>
                                <fmt:message key='dee.schedule.hour.label'/>
                                <select type="text" style="width:40px" id="quartz_mint" name="quartz_mint">
                                    <c:forEach var="i" begin="0" end="59" step="1">
                                        <option value="${i}">${i}</option>
                                    </c:forEach>
                                </select>
                                <fmt:message key='dee.schedule.minute.label'/>
                            </span>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10"><font color="red">*</font>&nbsp;<fmt:message key='dee.schedule.whetherEnable.label'/>:</label></th>
                    <td>
                        <div class="common_selectbox_wrap">
                            <input type="radio" id="isEnable" name="isEnable" value="1" <c:if test="${bean.enable}">checked</c:if>/> <fmt:message key='dee.schedule.start.label'/>
                            <input type="radio" id="isEnable" name="isEnable" value="0" <c:if test="${!bean.enable}">checked</c:if>/><fmt:message key='dee.schedule.stop.label'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="flow_name"><font color="red">*</font>&nbsp;<fmt:message key='dee.schedule.flowName.label'/>:</label></th>
                    <td width="100%">
                        <div class="common_txtbox_wrap">
                            <input type="text" id="flow_name" name="flow_name" value="${flow.DIS_NAME}" disabled="true"/>
                            <input type="hidden" id="flow_id" name="flow_id" value="${bean.flow_id}">
                        </div>
                    </td>
                </tr>
                <tr>
                    <th nowrap="nowrap">
                        <label class="margin_r_10" for="resource_desc">描述:</label></th>
                    <td>
                        <div class="common_txtbox  clearfix">
                            <textarea cols="30" rows="7" class="w100b " id="resource_desc" name="resource_desc">${bean.schedule_desc}</textarea>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="align_center">
            <input type="hidden" id="schedule_id" name="schedule_id" value="${bean.schedule_id}"/>
            <input type="hidden" id="schedule_name" name="schedule_name" value="${bean.schedule_name}"/>
            <a href="javascript:" onclick="save()" class="common_button common_button_gray"><fmt:message key='dee.dataSource.save.label'/></a>
        </div>
    </div>

</form>
</body>
</html>