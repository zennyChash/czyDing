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
		    var p = JSON.stringify({"rptID":"dzjk","optionParams":["pYearMonth","pDistrict"]})
		    $.ajax({
				type: 'post',
				url: 'api/paramOptions',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
				}
		    });
		   	p = JSON.stringify({"rptID":"dzjk","rptParams":{"thisYear":"201806","lastYear":"201704"}})
		    $.ajax({
				type: 'post',
				url: 'api/queryData',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
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
