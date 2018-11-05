package com.ifugle.etl.entity;
import com.ifugle.etl.entity.base.ETLObject;
public class ConnectInfo extends ETLObject{
	private int type;
	private String charset;
	private String password;
	private String username;
	private String conurl;
	private String driver_class;
	private int minIdle;
	private int maxIdle;
	private long maxWait;
	private int maxActive;
	private boolean removeAbandoned;
	private int removeAbandonedTimeout;
	private int initialSize;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getConurl() {
		return conurl;
	}
	public void setConurl(String conurl) {
		this.conurl = conurl;
	}
	public String getDriver_class() {
		return driver_class;
	}
	public void setDriver_class(String driverClass) {
		driver_class = driverClass;
	}
	public int getMinIdle() {
		return minIdle;
	}
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
	public int getMaxIdle() {
		return maxIdle;
	}
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}
	public long getMaxWait() {
		return maxWait;
	}
	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}
	public int getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}
	public boolean isRemoveAbandoned() {
		return removeAbandoned;
	}
	public void setRemoveAbandoned(boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
	}
	public int getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}
	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		this.removeAbandonedTimeout = removeAbandonedTimeout;
	}
	public int getInitialSize() {
		return initialSize;
	}
	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}
}
