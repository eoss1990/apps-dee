<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">	
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<%@ include file="/WEB-INF/jsp/plugin/deeSection/selectTask/formHeader.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />"/>
<script type="text/javascript">
window.onload= function (){
	loadReady();
}

function loadReady(){
	if(parent.parent.document.readyState=="complete"){
		//load();
	}else{
		setTimeout("loadReady()",100);
	}
}

function selectTaskEvent(taskId,taskName){
	//alert(parent.parent.parent.document.all.taskName[0]);
	parent.parent.document.all.returnId.value=taskId;
	parent.parent.document.all.returnName.value=taskName;
}

function load(){
	var taskId  = parent.parent.document.all.taskId.value;
	var tasks = document.getElementsByName("id");
	for(var i =0 ;tasks!=null&&i<tasks.length;i++){
		if(tasks[i].value==taskId){
			tasks[i].checked = true;
		}
	}
}
</script>
</head>
<body scroll="no">
<div class="scrollList" style="height:101%;width:101%">
<form name="listForm" id="listForm" method="get" onsubmit="return false" style="margin: 0px">
	 <v3x:table data="flowList" var="con" htmlId="sss"
				isChangeTRColor="true" showHeader="true" showPager="true"  pageSize="20" className="ellipsis sort">
				<c:set var="click" value="selectTaskEvent('${con.FLOW_ID }','${con.DIS_NAME}');"/>
				<v3x:column width="10%" align="center" onClick="${click}"
					label="form.trigger.triggerSet.select.label">
					<input type='radio' name='id' value="${con.FLOW_ID}" taskName="${con.FLOW_NAME }"/>
				</v3x:column>
	
				<v3x:column onClick="${click}" width="30%" label="form.trigger.triggerSet.collectionDataName.label" alt="${con.DIS_NAME}"  value="${con.DIS_NAME}" 
					className="cursor-hand sort"  type="String"></v3x:column>
					
				<v3x:column onClick="${click}" width="30%" label="form.trigger.triggerSet.taskClassification.label" alt="${con.FLOW_TYPE_NAME}"  value="${con.FLOW_TYPE_NAME}" 
					className="cursor-hand sort"  type="String"></v3x:column>
					
				<v3x:column onClick="${click}" width="30%" label="${deeFlowCreateTime}" alt="${con.CREATE_TIME}"  value="${con.CREATE_TIME}" 
					className="cursor-hand sort"  type="String"></v3x:column>
			</v3x:table>
</form>	
</div>
</body>
</html>