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
// 			p = JSON.stringify({"rptID":"dzjk_srjd","rptParams":{"zwrq":"20180430"}});
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
//  			$.ajax({
// 				type: 'get',
// 				url: 'api/getMyMenusTest',
// 				data: {"userid":"03526769651114336"},
// 				headers: {
// 			        "Content-Type": "application/x-www-form-urlencoded"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			}); 
			
// 			$.ajax({
// 				type: 'get',
// 				url: 'api/getUserMenusTest',
// 				data: {"userid":"manager431"},
// 				headers: {
// 			        "Content-Type": "application/x-www-form-urlencoded"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
// 			p = JSON.stringify({"userid":"0352676957760910","saveContent":{"saveType":"myMenus","saveObj":{"menus":[{"id":"srjd"},{"id":"dhss"}]}}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/saveUserInfoTest',
// 				data: p,
// 				headers: {
// 					"Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getSingleAppList","svParams":{"userid":"manager431","lid":"882","state":"1"}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/remoteService',
// 				data: p,
// 				headers: {
// 					"Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getAppListCheckInfo","svParams":{"userid":"manager431","lid":"882"}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/remoteService',
// 				data: p,
// 				headers: {
// 					"Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getAppListDetails","svParams":{"userid":"manager431","sort":"","dir":"","start":0,"limit":10,"params":{"lid":"843"}}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/remoteService',
// 				data: p,
// 				headers: {
// 					"Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
			p = JSON.stringify({"reqService":"czfc","reqMethod":"appListCheck","svParams":{"userid":"manager431","doType":"1","lid":"804","remark":"xxxx"}});
			$.ajax({
				type: 'post',
				url: 'api/remoteService',
				data: p,
				headers: {
					"Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert("string:"+dt);
				}
			});
// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getApprovalLists","svParams":{"userid":"manager431","state":"1","sort":"","dir":"","start":0,"limit":10,"params":{}}});
// 			$.ajax({
// 				type: 'post',
// 				url: 'api/remoteService',
// 				data: p,
// 				headers: {
// 					"Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
})

</script>

</head>
<body >
</body>
</html>
