package com.ifugle.utils;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletContextEvent; 
import javax.servlet.ServletContextListener; 

import com.ifugle.czy.utils.TemplatesLoader;

public class InitTemplatesListener implements ServletContextListener{ 
   
	public void contextInitialized(ServletContextEvent event){ 
		//数据源定义模板
    	TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
    	try{
    		ltmp.loadDataSrcs();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		//json数据模板
    	try{
    		ltmp.loadJSONPages();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    } 
    public void contextDestroyed(ServletContextEvent event){ 
    } 
} 

