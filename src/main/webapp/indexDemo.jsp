<!DOCTYPE html>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<html>
<head>
<title>test</title>
<script type="text/javascript">
//在此拿到jsAPI权限验证配置所需要的信息
var _config = <%=com.ifugle.czy.service.AuthDao.getConfig(request)%>;
var _config = {};
$.ajax({
	url : '/api/dingUserInfo?doType=getDingConfig',
	type : 'GET',
	success : function(data, status, xhr) {
		_config = JSON.parse(data);
	},
	error : function(xhr, errorType, error) {
		logger.e("corpId:" + _config.corpId);
		alert(errorType + ', ' + error);
	}
});
</script>
<script type="text/javascript" src="libs/zepto.min.js">
</script>
<script type="text/javascript" src="http://g.alicdn.com/dingding/open-develop/1.6.9/dingtalk.js"> 
</script>
<script type="text/javascript" src="dingJs/dingAuth.js">
</script>
</head>
<body >
</body>
</html>
