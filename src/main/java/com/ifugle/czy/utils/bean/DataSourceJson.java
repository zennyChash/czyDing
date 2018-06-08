package com.ifugle.czy.utils.bean;
import com.alibaba.fastjson.JSONObject;

public class DataSourceJson {
	private String dsID;
	private String dsParams ;
	
	public JSONObject parseJDsParams(){
		return JSONObject.parseObject(this.dsParams);
	}

	public String getDsID() {
		return dsID;
	}

	public void setDsID(String dsID) {
		this.dsID = dsID;
	}

	public String getDsParams() {
		return dsParams;
	}

	public void setDsParams(String dsParams) {
		this.dsParams = dsParams;
	}

}
