package com.ifugle.czy.utils;

import java.util.Map;

public class SubmitResult {
	private boolean success = false;
	private Map<String, String> errors;
	private Map<String,String > debug_formPacket;
	private Map<String,String > infos;
	public Map<String, String> getInfos() {
		return infos;
	}
	public void setInfos(Map<String, String> infos) {
		this.infos = infos;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Map<String, String> getErrors() {
		return errors;
	}
	public void setErrors(Map<String, String> errors) {
		this.errors = errors;
	}
	public Map<String, String> getDebug_formPacket() {
		return debug_formPacket;
	}
	public void setDebug_formPacket(Map<String, String> debugFormPacket) {
		debug_formPacket = debugFormPacket;
	}
}
