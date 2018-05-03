package com.ifugle.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Configuration {
	private static Configuration systemConfig = null;
	private static ResourceBundle resources = null;
	private Configuration(){
	    try{
	    	resources = ResourceBundle.getBundle("Resource", Locale.getDefault());
	    }catch(MissingResourceException mre){
	    	System.out.println(mre.toString());
	    }
    }

    public static Configuration getConfig(){
        if(systemConfig == null)
            systemConfig = new Configuration();
        return systemConfig;
    }
    //检查是否获得正确的资源文件
    private static boolean checkResources(){
        boolean result = true;
        if(resources == null){
            result = false;
        }
        return result;
    }

   /**
    * 获取指定配置项的值
    * @param key 配置项名
    * @param defaultValue 默认值。
    * @return 配置项的值。如找不到该项，则使用默认值。
    */
    public String getString(String key, String defaultValue){
        String result = null;
        try{
            if(checkResources())
                result = resources.getString(key);
            else
                result = defaultValue;
        }catch(MissingResourceException mre){
            result = defaultValue;
        }
        return result;
    }
    /**
     * 获取指定配置项的值。
     * 该方法不提供默认值。
     * @param key 配置项名
     * @return 配置项的值。
     */
    public String getString(String key){
        return getString(key, null);
    }
}
