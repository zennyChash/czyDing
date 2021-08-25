package com.ifugle.czyDing.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class FtsParam {
	private String idx;
	private String params ;
	
	public String getIdx() {
		return idx;
	}
	public void setIdx(String idx) {
		this.idx = idx;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}

	public JSONObject parseFtsParams(){
		return JSONObject.parseObject(this.params);
	}
}
