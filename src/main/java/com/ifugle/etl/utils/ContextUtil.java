package com.ifugle.etl.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class ContextUtil {
	private static Logger logger = LoggerFactory.getLogger(ContextUtil.class);
	private static ApplicationContext context;  
    public static ApplicationContext getContext() {  
       return context;  
    }  
    public static Object getBean(String beanId) { 
    	Object bean = null;
    	try{
    		bean = context.getBean(beanId);  
    	}catch(Exception e){
    		logger.error(e.toString());
    	}
    	if (bean == null)  
            return null;
        return bean;  
    }  
    public static void setContext(ApplicationContext ctx) {  
       context = ctx;  
    }
}
