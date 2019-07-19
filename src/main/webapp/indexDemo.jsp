<!DOCTYPE html>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<html>
<head>
<title>test</title>
<script type="text/javascript" src="libs/zepto.js">
</script>
<script type="text/javascript" src="js/md5.js">
</script>
<script type="text/javascript">
// var _config = {};
// Zepto(function($){
// 			p = JSON.stringify({"rptID":"q_xnjk_decrypt_rdpro","rptParams":{"ny":201701,"from":0,"size":10}});
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
// 			$.ajax({
// 				type: 'get',
// 				url: 'api/validateLogin',
// 				data: {"pswd":"1203",userid:'manager431'},
// 				headers: {
// 			        "Content-Type": "application/x-www-form-urlencoded"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			}); 
// 			p = JSON.stringify({"rptID":"rds_test2","rptParams":{"did":"2","itemmc":"资","lid":"100","czfpbms":"01,03","ny":201801,"from":0,"size":10}});
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
// 			p = JSON.stringify({"rptID":"sryb","rptParams":{"zwrq":"201804","from":0,"size":10}});
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
/* 			p = JSON.stringify({"rptID":"enbasic","rptParams":{"searchKey":"公司","filterBy":{"czfpbm":"330108099"},"from":0,"size":10}});
		    $.ajax({
				type: 'post',
				url: 'api/searchForWord',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
				}
		    }); */
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
//  			$.ajax({
// 				type: 'get',
// 				url: 'api/getUserConfigTest',
// 				data: {"userid":"manager431"},
// 				headers: {
// 			        "Content-Type": "application/x-www-form-urlencoded"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			}); 
			/* $.ajax({
				type: 'get',
				url: 'api/getUserMenusTest',
				data: {"userid":"manager431"},
				headers: {
			        "Content-Type": "application/x-www-form-urlencoded"
			    },
				success: function(dt){
					alert("string:"+dt);
				}
 			}); */
// 			p = JSON.stringify({"userid":"manager431","saveContent":{"saveType":"myPswd","saveObj":{"pswd_on":"1","pswd_mode":"1","pswd":"1203"}}});
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

// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getMsgAfterCheck","svParams":{"userid":"manager431","lid":"882"}});
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
			/* p = JSON.stringify({"msg":"测试：神锋无影！","users":"manager431"});
			$.ajax({
				type: 'post',
				url: 'api/sendDingMsg',
				data: p,
				headers: {
					"Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert("string:"+dt);
				}
			});
			 */
/* 			 $.ajax({
					url : 'api/canAccessTo?mid=xmsp&code=ddddddd&corpid=sfasfsafsadd',
					type : 'GET',
					success : function(data, status, xhr) {
						alert(data);
						var info = JSON.parse(data);
					},
					error : function(xhr, errorType, error) {
						logger.e("yinyien:" + _config.corpId);
						alert(errorType + ', ' + error);
					}
				}); */
				
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
			
// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getAppListDetails","svParams":{"userid":"manager431","sort":"","dir":"","start":0,"limit":10,"params":{"lid":"841"}}});
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
// 			p = JSON.stringify({"reqService":"czfc","reqMethod":"appListCheck","svParams":{"userid":"manager431","doType":"0","lid":"841","remark":"xxxx"}});
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
/* 			p = JSON.stringify({"reqService":"czfc","reqMethod":"getApprovalLists","svParams":{"userid":"manager431","state":"0","sort":"","dir":"","start":0,"limit":10,"params":{}}});
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
			}); */
			/* $.ajax({
				type: 'GET',
				url: 'manage/getUsersAllMenus',
				data: {"userid":"manager431"},
				headers: {
					"Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
				}
			}); */
// 			$.ajax({
// 				type: 'GET',
// 				url: 'manage/getUsersDfMenus',
// 				data: {"userid":"manager431"},
// 				headers: {
// 					"Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert("string:"+dt);
// 				}
// 			});
			p = JSON.stringify({"dataID":"annualReport","params":{"id":"1232777414"}});
 			$.ajax({
				type: 'post',
				url: 'api/queryTyc',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
				}
			}); 
			/* p = JSON.stringify({"logType":"user","logId":"login","userid":"admin","log":{"event":"changePswd"}});
 			$.ajax({
				type: 'post',
				url: 'api/log',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
				}
			});  */
//});
  
</script>

</head>
<body >
</body>
</html>
