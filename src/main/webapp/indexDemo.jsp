<!DOCTYPE html>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<html>
<head>
<title>test</title>
<script type="text/javascript" src="libs/zepto.js">
</script>
<script type="text/javascript">
//在此拿到jsAPI权限验证配置所需要的信息
var _config = {};
Zepto(function($){
	$.ajax({
		  type: 'GET',
		  url: 'api/getDingConfig',
		  data: { doType: '1' },
		  dataType: 'json',
		  success: function(data){
		    alert(data.retData.agentid);
		    _config = data;
		    $.ajax({
				  type: 'post',
				  url: 'api/queryData/dzjk',
				  data: { rptParams:JSON.stringify({thisYear:'2018',lastYear:"2017"}) ,code: 'ddd12345',corpid:'ccddd5678' },
				  dataType: 'json',
				  success: function(dt){
				    alert(dt.retData);
				  }
		    });
		  },
		  error: function(xhr, type){
		    alert('Ajax error!'+type)
		  }
	});
})

</script>

</head>
<body >
</body>
</html>
