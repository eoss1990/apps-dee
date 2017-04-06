<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<%@ include file="/WEB-INF/jsp/plugin/deeSection/selectTask/formHeader.jsp"%>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/xtree/xtree.js${v3x:resSuffix()}" />"></script>
<link href="<c:url value="/common/js/xtree/xtree.css${v3x:resSuffix()}" />" type="text/css" rel="stylesheet">
<script type="text/javascript">
<!--
var templeteCategoryType = '<c:out value="${param.categoryType}" default="0" />';
//-->

function checknodisable(){
    var newbutton = parent.templeteToolBarFrame.document.all("new");
	var updatebutton = parent.templeteToolBarFrame.document.all("updateCategory");
	var deletebutton = parent.templeteToolBarFrame.document.all("deleteCategory");
	newbutton.disabled = false;
	updatebutton.disabled = false;
	deletebutton.disabled = false; 
}
function showTempleteList(categoryId,categorType){
	if(categoryId != null){
		parent.parent.document.all.type_id.value=categoryId;
		var taskName=parent.parent.document.all.taskName.value;
		parent.showList(categoryId,taskName==""?null:taskName);
	}	
}
</script>

<c:set value="${(param.categoryType=='0') && (param.from == 'SYS')}" var="hasClick" />

</head>
<body onload="showTempleteList('-1');dontSelect();" onkeydown="${v3x:outConditionExpression(noClick, 'listenerKeyESC()', '')}">

<div class="scrollList border-padding">
<script type="text/javascript">
webFXTreeConfig.fileIcon = webFXTreeConfig.folderIcon;


function dontSelect()
{
	try{		
		tree.select()
	}
	catch(e){
	}
}

//if (document.getElementById) 
{
	var tree = new WebFXTree('-1', "<fmt:message key='form.trigger.triggerSet.DEETask.label' />", "javascript:showTempleteList('-1','${param.categoryType}');dontSelect();");
	tree.setBehavior('classic');
	tree.icon = "<c:url value='/apps_res/collaboration/images/templete.gif'/>";
	tree.openIcon = "<c:url value='/apps_res/collaboration/images/templete.gif'/>";
	<c:forEach items="${typeList}" var="c">
		<%--  WebFXTreeItem(sBusinessId, sText, sAction, sDBlClick, eParent, sIcon, sOpenIcon)  --%>
		var tree${fn:replace(c.FLOW_TYPE_ID, '-', '_')} = new WebFXTreeItem("${c.FLOW_TYPE_ID}", "${v3x:escapeJavascript(c.FLOW_TYPE_NAME)}", "javascript:showTempleteList('${c.FLOW_TYPE_ID}','')");
		//tree${c.FLOW_TYPE_ID}.categoryType = ${param.categoryType};
	</c:forEach>
	var expandObj;//需展开的节点对象
	<c:forEach items="${typeList}" var="c">
	  <c:choose>
	    <c:when test="${c.PARENT_ID=='-1'}">
		<%-- tree${fn:replace(c.parentId, '-', '_')}.add(tree${fn:replace(c.id, '-', '_')}); --%>
			tree.add(tree${fn:replace(c.FLOW_TYPE_ID, '-', '_')});
			expandObj = tree${fn:replace(c.FLOW_TYPE_ID, '-', '_')};
		</c:when>
		<c:otherwise>
		try{
		if(tree${fn:replace(c.PARENT_ID, '-', '_')}){
			tree${fn:replace(c.PARENT_ID, '-', '_')}.add(tree${fn:replace(c.FLOW_TYPE_ID, '-', '_')});
		}
		}catch(e){}
		</c:otherwise>
	  </c:choose>
	</c:forEach>

	document.write(tree);
	document.close();
    if (expandObj && !expandObj.openIcon) {
        expandObj.openIcon = "<c:url value='/common/js/xtree/images/foldericon.png'/>";
    }
	expandObj.expand();
	//if(templeteCategoryType=="1"){categoryId="2";}
	//if(templeteCategoryType=="1"){webFXTreeHandler.select(tree2);}
}
</script>
</div>
<iframe src="javascript:void(0)" name="hiddenIframe" frameborder="0" height="0" width="0" scrolling="no" marginheight="0" marginwidth="0"></iframe>
</body>
</html>