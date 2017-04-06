<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">	
<html>
<head>
<%@ include file="header.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
<script type="text/javascript"><!--
function checkALL(){
	var checkValue = document.getElementById("selectAll").checked;
	var fields = document.getElementsByName("showFieldKey");
	for(var i=0; i<fields.length; i++){
		fields[i].checked = checkValue;
	}	
}
//
--></script>
</head>
<body style="overflow: auto;">
<div class="mxt-grid-header">
	<table class="sort ellipsis" width="100%" cellpadding="0" cellspacing="0" dragable="false">
		<THEAD class="mxt-grid-thead">
			<tr class="sort">
				<td align="center"  width="20%"><input type="checkbox" id="selectAll" name="selectAll" onclick="javascript:checkALL();"/></td>
				<td type="String" align="center" width="80%"><fmt:message key="common.name.label" bundle="${v3xCommonI18N}" /></td>
			</tr>
		</THEAD>
		<TBODY class="mxt-grid-tbody">
			<c:forEach items="${props}" var="prop">
				<tr class="sort erow">
					<!--<c:set value="${deeSectionProps[prop.key]==prop.value?'checked':''}" var="checkedStr"></c:set>-->
					<td align=center class="sort" width="20%"><input type="checkbox" ${checkedStr} name="showFieldKey" value="${prop.key}"/></td>
					<td align=center class="sort" width="80%">${prop.value}</td>
				</tr>
			</c:forEach>
		</TBODY>
	</table>
</div>
</body>
<script type="text/javascript">
function OK(){
	var showFieldKeys = document.getElementsByName("showFieldKey");
	var count = 0;
	for(var i=0 ; i<showFieldKeys.length ;i++){
		if(!showFieldKeys[i].checked) count ++;
	}
	if(count == showFieldKeys.length) {
		//alert(v3x.getMessage('sysMgrLang.deeSection_source_selectoneleast'));
		location.reload();
	}
	var showFieldstr = [];
	var allValue = [];
	var resMap = new Properties();
	if(showFieldKeys){
		for(var i=0 ; i<showFieldKeys.length ;i++){
			var key = showFieldKeys[i]
			if(key && key.checked){
				resMap.put(key.value,key.value);
			}
		}
	}
	var keys = resMap.keys();
	for(var i = 0 ; i < keys.size();i++){
		allValue[allValue.length] = keys.get(i);
	}

	if(allValue.length != 0){
		showFieldstr[0] = allValue;
	}else{
		showFieldstr[0] = [];
	}
	return showFieldstr;
}
function sel(v){
	var obj = document.getElementsByName("showFieldKey");
	if(obj){
		for(var i=0 ; i<obj.length ;i++){
			obj[i].checked = v.checked;
		}
	}
}
window.onload = function () {
	var values = parent.paramValue ;
	var showFieldKey = document.getElementsByName("showFieldKey");
	if(showFieldKey && values){
		for(var i=0 ; i<showFieldKey.length ;i++){
			var valuestr  = values.split(",");
			for(var j=0 ; j<valuestr.length ;j++){
				if(valuestr[j]  == showFieldKey[i].value){
					showFieldKey[i].checked = true;
				}
			}
		}
	}else{//默认全选
		for(var i=0 ; i<showFieldKey.length ;i++){
			showFieldKey[i].checked = true;
		}
	}
}
</script>
</html>