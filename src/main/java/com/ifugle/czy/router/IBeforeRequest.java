package com.ifugle.czy.router;

import com.ifugle.czy.router.bean.ProxyRequest;

public interface IBeforeRequest {
	public String[] process(String serviceName,String reqMethod,ProxyRequest req,String svParams, String userid);
}
