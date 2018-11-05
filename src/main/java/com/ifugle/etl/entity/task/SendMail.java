package com.ifugle.etl.entity.task;

import java.util.List;

import com.ifugle.etl.entity.base.Task;

public class SendMail extends Task{
	private String host;
	private String username;
	private String password;
	private String from;
	private String dest;
	private String subject;
	private String content;
	private List attachments;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List getAttachments() {
		return attachments;
	}
	public void setAttachments(List attachments) {
		this.attachments = attachments;
	}
}
