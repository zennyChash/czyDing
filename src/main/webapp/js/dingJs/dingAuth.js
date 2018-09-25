dd.config({
	agentId : _config.agentid,
	corpId : _config.corpId,
	timeStamp : _config.timeStamp,
	nonceStr : _config.nonceStr,
	signature : _config.signature,
	jsApiList : [ 'runtime.info','biz.contact.choose',
			'device.notification.confirm','device.notification.alert',
			'device.notification.prompt','biz.ding.post',
			'biz.util.openLink' ]
});
dd.ready(function() {
	dd.runtime.permission.requestAuthCode({
		corpId : _config.corpId,
		onSuccess : function(info) {
			$.ajax({
				url : 'api/dingUserInfo?doType=authUser&code=' + info.code + '&corpid='
						+ _config.corpId,
				type : 'GET',
				success : function(data, status, xhr) {
					alert(data);
					var info = JSON.parse(data);
				},
				error : function(xhr, errorType, error) {
					logger.e("yinyien:" + _config.corpId);
					alert(errorType + ', ' + error);
				}
			});
		},
		onFail : function(err) {
			alert('fail: ' + JSON.stringify(err));
		}
	});
});