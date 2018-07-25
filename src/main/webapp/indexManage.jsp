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
		   	//p = JSON.stringify({"rptID":"dzjk","rptParams":{"thisYear":"2018","lastYear":"2017"}})
// 		   	p = JSON.stringify({"rptID":"nsdh_sssr","rptParams":{"from":"0","ny":"201805","size":"10"}})
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/queryDataDB',
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
// 		    p = JSON.stringify({"rptid":"enbasic","rptparams":{}});
// 		    $.ajax({
// 				type: 'post',
// 				//url: 'api/deleteIndex',
// 				url: 'api/deleteESIndex',
// 				data: p,
// 				headers: {
// 			        "content-type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
		    
// 		 	p = JSON.stringify({"rptID":"enbasic","rptParams":{"searchKey":"丰惠月丰 手套","from":0,"size":10}});
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
		    
// 		    p = JSON.stringify({"userid":"manager431","deleteContent":{"deleteType":"myFavorite","deleteObj":[{"swdjzh":"3309xxxx"},{"swdjzh":"3302xxxx"}]}});
// 		    $.ajax({
// 				type: 'post',
// 				url: 'api/deleteUserInfo',
// 				data: p,
// 				headers: {
// 			        "Content-Type": "application/json;charset=utf-8"
// 			    },
// 				success: function(dt){
// 					alert(dt);
// 				}
// 		    });
		    
// 		    p = JSON.stringify({"userid":"manager431","queryContent":{"qType":"myFavorite","params":{"from":0,"limit":5}}});
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
			p = JSON.stringify({"dsID":"en_year","dsParams":{"reMapping":true,"deleteOldData":true}});
			$.ajax({
				type: 'post',
				url: 'api/indexData2ES',
				data: p,
				headers: {
			        "Content-Type": "application/json;charset=utf-8"
			    },
				success: function(dt){
					alert("string:"+dt);
				}
			});
			
// 			p = JSON.stringify({"rptID":"dzjk","rptParams":{"thisYear":"2018","lastYear":"2017","start":0,"size":12}});
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
// 			p = JSON.stringify({"rptID":"dhcx_df_ny","rptParams":{"swdjzh":"91330604146165334B","from":0,"size":10}});
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
