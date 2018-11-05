package com.ifugle.czy.router.bean;

import java.util.List;
import java.util.Map;

public class ProxyRequest {
	private String subURI;
	private String doBefore;
	private String method; //请求的request，默认get
	private String socketTimeout="10000";
	private String connTimeout="10000";
	private Map properties; //请求头的properties
	
	//如果前端一个请求，拆分成多个服务请求，即一个method包含多个request，
	//每个request的返回结果要拼凑，该属性指明返回前端时，该请求的结果是哪个属性
	private String returnProperty;
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getReturnProperty() {
		return returnProperty;
	}
	public void setReturnProperty(String returnProperty) {
		this.returnProperty = returnProperty;
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
	public String getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(String socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public String getConnTimeout() {
		return connTimeout;
	}
	public void setConnTimeout(String connTimeout) {
		this.connTimeout = connTimeout;
	}
}
