var _config = {};
$.ajax({
	url : '/api/dingUserInfo?doType=getDingConfig',
	type : 'GET',
	success : function(data, status, xhr) {
		_config = JSON.parse(data);
	},
	error : function(xhr, errorType, error) {
		logger.e("corpId:" + _config.corpId);
		alert(errorType + ', ' + error);
	}
});