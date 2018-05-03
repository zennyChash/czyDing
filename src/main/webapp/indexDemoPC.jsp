<!DOCTYPE html>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<%
	//String cg = com.ifugle.czy.service.AuthDao.getConfig(request) ;
%>
<html>
<head>
<title>报表查询</title>
<script type="text/javascript" src="libs/zepto.min.js"></script>
<script type="text/javascript" src="dingJs/loadConfig.js"></script>
<script type="text/javascript" src="https://g.alicdn.com/dingding/dingtalk-pc-api/2.7.0/index.js">
<script type="text/javascript" src="dingJs/dingAuth.js">
</script>
<script>
DingTalkPC.ready(function() {
	DingTalkPC.runtime.permission.requestAuthCode({
		corpId : _config.corpId,
		onSuccess : function(info) {
			$.ajax({
				url : 'dingUserInfo?doType=authUser&code=' + info.code + '&corpid='
						+ _config.corpId,
				type : 'GET',
				success : function(data, status, xhr) {
					var info = JSON.parse(data);
					document.getElementById("userName").value = info.username;
					document.getElementById("userId").value = info.userid;
					alert(info.menus);
				},
				error : function(xhr, errorType, error) {
					logger.e("yinyien:" + _config.corpId);
					alert(errorType + ', ' + error);
				}
			});
		},
		onFail : function(err) {
			alert('fail: ' + JSON.stringify(err));
		}
	});
});
</script>
</head>
<body >
	<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0"> 
	<tr> 
	<td align="center"><table width="900" height="300" border="0" cellpadding="0" cellspacing="0" > 
	<tr> 
	<td align="center" >
	<div >
	  <table width="86%" height="300" border="0" cellpadding="0" cellspacing="0">
	    <tr>
	      <td width="51%" height="200">&nbsp;</td>
	      <td width="49%">&nbsp;</td>
	    </tr>
	    <tr>
	      <td height="200">&nbsp;</td>
	      <td ><table width="96%" height="100" border="0" cellpadding="0" cellspacing="0">
	        <tr>
	          <td width="18%" height="37" align="right">用户名：</td>
	          <td width="82%"><input tabindex="1" type="text" id="userName" style="width:150px" maxlength="30" value=""  >
	          </td>
	        </tr>
	        <tr>
	          <td align="right">用户ID：</td>
	          <td><input tabindex="2" id="userId" style="width:150px"/></td>
	        </tr>
	  </table>
	</div>
	
	</td> 
	</tr> 
	</table></td> 
	</tr> 
	</table> 
</body>
</html>
