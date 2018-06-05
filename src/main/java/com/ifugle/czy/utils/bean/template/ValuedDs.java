package com.ifugle.czy.utils.bean.template;

import java.util.List;

//根据数据源定义的描述，在运行时封装好查询、排序等条件，用于查询具体数据集。
public class ValuedDs {
	//引用的数据源的名称。
	private String refDtSrc; 
	//记录集使用的名称，应符合变量名命名规则；
	//对相同的数据源（refDtSr），使用不同参数，形成不同name的记录集。
	private String name;  
	private List filterFlds;
	private List orderByFlds;
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
