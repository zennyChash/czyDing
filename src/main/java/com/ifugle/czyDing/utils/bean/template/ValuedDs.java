package com.ifugle.czyDing.utils.bean.template;

import java.util.List;
import java.util.Map;

//根据数据源定义的描述，在运行时封装好查询、排序等条件，用于查询具体数据集。
public class ValuedDs {
	//引用的数据源的名称。
	private String refDtSrc; 
	//记录集使用的名称，应符合变量名命名规则；
	//对相同的数据源（refDtSr），使用不同参数，形成不同name的记录集。
	private String name;  
	private boolean paging;
	private String startParam;
	private String sizeParam;
	private String fields;
	private List filterFlds;
	private List orderByFlds;
	private Map filterFldsMap;
	
	public Map getFilterFldsMap(){
		return filterFldsMap;
	}
	public void setFilterFldsMap(Map filterFldsMap){
		this.filterFldsMap= filterFldsMap;
	}
	public String getFields() {
		return fields;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	public boolean isPaging() {
		return paging;
	}
	public void setPaging(boolean paging) {
		this.paging = paging;
	}
	public String getStartParam() {
		return startParam;
	}
	public void setStartParam(String startParam) {
		this.startParam = startParam;
	}
	public String getSizeParam() {
		return sizeParam;
	}
	public void setSizeParam(String sizeParam) {
		this.sizeParam = sizeParam;
	}
	public String getRefDtSrc() {
		return refDtSrc;
	}
	public void setRefDtSrc(String refDtSrc) {
		this.refDtSrc = refDtSrc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List getFilterFlds() {
		return filterFlds;
	}
	public void setFilterFlds(List filterFlds) {
		this.filterFlds = filterFlds;
	}
	public List getOrderByFlds() {
		return orderByFlds;
	}
	public void setOrderByFlds(List orderByFlds) {
		this.orderByFlds = orderByFlds;
	}
	
}
