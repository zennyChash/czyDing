package com.ifugle.etl.entity;

import java.util.List;
import java.util.Map;

import com.ifugle.etl.entity.base.ETLObject;

public class SchedulerInfo extends ETLObject{
	private List jobs;
	private Map jobsMap;
	
	public List getJobs(){
		return this.jobs;
	}
	public Map getJobsMap() {
		return jobsMap;
	}
	public void setJobsMap(Map jobsMap) {
		this.jobsMap = jobsMap;
	}
	public void setJobs(List jobs){
		this.jobs=jobs;
	}
}
