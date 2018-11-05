package com.ifugle.etl.entity;


public class ScheduledTask{
	private String taskId ;
	private String onSuccess;
	private String onFail;
	private int onFailRetry;
	private int disabled;
	private ScheduledTask taskOnSuccess;
	private ScheduledTask taskOnFail;
	
	public ScheduledTask getTaskOnSuccess() {
		return taskOnSuccess;
	}
	public void setTaskOnSuccess(ScheduledTask taskOnSuccess) {
		this.taskOnSuccess = taskOnSuccess;
	}
	public ScheduledTask getTaskOnFail() {
		return taskOnFail;
	}
	public void setTaskOnFail(ScheduledTask taskOnFail) {
		this.taskOnFail = taskOnFail;
	}
	private ScheduledTask pTask;
	public ScheduledTask getpTask() {
		return pTask;
	}
	public void setpTask(ScheduledTask pTask) {
		this.pTask = pTask;
	}
	public int getOnFailRetry() {
		return onFailRetry;
	}
	public void setOnFailRetry(int onFailRetry) {
		this.onFailRetry = onFailRetry;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getOnSuccess() {
		return onSuccess;
	}
	public void setOnSuccess(String onSuccess) {
		this.onSuccess = onSuccess;
	}
	public String getOnFail() {
		return onFail;
	}
	public void setOnFail(String onFail) {
		this.onFail = onFail;
	}
	public int getDisabled() {
		return disabled;
	}
	public void setDisabled(int disabled) {
		this.disabled = disabled;
	}
}
