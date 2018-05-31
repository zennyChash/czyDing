package com.ifugle.czy.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class DeleteUserObj {
	private String userid;
	private String deleteContent;
	
	public String getUserid() {
		return userid;
	}
	public String getDeleteContent() {
		return deleteContent;
	}
	public void setDeleteContent(String deleteContent) {
		this.deleteContent = deleteContent;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public JSONObject parseDeleteContent(){
		return JSONObject.parseObject(this.deleteContent);
	}
}
