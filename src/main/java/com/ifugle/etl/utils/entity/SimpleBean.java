package com.ifugle.etl.utils.entity;

public class SimpleBean {
	private String bm;
	private String mc;
	private String pid;
	private String href;
	private int level;      //节点所在层次；
    private int isLeaf;     //节点是否是叶子节点；
    private int selected;   //节点是否被选择；
    private String target;  //链接打开的位置
    private int expand;     //描述树节点时，表示是否展开
    private long autoid;
    private int dorder;
    private String nodeIcon;
	public String getNodeIcon() {
		return nodeIcon;
	}
	public void setNodeIcon(String nodeIcon) {
		this.nodeIcon = nodeIcon;
	}
	public long getAutoid() {
		return autoid;
	}
	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}
	public String getBm() {
		return bm;
	}
	public int getDorder() {
		return dorder;
	}
	public void setDorder(int dorder) {
		this.dorder = dorder;
	}
	public void setBm(String bm) {
		this.bm = bm;
	}
	public String getMc() {
		return mc;
	}
	public void setMc(String mc) {
		this.mc = mc;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getIsLeaf() {
		return isLeaf;
	}
	public void setIsLeaf(int isLeaf) {
		this.isLeaf = isLeaf;
	}
	public int getSelected() {
		return selected;
	}
	public void setSelected(int selected) {
		this.selected = selected;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public int getExpand() {
		return expand;
	}
	public void setExpand(int expand) {
		this.expand = expand;
	}
}
