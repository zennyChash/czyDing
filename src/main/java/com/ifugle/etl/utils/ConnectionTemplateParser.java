package com.ifugle.etl.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.ifugle.etl.entity.ConnectInfo;


public class ConnectionTemplateParser {
	private static ConnectionTemplateParser tmpParser;
	private ConnectionTemplateParser(){
	}
	/**
	 * 获取解析器实例。
	 * singleton模式，调用返回的是同一个解析器实例。 
	 * @return 解析器实例。
	 */
	public static ConnectionTemplateParser getParser(){
		if(tmpParser==null)
			tmpParser=new ConnectionTemplateParser();
		return tmpParser;
	}
	public List parseTemplate(String tmpContent)throws ParseException{
		List cons = null;
		if(tmpContent==null||"".equals(tmpContent))return null;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tmpContent.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return null;
		    if(root.elementIterator("connection")!=null){
		    	cons=new ArrayList();
				for(Iterator it=root.elementIterator("connection");it.hasNext();){
					Element cnode=(Element)it.next();
					ConnectInfo con = parseConnect(cnode);
					cons.add(con);
				}
		    }
		}catch(Exception e){
			throw new ParseException(e.toString());
		}
		return cons;
	}
	
	private ConnectInfo parseConnect(Element cnode){
		if(cnode==null){
			return null;
		}
		ConnectInfo con = new ConnectInfo();
		con.setId(cnode.elementText("id"));
		con.setName(cnode.elementText("name"));
		con.setDesc(cnode.elementText("description"));
		con.setCharset(cnode.elementText("charset"));
		int type = 0;
		try{
			String st=cnode.elementText("type");
			type=Integer.parseInt(st);
		}catch(Exception e){
			type=0;
		}
		con.setType(type);
		con.setConurl(cnode.elementText("conurl"));
		con.setDriver_class(cnode.elementText("driver_class"));
		int initialSize = 1;
		try{
			String siSize = cnode.elementText("initialSize");
			initialSize = Integer.parseInt(siSize);
		}catch(Exception e){
			initialSize = 1;
		}
		con.setInitialSize(initialSize);
		int maxActive = 5;
		try{
			String sMactive = cnode.elementText("maxActive");
			maxActive = Integer.parseInt(sMactive);
		}catch(Exception e){
			maxActive = 5;
		}
		con.setMaxActive(maxActive);
		int maxIdle =10;
		try{
			String sMaxIdle = cnode.elementText("maxIdle");
			maxIdle = Integer.parseInt(sMaxIdle);
		}catch(Exception e){
			maxIdle = 10;
		}
		con.setMaxIdle(maxIdle);
		long maxWait = 10000;
		try{
			String sMaxWait = cnode.elementText("maxWait");
			maxWait = Long.parseLong(sMaxWait);
		}catch(Exception e){
			maxWait = 10000;
		}
		con.setMaxWait(maxWait);
		int minIdle = 1;
		try{
			String sMinIdle = cnode.elementText("minIdle");
			minIdle = Integer.parseInt(sMinIdle);
		}catch(Exception e){
			minIdle = 1;
		}
		con.setMinIdle(minIdle);
		con.setPassword(cnode.elementText("password"));
		boolean removeAbandoned = true;
		try{
			String sRemoveAb = cnode.elementText("removeAbandoned");
			removeAbandoned = Boolean.parseBoolean(sRemoveAb);
		}catch(Exception e){
			minIdle = 1;
		}
		con.setRemoveAbandoned(removeAbandoned);
		int removeAbandonedTimeout=600;
		try{
			String sReTimeout = cnode.elementText("removeAbandonedTimeout");
			removeAbandonedTimeout = Integer.parseInt(sReTimeout);
		}catch(Exception e){
			removeAbandonedTimeout = 1;
		}
		con.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		con.setUsername(cnode.elementText("username"));
		return con;
	}
}
