package com.ifugle.czyDing.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class QueryUserObj {
	private String userid;
	private String queryContent;
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getQueryContent() {
		return queryContent;
	}
	public void setQueryContent(String queryContent) {
		this.queryContent = queryContent;
	}
	
	public JSONObject parseQueryContent(){
		return JSONObject.parseObject(this.queryContent);
	}
}
