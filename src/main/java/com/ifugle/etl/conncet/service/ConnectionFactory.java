package com.ifugle.etl.conncet.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifugle.etl.entity.ConnectInfo;
import com.ifugle.etl.utils.TemplateLoader;

public class ConnectionFactory {
	private Map poolMap=new HashMap();
	private static Logger log = LoggerFactory.getLogger(ConnectionFactory.class);
	private static ConnectionFactory connFactory= null;
	private ConnectionFactory(){
	}
	public static ConnectionFactory getConnectionFactory(){
		if(connFactory ==null){
			connFactory = new ConnectionFactory();
		}
		return connFactory;
	}
	public synchronized IConnectionPool getConnectionPool(String conId){
		if(conId==null||"".equals(conId)){
			return null;
		}
		IConnectionPool connPool = null;
		if(!poolMap.containsKey(conId)){
			//构造
			ConnectInfo cinfo = (ConnectInfo)TemplateLoader.getLoader().getETLObjectTemplate(1,conId);
			if(cinfo.getType()==0){
				connPool = new DBConnectionPool();
				connPool.init(cinfo);
				poolMap.put(conId, connPool);
			}
		}else{
			connPool = (IConnectionPool)poolMap.get(conId);
		}
		return connPool;
	}
	
}
