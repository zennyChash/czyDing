package com.ifugle.czy.router.bean;

import java.util.List;
import java.util.Map;

public class ProxyRequest {
	private String subURI;
	private String doBefore;
	private String method; //请求的request，默认get
	private Map properties; //请求头的properties
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Map getProperties() {
		return properties;
	}
	public void setProperties(Map properties) {
		this.properties = properties;
	}
	public String getSubURI() {
		return subURI;
	}
	public void setSubURI(String subURI) {
		this.subURI = subURI;
	}
	public String getDoBefore() {
		return doBefore;
	}
	public void setDoBefore(String doBefore) {
		this.doBefore = doBefore;
	}
}
