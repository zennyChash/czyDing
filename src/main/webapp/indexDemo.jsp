<!DOCTYPE html>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<html>
<head>
<title>test</title>
<script type="text/javascript" src="libs/zepto.js">
</script>
<script type="text/javascript">
var _config = {};
Zepto(function($){
// 			p = JSON.stringify({"rptID":"dzjk_srjd","rptParams":{"hybm":"K","ny":"201806"}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/queryData',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
			//"swdjzh":"91330604146165334B","ny":"201805","from":0,"size":10
			p = JSON.stringify({"rptID":"","rptParams":{}});
			$.ajax({
				type: 'post',
				url: 'api/testQueryData',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert("string:"+dt);
				}
			});
			
})

</script>

</head>
<body >
</body>
</html>
