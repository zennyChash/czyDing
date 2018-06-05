package com.ifugle.czy.utils.bean.template;

public class Column {
	private String name;
	private String dataType;
	private int canOrder;
	private int isFilter;
	private String analyzer;
	private String search_analyzer;
	public String getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}
	public String getSearch_analyzer() {
		return search_analyzer;
	}
	public void setSearch_analyzer(String search_analyzer) {
		this.search_analyzer = search_analyzer;
	}
	public int getCanOrder() {
		return canOrder;
	}
	public void setCanOrder(int canOrder) {
		this.canOrder = canOrder;
	}
	public int getIsFilter() {
		return isFilter;
	}
	public void setIsFilter(int isFilter) {
		this.isFilter = isFilter;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
