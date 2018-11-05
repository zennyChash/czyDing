package com.ifugle.etl.utils;
import java.util.List;

import javax.servlet.ServletContextEvent; 
import javax.servlet.ServletContextListener; 

import com.ifugle.etl.schedule.service.SchedulService;

public class InitTemplatesListener implements ServletContextListener{ 
    /**
     * 监听，加载报表模板。
     */
	public void contextInitialized(ServletContextEvent event){ 
    	TemplateLoader ltmp=TemplateLoader.getLoader();
    	try{
    		List tasks = ltmp.getETLObjectTemplates(0);
    		List conns = ltmp.getETLObjectTemplates(1);
    		List schedules = ltmp.getETLObjectTemplates(2);
    		if(schedules!=null&&tasks!=null){
    			//注册任务到调度器
    			SchedulService example = SchedulService.getSchedulService();
    			example.setSchedulers(schedules);
    	        try{
    	        	example.readyScheduler();
    	        	example.startScheduler();
    	        } catch (Exception se) {
    	            se.printStackTrace();
    	        }
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    } 

    public void contextDestroyed(ServletContextEvent event){ 
    	
    } 
} 

