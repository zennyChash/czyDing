package com.ifugle.czy.service;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticSearchServiceCzy {
	private static Logger log = Logger.getLogger(ElasticSearchServiceCzy.class);
	private TransportClient client;  
	private String esClusterName;
	private String esIP;
	private String esPort;
	
	public String getEsClusterName() {
		return esClusterName;
	}
	@Autowired
	public void setEsClusterName(String esClusterName) {
		this.esClusterName = esClusterName;
	}
	public String getEsIP() {
		return esIP;
	}
	@Autowired
	public void setEsIP(String esIP) {
		this.esIP = esIP;
	}
	public String getEsPort() {
		return esPort;
	}
	@Autowired
	public void setEsPort(String esPort) {
		this.esPort = esPort;
	}
	
	public TransportClient getClient() {
		if(client==null){
	        //设置集群名称  
	        Settings settings = Settings.builder().put("cluster.name",esClusterName).put("client.transport.sniff", true).build();// 集群名  
	        //创建client 
	        try{
	        	client  = new PreBuiltTransportClient(settings)  
	                .addTransportAddress(new TransportAddress(InetAddress.getByName(esIP), Integer.parseInt(esPort)));  
	        }catch(Exception e){
	        	log.error("elasticSearch建立连接失败！集群名："+esClusterName+"；服务器IP："+esIP+"；端口："+esPort);
	        	log.error(e.toString());
	        }
		}
		return client;
	}
}
