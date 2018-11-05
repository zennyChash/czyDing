package com.ifugle.etl.entity.task;

import java.util.List;
import java.util.Map;

import com.ifugle.etl.entity.base.Task;

public class RequestTask extends Task{
	private String method="POST";
	private String uri;
	private int socketTimeout;
	private int connTimeout;
	private String beforeReq;
	private String beforeResponse;
	private Map props;
	private String mode = "asyn";
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public int getConnTimeout() {
		return connTimeout;
	}
	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}
	public String getBeforeReq() {
		return beforeReq;
	}
	public void setBeforeReq(String beforeReq) {
		this.beforeReq = beforeReq;
	}
	public String getBeforeResponse() {
		return beforeResponse;
	}
	public void setBeforeResponse(String beforeResponse) {
		this.beforeResponse = beforeResponse;
	}
	public Map getProps() {
		return props;
	}
	public void setProps(Map props) {
		this.props = props;
	}
}
