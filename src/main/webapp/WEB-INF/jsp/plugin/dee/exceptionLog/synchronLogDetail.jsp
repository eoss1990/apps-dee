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
<script type="text/javascript">
var gridObj;

$(document).ready(function () {
    initTree();
});
/**
 * 初始化任务分类树
 */
function initTree() {
    var $adapterTree = $("#adapterTree");
    var o = {};
    o.sysId = ${syncId};
    o.flowId = <%= request.getParameter("flowId")%>;
    $adapterTree.tree({
        idKey: "id",
        pIdKey: "parentId",
        nameKey: "name",
        managerName: "deeSynchronLogManager",
        managerMethod: "getAdapterListTree",
        asyncParam:o,
        onClick: nodeClk,
        nodeHandler: function (n) {
            n.open = true;
            var path = "<%= this.getServletContext().getRealPath("/").replace("\\", "/") %>";
            if(n.data.state == 1){
                n.icon = path + "common/images/msdropdown/icons/1273404841_tick_16.png";
            }else if (n.data.state == 0){
                n.icon = path + "common/images/risk3.gif";
            }
        }
    });
    $adapterTree.empty();
    $adapterTree.treeObj().reAsyncChildNodes(null, "refresh");
}
function nodeClk(e, treeId, node) {
    var r = new deeSynchronLogManager();
    var retMap = r.getAdapterDetail(${syncId}+","+node.id+","+<%= request.getParameter("flowId")%>);
    if(!("startData" == retMap.adapterName || "endData" == retMap.adapterName)){
    	if("" != retMap.data){
    		$("#dataText").val("异常信息:"+formatXml(unescapeHTML(retMap.data)));
    	}else{
    		$("#dataText").val(retMap.parm)
    	}
    } else {
        $("#dataText").val("数据:"+formatXml(unescapeHTML(retMap.data)) + 
        		"\r\n参数:" + retMap.parm.replace(new RegExp('],','gm'),'],\r\n'));
    }
}
function formatXml(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match( /.+<\/\w[^>]*>$/ )) {
            indent = 0;
        } else if (node.match( /^<\/\w/ )) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) {
            indent = 1;
        } else {
            indent = 0;
        }

        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }

        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
}
function unescapeHTML(target) {
    //还原为可被文档解析的HTML标签            
    return target.replace(/&quot;/g, '"').replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&")
        //处理转义的中文和实体字符
        .replace(/&#([\d]+);/g, function($0, $1) {
            return String.fromCharCode(parseInt($1, 10));
        });
}
</script>
</head>
<body>
	<table style="width: 100%; height: 235px;">
		<tr>
			<td style="vertical-align:top; width: 20%;">
        		<textarea id="adapterTree"></textarea>
			</td>
			<td  style="vertical-align:top; word-wrap:break-word; word-break:break-all;">
				<textarea id="dataText" readonly="readonly" style="width: 100%; height: 232px; border: 0px;"></textarea>
			</td>
		</tr>
	</table>
	<input type="hidden" id="syncId" value="${syncId}" />
	<input type="hidden" id="flowId" value="${flowId}" />
</body>
</html>