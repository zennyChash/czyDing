package com.ifugle.czy.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.JPage;
import com.ifugle.utils.Configuration;

public class TemplatesLoader {
	private static Map dataSrcMap;
	private static List dataSrcTemplates;
	private static TemplatesLoader tLoader;
	private static Map JSONPageMap;
	private static List JSONPageTemplates;
	private TemplatesLoader(){};
	/**
	 * 获取模板加载器的实例。
	 * singleton，每次调用返回的是同一个模板加载器实例。
	 * @return 模板加载器实例。
	 */
	public static TemplatesLoader getTemplatesLoader(){
		if(tLoader!=null){
			return tLoader;
		}else{
			tLoader=new TemplatesLoader();
			return tLoader;
		}
	}
	public void loadDataSrcs(){
		String loadDs=Configuration.getConfig().getString("loadDataSrcTemplates", "0");
		//数据源模板不是每次都需要加载，可以不加载。
		if("0".equals(loadDs)){
			return;
		}
		loadTemplatesFromFile("dataSrc",dataSrcTemplates,dataSrcMap);
	}
	
	public void loadJSONPages(){
		loadTemplatesFromFile("JSONPage",JSONPageTemplates,JSONPageMap);
	}
	public void loadTemplatesFromFile(String tmpType,List tslst,Map tsmap){
		String path=Configuration.getConfig().getString(tmpType+"TemplatesPath", "");
		if(path==null||"".equals(path)){
			System.out.print("数据源定义模板路径未指定或为空，没有要加载的信息！");
			return;
		}
		String pre=path.substring(0,1);
		String pathType = Configuration.getConfig().getString(tmpType+"PathType", "relative");
		if("relative".equals(pathType)){
			if(!"/".equals(pre)){
				path="/"+path;
			}
			URL rootP=TemplatesLoader.class.getClassLoader().getResource(path); 
			if(rootP==null){
				System.out.println(tmpType+"TemplatesPath 根目录为空!");
				return;
			}
			try{
				System.out.println(tmpType+"TemplatesPath.getPath:"+rootP.getPath());
				path=rootP.toURI().getPath();
			}catch(Throwable e){
				System.out.println("toURI转换错误："+e.toString());
				path=rootP.getPath();
				path = path.replaceAll("%20", " ");
			}
		}
		List pathes=new ArrayList();
		tsmap=new HashMap();
		tslst = new ArrayList();
		InputStream is=null;
		try{
			java.io.File dir=new java.io.File(path);
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
					if("dataSrc".equals(tmpType)){
						try{
							loadDataSrcTemplate(tInfo,tslst,tsmap);
						}catch(Exception e){
							System.out.println();
						}
					}else{
						try{
							loadJSONPageTemplate(tInfo,tslst,tsmap);
						}catch(Exception e){
							System.out.println();
						}
					}
				}
				System.out.println(tmpType+"共解析"+tslst.size()+"个模板！");
			}
		}catch(Exception e){
			if(is!=null){
				try{
					is.close();
				}catch(Exception ex){};
			}
			System.out.print("加载"+tmpType+"模板信息时发生错误："+e.toString());
		}finally{
			try{
				is.close();
			}catch(Exception e){
			}
		}
	}
	private void loadDataSrcTemplate(String tInfo,List tslst,Map tsmap) {
		DataSourceTemplateParser parser=DataSourceTemplateParser.getParser();
		parser.parseTemplateToDtSrc(tInfo,tslst,tsmap);
		return;
	}
	
	private void loadJSONPageTemplate(String tInfo,List tslst,Map tsmap) {
		JPageParser parser=JPageParser.getParser();
		parser.parseTemplateToJPage(tInfo,tslst,tsmap);
	}
	public JPage getJPage(String jpID){
		if(jpID==null||"".equals(jpID))
			return null;
		if(JSONPageMap==null){
			loadTemplatesFromFile("dataSrc",dataSrcTemplates,dataSrcMap);
			loadTemplatesFromFile("JSONPage",JSONPageTemplates,JSONPageMap);
		}
		JPage jp=(JPage)JSONPageMap.get(jpID);
		return jp;
	}
	public DataSrc getDataSrc(String dtID){
		if(dtID==null||"".equals(dtID))
			return null;
		if(dataSrcMap==null){
			loadTemplatesFromFile("dataSrc",dataSrcTemplates,dataSrcMap);
		}
		DataSrc dts=(DataSrc)JSONPageMap.get(dtID);
		return dts;
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
				paths.add(fs[i].getAbsolutePath());
				System.out.println(fs[i].getAbsolutePath());
			}
		} 
	}
}
