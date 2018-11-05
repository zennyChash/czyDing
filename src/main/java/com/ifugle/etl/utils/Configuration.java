package com.ifugle.etl.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Configuration {
	private static Logger log = LoggerFactory.getLogger(Configuration.class);
	private Map handlersMap = new HashMap();
	private static Configuration systemConfig = null;
	private static ResourceBundle resources = null;
	public Map getHandlersMap(){
		return handlersMap;
	}
	private Configuration(){
		try{
            resources = ResourceBundle.getBundle("Resource", Locale.getDefault());
        }
        catch(MissingResourceException mre){
          System.out.println(mre.toString());
        }
	}
	public static Configuration getConfig(){
        if(systemConfig == null)
            systemConfig = new Configuration();
        return systemConfig;
    }
	
	 public String getString(String key, String defaultValue){
	    String result = null;
	    try{
	        result=resources.getString(key);
	        if(result==null){
	        	result = defaultValue;
	        }
	    }catch(Exception e){
	        result = defaultValue;
	    }
	    return result;
	}

	public String getString(String key){
	    return getString(key, null);
	}
}
