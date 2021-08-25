package com.ifugle.czyDing.utils.bean.template;

public class Column {
	private String name;
	private String fldType;
	private int canOrder;
	private int isFilter;
	private int decipher;
	private String algorithm="AES";
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
	public int getDecipher() {
		return decipher;
	}
	public void setDecipher(int decipher) {
		this.decipher = decipher;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFldType() {
		return fldType;
	}
	public void setFldType(String fldType) {
		this.fldType = fldType;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
}
