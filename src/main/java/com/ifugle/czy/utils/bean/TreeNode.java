package com.ifugle.czy.utils.bean;

import java.util.List;

public class TreeNode {
	private String id;
	private String pid;
	private String text;
	private boolean leaf;
	private String cls = "file";
	private boolean loaded;
	private boolean expanded;
	private String href;
	private String hrefTarget;
	private int disabled = 0;

	public List getChildren() {
		return children;
	}

	public void setChildren(List children) {
		this.children = children;
	}

	private String creator;
	private List children;

	/*public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}*/
	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getDisabled() {
		return disabled;
	}

	public void setDisabled(int disabled) {
		this.disabled = disabled;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCls() {
		return cls;
	}

	public void setCls(String cls) {
		this.cls = cls;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean getExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getHrefTarget() {
		return hrefTarget;
	}

	public void setHrefTarget(String hrefTarget) {
		this.hrefTarget = hrefTarget;
	}
}
