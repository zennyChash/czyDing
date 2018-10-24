package com.ifugle.czy.router;

import java.util.List;

public interface IBeforeResponse {
	public Object process(String serviceName,String reqMethod,List results);
}
