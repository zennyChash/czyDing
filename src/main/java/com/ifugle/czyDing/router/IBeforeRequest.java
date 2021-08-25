package com.ifugle.czyDing.router;

import com.ifugle.czyDing.router.bean.ProxyRequest;
import com.ifugle.czyDing.router.bean.ProxyResponse;

public interface IBeforeRequest {
	public String[] process(String serviceName,String reqMethod,ProxyRequest reqTmp,String svParams, String userid);
}
