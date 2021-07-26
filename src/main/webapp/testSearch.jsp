<!DOCTYPE html>
<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<html>
<head>
<title>test</title>
<script type="text/javascript" src="libs/zepto.js"></script>
<script type="text/javascript">
function doSearch(){
	Zepto(function($){
		//var idx=document.getElementById('idx').getValue();
		var myselect = document.getElementById("idx");
		var index = myselect.selectedIndex;
		var idx = myselect.options[index].value;
		var searchKey = $('#searchKey').val();
	    //var searchKey=document.getElementById('searchKey').getValue();
	    var searchSize = 10;
	    var sp = {"searchKey":searchKey,"searchSize":searchSize};
	    p = JSON.stringify({"idx":idx,"params":sp});
		$.ajax({
			type: 'post',
			url: 'api/ftsWords',
			data: p,
			headers: {
				"Content-Type": "application/json;charset=utf-8"
		    },
			success: function(result){
				var obj=JSON.parse(result);
        	   	if(obj.retCode==0){
        		   	var lst = obj.retData.matches;
        		   	document.getElementById('result').innerHTML ="";
        		   	for(var i=0;i<lst.length;i++) {
        			   	var en = document.createElement("p");  
        			   	en.innerHTML=lst[i].mc; 
        			   	document.getElementById('result').appendChild(en);  
        		   	}
        	   	}else{
        	   		alert(obj.retMsg);
        	   	}
			}
		});
	});		
}  
function loadIdx(){
	Zepto(function($){
		$.ajax({
	        url: "api/getOraIdx",
	        type: 'POST',
	        data: '',
	        async: false,
	        cache: false,
	        contentType: false,
	        processData: false,
	        success: function (result) {
	           	if (result) {
	        	   	var obj=JSON.parse(result);
	        	   	if(obj.retCode==0){
	        		   	var ops = obj.retData;
	        		   	$('#idx').innerHTML ="";
	        		   	for(var i=0;i<ops.length;i++) {
	        			   	var option = document.createElement("option");  
	        			   	option.value = ops[i].idx;  
	        			   	option.innerHTML = ops[i].mc;  
	        			   	document.getElementById('idx').appendChild(option);  
	        			}  
	        	   	}
	        	   
	           	} else {
	               	$.messager.alert('提示：','获取索引失败！');  
	           	}
	        }
	    });
	});	
}
</script>
</head>
<body onload="loadIdx();">
	<form>
		<p>匹配内容：<select id="idx" style="width:120px;"></select></p>
		<p>关键词：&nbsp;&nbsp;&nbsp;
		<input type="text" id="searchKey" value="" onkeyup="doSearch();" style="width:120px;"/>
		<input type="button" onclick="doSearch();" value="搜索"/></p>
	</form>
	<div id="result"></div>
</body>
</html>
