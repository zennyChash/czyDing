package com.ifugle.czyDing.utils.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class RptDataJson {
	private String rptID;
	private String rptParams ;
	private String optionParams;
	
	public String getOptionParams() {
		return optionParams;
	}
	public void setOptionParams(String optionParams) {
		this.optionParams = optionParams;
	}

	public String getRptParams() {
		return rptParams;
	}

	public void setRptParams(String rptParams) {
		this.rptParams = rptParams;
	}
	public JSONObject parseJRptParams(){
		return JSONObject.parseObject(this.rptParams);
	}
	public JSONArray parseJOptionParams(){
		return JSONArray.parseArray(this.optionParams);
	}
	public String getRptID() {
		return rptID;
	}

	public void setRptID(String rptID) {
		this.rptID = rptID;
	}
}
