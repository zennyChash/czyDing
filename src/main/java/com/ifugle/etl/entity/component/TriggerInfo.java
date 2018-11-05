package com.ifugle.etl.entity.component;

public class TriggerInfo {
	private int type;
	private int repeat;
	private int interval;
	private int intervalUnit;
	private String startTime;
	
	public String getStartTime(){
		return startTime;
	}
	public void setStartTime(String startTime){
		this.startTime = startTime;
	}
	public int getIntervalUnit() {
		return intervalUnit;
	}
	public void setIntervalUnit(int intervalUnit) {
		this.intervalUnit = intervalUnit;
	}
	private String expression;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getRepeat() {
		return repeat;
	}
	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
}
