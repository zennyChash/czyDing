package com.ifugle.czyDing.utils;

public class JResponse<T> {
	private String retCode;
	private String retMsg;
	private T retData;
	public JResponse(){
	}
	public JResponse(String rcode,String rmsg,T rdata){
		this.retCode=rcode;
		this.retMsg=rmsg;
		this.retData=rdata;
	}
	public String getRetCode() {
		return retCode;
	}
	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}
	public String getRetMsg() {
		return retMsg;
	}
	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}
	public T getRetData() {
		return retData;
	}
	public void setRetData(T retData) {
		this.retData = retData;
	}
}
