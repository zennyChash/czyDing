package com.ifugle.czy.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class SaveUserObj {
	private String userid;
	private String saveContent;
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getSaveContent() {
		return saveContent;
	}
	public void setSaveContent(String saveContent) {
		this.saveContent = saveContent;
	}
	public JSONObject parseSaveContent(){
		return JSONObject.parseObject(this.saveContent);
	}
}
