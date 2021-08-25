package com.ifugle.czyDing.utils.bean.template;

public class FilterField {
	private String name;
	private String refParam; //筛选字段的值来自哪个请求参数
	private String value;
	private String ftype;
	private int dataType;
	
	public String getRefParam() {
		return refParam;
	}
	public void setRefParam(String refParam) {
		this.refParam = refParam;
	}
	public String getFtype() {
		return ftype;
	}
	public void setFtype(String ftype) {
		this.ftype = ftype;
	}
	
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
