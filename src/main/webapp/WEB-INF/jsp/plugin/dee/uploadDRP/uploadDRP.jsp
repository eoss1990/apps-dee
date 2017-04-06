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
<title>Insert title here</title>
<style>
.uploaddiv-1 {
	position: absolute;
	top: 20%;
	left: 27%;
	border: 1px solid;
	background: #ededed;
	color: silver;
	width: 400px;
	height: 250px;
}

.uploaddiv2 {
	position: absolute;
	top: 15%;
	left: 1%;
	border: 1px solid;
	background: white;
	color: silver;
	width: 98%;
	height: 150px;
}

.uploaddiv3 {
	position: absolute;
	bottom: 10%;
	left: 1%;
}

.uploaddiv4 {
	position: relative;
	top: 20%;
}

#myFile{
	width:60px; 
	position:absolute; 
	left:132px; 
	filter:alpha(opacity:0);
	opacity:0;
	cursor:pointer;
	z-index: 1;
}
 
#btBrow{
	width:60px;
	
}
#upload{
	width:60px;
}

.updiv{
	position:absolute;
	top:0px;
	left:200px;
}
</style>
<script>
    var menus = [ {
        name : "<fmt:message key='dee.pluginMainMenu.label'/>"
    }, {
        name : "<fmt:message key='dee.deploy.label'/>",
        url : "/deeDeployDRPController.do?method=show"
    } ];
    updateCurrentLocation(menus);
    
    $(function(){
     
       $("#myFile").change(function() {
           var file=$("#myFile").val();
           $("#tempFile").val(file);
          
       });
       
       $("#upload").click(function() {
           $("#formFile").submit();
       });
     	
    });
    
    
    
</script>

<c:if test="${!empty retMsg }">
	<script type="text/javascript">
        $.infor({
            msg : "${retMsg}",
            ok_fn : function() {
                location.href = _ctxPath
                        + "/deeDeployDRPController.do?method=show";
            },
            close_fn : function() {
                location.href = _ctxPath
                        + "/deeDeployDRPController.do?method=show";
            }
        });
    </script>
</c:if>
</head>
<body>
	<div class="uploaddiv-1">
		<table>
			<tr>
				<td><font color="green"> <fmt:message
						key='dee.deploy.label' /></font></td>
			</tr>

			<tr>
			
					<div class="uploaddiv2">
						<div class="uploaddiv4">
							<form method="post" id="formFile" action="${urlDeeDeploy}?method=deployDRP"
								enctype="multipart/form-data">
									
									
									<div style="width:220px;">
									<input type="text" id="tempFile">
									<input type="button" value="<fmt:message key='dee.uploadDRP.brow' />" id="btBrow"  class="common_button common_button_gray" />
									<input type="file" id="myFile" name="drpFile"/>
									</div>
									<div class="updiv">
									<input type="button" value="<fmt:message key='dee.uploadDRP.upload' />" name="upload" id="upload" class="common_button common_button_gray" />
									</div>
							</form>
						</div>
					</div>
				</td>
			</tr>
			<tr>

				<td>
					<div class="uploaddiv3">
						<font size="-1" color="green">*<fmt:message key='dee.uploadDRP.select' /></font>
					</div>
				</td>
			</tr>

		</table>
	</div>

</body>
</html>