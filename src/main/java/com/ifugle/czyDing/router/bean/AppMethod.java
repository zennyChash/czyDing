package com.ifugle.czyDing.router.bean;

import java.util.List;
import java.util.Map;

public class AppMethod {
	private String name;
	private List requests;
	private Map requestsMap;
	private ProxyResponse response;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List getRequests() {
		return requests;
	}
	public void setRequests(List requests) {
		this.requests = requests;
	}
	public Map getRequestsMap() {
		return requestsMap;
	}
	public void setRequestsMap(Map requestsMap) {
		this.requestsMap = requestsMap;
	}
	public ProxyResponse getResponse() {
		return response;
	}
	public void setResponse(ProxyResponse response) {
		this.response = response;
	}
}
