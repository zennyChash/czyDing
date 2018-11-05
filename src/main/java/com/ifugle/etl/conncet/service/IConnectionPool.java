package com.ifugle.etl.conncet.service;

import java.util.List;

import com.ifugle.etl.entity.ConnectInfo;

public interface IConnectionPool {
	public void init(ConnectInfo cinfo);
	public void shutdown();
	public void restart();
	public Object getConnection();
	public void close(List resourcesToRelieve);
}
