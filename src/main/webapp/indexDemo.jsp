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
// 			p = JSON.stringify({"rptID":"nsdh_details","rptParams":{"taxLimit":"1","ny":"201803","from":30,"size":10}});
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
// 			p = JSON.stringify({"rptID":"enbasic","rptParams":{"searchKey":"顺海","filterBy":{"czfpbm":"330604002"},"from":0,"size":10}});
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/searchForWord',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
			//330604001 "swdjzh":"91330604146165334B","ny":"201805","from":0,"size":10
// 			p = JSON.stringify({"rptID":"","rptParams":{}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/testQueryData',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
			$.ajax({
				type: 'post',
				url: 'api/getUserConfigTest',
				data: {"userid":"0664293747747028"},
				headers: {
			        "Content-Type": "application/x-www-form-urlencoded"
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
