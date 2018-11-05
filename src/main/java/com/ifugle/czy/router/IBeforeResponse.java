package com.ifugle.czy.router;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.router.bean.ProxyResponse;
import com.ifugle.czy.utils.JResponse;

public interface IBeforeResponse {
	public JResponse process(String serviceName,String reqMethod,ProxyResponse resonpseTmp,JSONObject results);
}
