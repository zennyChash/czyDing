package com.ifugle.etl.entity;

import java.util.List;
import java.util.Map;

import com.ifugle.etl.entity.component.TriggerInfo;

public class ScheduledJob {
	private String jobId;
	private String jobMc;
	private int disabled;
	private List parameters;
	private Map paramMap;
	private TriggerInfo trigger;
	private Map taskMap;
	private ScheduledTask headTask;
	private int isMultiTask;
	
	public String getJobMc() {
		return jobMc;
	}
	public void setJobMc(String jobMc) {
		this.jobMc = jobMc;
	}
	public int getIsMultiTask() {
		return isMultiTask;
	}
	public void setIsMultiTask(int isMultiTask) {
		this.isMultiTask = isMultiTask;
	}
	public ScheduledTask getHeadTask() {
		return headTask;
	}
	public void setHeadTask(ScheduledTask headTask) {
		this.headTask = headTask;
	}
	public Map getTaskMap() {
		return taskMap;
	}
	public void setTaskMap(Map taskMap) {
		this.taskMap = taskMap;
	}
	public TriggerInfo getTrigger() {
		return trigger;
	}
	public void setTrigger(TriggerInfo trigger) {
		this.trigger = trigger;
	}
	public List getParameters() {
		return parameters;
	}
	public void setParameters(List parameters) {
		this.parameters = parameters;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public Map getParamMap() {
		return paramMap;
	}
	public void setParamMap(Map paramMap) {
		this.paramMap = paramMap;
	}
	public int getDisabled() {
		return disabled;
	}
	public void setDisabled(int disabled) {
		this.disabled = disabled;
	}
}
