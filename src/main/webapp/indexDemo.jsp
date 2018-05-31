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
// 	$.ajax({
// 		type: 'GET',
// 		url: 'api/getDingConfig',
// 		data: { doType: '1' },
// 		dataType: 'json',
// 		success: function(data){
		    //alert(data.retData.agentid);
		    //_config = data;
// 		    var p = JSON.stringify({"rptID":"dzjk","optionParams":["pYearMonth","pDistrict"]})
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/paramOptions',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
		   	//p = JSON.stringify({"rptID":"dzjk","rptParams":{"thisYear":"201806","lastYear":"201704"}})
// 		   	p = JSON.stringify({"rptID":"dhcx_sshistory","rptParams":{"swdjzh":"201804"}})
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/queryData',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				},
// 				error: function(xhr, type){
// 				    alert('Ajax error!'+type)
// 				}
// 		    });
		   	
//  			p = JSON.stringify({"rptid":"enbasic","rptparams":{"sugType":"completion"}});
// 		    $.ajax({
// 				type: 'post',
// 				//url: 'api/deleteIndex',
// 				url: 'api/buildIndex',
// 				data: p,
// 				headers: {
// 			        "content-type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
// 		    p = JSON.stringify({"rptid":"enbasic","rptparams":{"sugType":""}});
// 		    $.ajax({
// 				type: 'post',
// 				//url: 'api/deleteIndex',
// 				url: 'api/buildIndex',
// 				data: p,
// 				headers: {
// 			        "content-type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
		    
// 		 	p = JSON.stringify({"rptID":"enbasic","rptParams":{"sugType":"completion","searchKey":"金龙","from":10,"size":10}});
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
// 		    p = JSON.stringify({"rptID":"enBasic","rptParams":{"sugType":"","searchKey":"百货","from":0,"size":10}});
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
// 		    p = JSON.stringify({"userid":"manager431","saveContent":{"saveType":"myFavorite","saveObj":{"swdjzh":"3302xxxx","mc":"浙江广厦"}}});
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/saveUserInfo',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
		    
		    p = JSON.stringify({"userid":"manager431","deleteContent":{"deleteType":"myFavorite","deleteObj":[{"swdjzh":"3309xxxx"},{"swdjzh":"3302xxxx"}]}});
		    $.ajax({
				type: 'post',
				url: 'api/deleteUserInfo',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert(dt);
				}
		    });
		    
// 		    p = JSON.stringify({"userid":"manager431","queryContent":{"qType":"myFavorite","params":{"start":0,"limit":5}}});
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/getUserInfo',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    }, 
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
		    
//		},
// 		error: function(xhr, type){
// 		    alert('Out Ajax error!'+type)
// 		}
// 	});
})

</script>

</head>
<body >
</body>
</html>
