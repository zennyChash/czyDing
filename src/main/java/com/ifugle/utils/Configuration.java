package com.ifugle.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
public class Configuration {
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	private static Configuration systemConfig = null;
	private static ResourceBundle resources = null;
	private List systemSets = null;
	private Map mapSystemSets = null;
	private Map logMap = new HashMap();
	private Map taskStatus = new HashMap();
	private Map taskScriptsCount = new HashMap();
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
	        if(mapSystemSets==null){
	        	loadSystemSets();
	        }
	        if(mapSystemSets!=null){
	        	Map sysSet = (Map)mapSystemSets.get(key);
	        	result = sysSet==null?null:(String)sysSet.get("ivalue");
	        }
	        if(result==null){
	        	result=resources.getString(key);
	        }
	        if(result==null){
	        	result = defaultValue;
	        }
	    }catch(Exception e){
	        result = defaultValue;
	    }
        return result;
    }
    //加载系统设置
  	private void loadSystemSets(){
  		try{
	  		Object[] params = null;
	  		StringBuffer sql = new StringBuffer("select item,iname,ivalue,remark from systemset order by item");
	  		systemSets=jdbcTemplate.queryForList(sql.toString(),new Object[]{});
	  		if(systemSets!=null&&systemSets.size()>0){
	  			mapSystemSets = new HashMap();
	  	    	for(int i=0;i<systemSets.size();i++){
	  	    		Map ss = (Map)systemSets.get(i);
	  	    		mapSystemSets.put((String)ss.get("item"), ss);
	  	    	}
	  		}
  		}catch(Exception e){
	  	}
  	}
  	public void reloadSystemSets(){
  		loadSystemSets();
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
    
    public void buildTaskLog(String taskID){
    	List logs = new ArrayList();
		logMap.put(taskID, logs);
  	}
    //多线程执行脚本任务时，给每次执行任务生成一个日志条目集合，可向其中逐个添加日志条目
  	public void addLogItem(String taskID,String logInfo){
  		List logs = null;
  		if(logMap.containsKey(taskID)){
  			logs = (List)logMap.get(taskID);
  		}else{
  			logs = new ArrayList();
  			logMap.put(taskID, logs);
  		}
  		logs.add(logInfo);
  	}
  	
  	public List getLogs(String taskID){
  		if(logMap.containsKey(taskID)){
  			return (List)logMap.get(taskID);
  		}
  		return null;
  	}
  	public synchronized List consumeLogs(String taskID){
  		List alldata = new ArrayList();
  		List lst = (List)logMap.get(taskID);
  		if(lst!=null){
  			for(int i=0;i<lst.size();i++){
  				alldata.add((String)lst.get(i));
  			}
  		}
  		lst.clear();
  		return alldata;
  	}
  	//任务执行结束后，清除任务日志相关的信息。
  	public void clearTaskLogs(String taskID){
  		if(logMap.containsKey(taskID)){
  			logMap.remove(taskID);
  		}
  		if(taskStatus.containsKey(taskID)){
  			taskStatus.remove(taskID);
  		}
  		if(taskScriptsCount.containsKey(taskID)){
  			taskScriptsCount.remove(taskID);
  		} 
  	}
  	public void setTaskStatus(String tid,int status){
  		taskStatus.put(tid, status);
  	}
  	public int getTaskStatus(String tid){
  		int flag = 0;
  		if(taskStatus.containsKey(tid)){
  			flag = ((Integer)taskStatus.get(tid)).intValue();
  		}else{
  			flag = 9;
  		}
  		return flag;
  	}
  	public void buildTaskScriptsCounts(String tid){
  		taskScriptsCount.put(tid, new int[]{0,0});
  	}
  	//增加任务的成功脚本数
  	public void addSuccessScript(String tid){
  		if(!taskScriptsCount.containsKey(tid)){
  			return;
  		}
  		int[] tscc = (int[])taskScriptsCount.get(tid);
  		tscc[0]++;
  	}
  	//增加任务的失败脚本数
  	public void addFailedScript(String tid){
  		if(!taskScriptsCount.containsKey(tid)){
  			return;
  		}
  		int[] tscc = (int[])taskScriptsCount.get(tid);
  		tscc[1]++;
  	}
  	//获取任务的成功脚本数
  	public int getSuccessScript(String tid){
  		if(!taskScriptsCount.containsKey(tid)){
  			return 0;
  		}
  		int[] tscc = (int[])taskScriptsCount.get(tid);
  		return tscc[0];
  	}
  	//获取任务的失败脚本数
  	public int getFailedScript(String tid){
  		if(!taskScriptsCount.containsKey(tid)){
  			return 0;
  		}
  		int[] tscc = (int[])taskScriptsCount.get(tid);
  		return tscc[1];
  	}
}
