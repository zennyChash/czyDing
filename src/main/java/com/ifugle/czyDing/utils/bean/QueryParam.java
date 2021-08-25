package com.ifugle.czyDing.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class QueryParam {
	private String dataID;
	private String params ;
	
	public String getDataID() {
		return dataID;
	}
	public void setDataID(String dataID) {
		this.dataID = dataID;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}

	public JSONObject parseQParams(){
		return JSONObject.parseObject(this.params);
	}
}
