package com.ifugle.etl.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifugle.etl.entity.ConnectInfo;
import com.ifugle.etl.entity.SchedulerInfo;
import com.ifugle.etl.entity.base.ETLObject;
import com.ifugle.etl.entity.base.Task;

public class TemplateLoader {
	private static Logger log = LoggerFactory.getLogger(TemplateLoader.class);
	private static TemplateLoader loader = null;
	private Map taskTemplatesMap;
	private List taskTemplates;
	private List taskPaths;
	private Map connectionInfosMap;
	private List connectionInfos;
	private List connPaths;
	
	private Map scheduleInfosMap;
	private List scheduleInfos;
	private List schedulePaths;
	
	private TemplateLoader(){
	}
	public static TemplateLoader getLoader(){
		if(loader==null){
			loader = new TemplateLoader();
		}
		return loader;
	}
	public void loadTemplatesFromFile(int type,String rootPath,List pathes,List tmps,Map tmpsMap){
		System.out.println("rootPath in TemplatesLoader:"+rootPath);
		if(rootPath==null||"".equals(rootPath)){
			System.out.print("配置文件路径未指定或为空，没有要加载的配置信息！");
			return;
		}
		String pre=rootPath.substring(0,1);
		String pathType = Configuration.getConfig().getString("pathType", "relative");
		if("relative".equals(pathType)){//相对路径模式
			if(!"/".equals(pre)){
				rootPath="/"+rootPath;
			}
			URL rootP=TemplateLoader.class.getClassLoader().getResource(rootPath); 
			if(rootP==null){
				System.out.println("rootP is null!");
				return;
			}
			try{
				System.out.println("rootP.getPath:"+rootP.getPath());
				rootPath=rootP.toURI().getPath();
			}catch(Throwable e){
				System.out.println("toURI转换错误："+e.toString());
				rootPath=rootP.getPath();
				rootPath = rootPath.replaceAll("%20", " ");
			}
		}
		InputStream is=null;
		try{
			java.io.File dir=new java.io.File(rootPath);
			getAllFilesPath(dir,pathes);
			System.out.println("共找到"+pathes.size()+"个文件！");
			//各个设计文件循环解析、加载。
			if(pathes!=null&&pathes.size()>0){
				for(int i=0;i<pathes.size();i++){
					//文件流转化成string作为参数传递给解析器
					String xmlPath=(String)pathes.get(i);
					File tmpFile=new File(xmlPath); 
					is=new FileInputStream(tmpFile) ;
					long contentLength = tmpFile.length();
					byte[] ba = new byte[(int)contentLength];
					is.read(ba);
					String tInfo = new String(ba,"utf-8");
					is.close();
					//设计内容，解析
					try{
						loadTemplate(type,tInfo,tmps,tmpsMap);
					}catch(Exception e){
						System.out.println();
					}
				}
				System.out.println("共解析"+tmps.size()+"个模板！");
			}
		}catch(Exception e){
			if(is!=null){
				try{
					is.close();
				}catch(Exception ex){};
			}
			System.out.print("加载设计文件信息时发生错误："+e.toString());
		}finally{
			try{
				is.close();
			}catch(Exception e){
			}
		}
	}
	//获取指定目录下的设计文件，递归
	@SuppressWarnings("unchecked")
	private void getAllFilesPath(File dir,List paths)throws Exception{
		File[] fs = dir.listFiles(); 
		if(fs==null||fs.length==0)return;
		for(int i=0; i<fs.length; i++){ 
			if(fs[i].isDirectory()){
				System.out.println(fs[i].getAbsolutePath());
				//递归获取任务文件
				getAllFilesPath(fs[i],paths); 
			}else{
				//添加文件。
				//if(fs[i].getName().toLowerCase().endsWith("xml")){
					paths.add(fs[i].getAbsolutePath());
					System.out.println(fs[i].getAbsolutePath());
				//}
			}
		} 
	}
	@SuppressWarnings("unchecked")
	public void loadTemplate(int type,String streamInfos,List tmps,Map tmpsMap)throws Exception{
		if(type==0){
			TaskTemplateParser parser=TaskTemplateParser.getParser();
			Task tmp= null;
			try{
				tmp=parser.parseTemplate(streamInfos);
			}catch(Exception e){
				log.error(e.toString());
			}
			if(tmp!=null){
				tmps.add(tmp);
				tmpsMap.put(tmp.getId(), tmp);
			}
		}else if(type==1){
			ConnectionTemplateParser cparser = ConnectionTemplateParser.getParser();
			List conns = cparser.parseTemplate(streamInfos);
			ConnectInfo conn=null;
			if(conns!=null&&conns.size()>0){
				for(int i=0;i<conns.size();i++){
					conn = (ConnectInfo)conns.get(i);
					if(conn==null){
						continue;
					}
					tmps.add(conn);
					tmpsMap.put(conn.getId(), conn);
				}
			}
		}else if(type==2){
			ScheduleTemplateParser sparser = ScheduleTemplateParser.getParser();
			SchedulerInfo sdu = null;
			try{
				sdu=sparser.parseTemplate(streamInfos);
			}catch(Exception e){
				log.error(e.toString());
			}
			if(sdu!=null){
				tmps.add(sdu);
				tmpsMap.put(sdu.getId(), sdu);
			}
		}
	}
	/**
	 * 获取模板定义信息集合。
	 * 是模板对象的有序集合。
	 * @return 模板集合。
	 */
	public List getETLObjectTemplates(int type){
		List tmps = null;
		if(type==0){
			if(taskTemplates==null){
				taskPaths = new ArrayList();
				taskTemplatesMap = new HashMap();
				taskTemplates = new ArrayList();
				loadTemplatesFromFile(type,"tasks",taskPaths,taskTemplates,taskTemplatesMap);
			}
			tmps =taskTemplates;
		}else if(type==1){
			if(connectionInfos==null){
				connPaths = new ArrayList();
				connectionInfosMap = new HashMap();
				connectionInfos = new ArrayList();
				loadTemplatesFromFile(type,"connections",connPaths,connectionInfos,connectionInfosMap);
			}
			tmps =connectionInfos;
		}else if(type==2){
			if(scheduleInfos==null){
				schedulePaths = new ArrayList();
				scheduleInfosMap = new HashMap();
				scheduleInfos = new ArrayList();
				loadTemplatesFromFile(type,"schedules",schedulePaths,scheduleInfos,scheduleInfosMap);
			}
			tmps =scheduleInfos;
		}
		return tmps;
	}
	/**
	 * 获取任务定义信息的map。
	 * @return 以Task对象的ID索引Task对象。
	 */
	public Map getETLObjectTemplatesMap(int type){
		Map tmpsMap = null;
		if(type==0){
			if(taskTemplatesMap==null){
				taskPaths = new ArrayList();
				taskTemplatesMap = new HashMap();
				taskTemplates = new ArrayList();
				String tpath = Configuration.getConfig().getString("taskPath","tasks");
				loadTemplatesFromFile(type,tpath,taskPaths,taskTemplates,taskTemplatesMap);
			}
			tmpsMap =taskTemplatesMap;
		}else if(type==1){
			if(connectionInfosMap==null){
				connPaths = new ArrayList();
				connectionInfosMap = new HashMap();
				connectionInfos = new ArrayList();
				String cpath = Configuration.getConfig().getString("connPath","connections");
				loadTemplatesFromFile(type,cpath,connPaths,connectionInfos,connectionInfosMap);
			}
			tmpsMap =connectionInfosMap;
		}else if(type==2){
			if(scheduleInfosMap==null){
				schedulePaths = new ArrayList();
				scheduleInfosMap = new HashMap();
				scheduleInfos = new ArrayList();
				String spath = Configuration.getConfig().getString("schedulePath","schedules");
				loadTemplatesFromFile(type,spath,schedulePaths,scheduleInfos,scheduleInfosMap);
			}
			tmpsMap =scheduleInfosMap;
		}
		return tmpsMap;
	}
	/**
	 * 根据指定的模板id获取对象
	 * @param tmpId
	 * @return 指定id的模板对象。
	 */
	public ETLObject getETLObjectTemplate(int type,String tmpId){
		if(tmpId==null||"".equals(tmpId))
			return null;
		Map maps = getETLObjectTemplatesMap(type);
		if(maps==null){
			return null;
		}
		ETLObject obj=(ETLObject)maps.get(tmpId);
		return obj;
	}
    public void destroyTemplates(int type){
    	if(type==0){
	    	taskPaths=null;
	    	taskTemplates=null;
	    	taskTemplatesMap=null;
    	}else if(type==1){
    		connectionInfosMap = null;
    		connectionInfos = null;
    		connPaths = null;
    	}else{
    		scheduleInfosMap= null;
    		scheduleInfos= null;
    		schedulePaths= null;
    	}
    }
}
