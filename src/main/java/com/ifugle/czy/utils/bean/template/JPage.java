package com.ifugle.czy.utils.bean.template;

import java.util.List;

public class JPage {
	private String id ;
	private String name;
	private String desc;
	private String jTemplate;
	private List valuedDs;
	public List getValuedDs() {
		return valuedDs;
	}

	public void setValuedDs(List valuedDs) {
		this.valuedDs = valuedDs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getjTemplate() {
		return jTemplate;
	}

	public void setjTemplate(String jTemplate) {
		this.jTemplate = jTemplate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
