package com.ifugle.czy.router.bean;

import com.alibaba.fastjson.JSONObject;

public class JRemoteParams {
	private String reqService;
	private String reqMethod;
	private String svParams;
	public String getReqService() {
		return reqService;
	}
	public void setReqService(String reqService) {
		this.reqService = reqService;
	}
	public String getReqMethod() {
		return reqMethod;
	}
	public void setReqMethod(String reqMethod) {
		this.reqMethod = reqMethod;
	}
	public String getSvParams() {
		return svParams;
	}
	public void setSvParams(String svParams) {
		this.svParams = svParams;
	}
	public JSONObject parseJRemoteParams(){
		return JSONObject.parseObject(this.svParams);
	}
}
