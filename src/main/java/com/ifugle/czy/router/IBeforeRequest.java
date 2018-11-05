package com.ifugle.czy.router;

import com.ifugle.czy.router.bean.ProxyRequest;
import com.ifugle.czy.router.bean.ProxyResponse;

public interface IBeforeRequest {
	public String[] process(String serviceName,String reqMethod,ProxyRequest reqTmp,String svParams, String userid);
}
