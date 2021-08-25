package com.ifugle.czyDing.router;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.router.bean.ProxyResponse;
import com.ifugle.czyDing.utils.JResponse;

public interface IBeforeResponse {
	public JResponse process(String serviceName,String reqMethod,ProxyResponse resonpseTmp,JSONObject results);
}
