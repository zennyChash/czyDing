package com.ifugle.czy.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.ifugle.czy.utils.bean.template.Column;
import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.ProParaIn;
import com.ifugle.czy.utils.bean.template.ProParaOut;
import com.ifugle.czy.utils.bean.template.ProcedureBean;

public class DataSourceTemplateParser {
	private static Logger log = Logger.getLogger(DataSourceTemplateParser.class);
	private static DataSourceTemplateParser tmpParser;
	private DataSourceTemplateParser(){
		
	}
	public static DataSourceTemplateParser getParser(){
		if(tmpParser==null)
			tmpParser=new DataSourceTemplateParser();
		return tmpParser;
	}
	
	public void parseTemplateToDtSrc(String tInfo,List tslst,Map tsmap,String fileName) {
		if(StringUtils.isEmpty(tInfo))return;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tInfo.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return ;
		    if(root.elementIterator("src")!=null){
				for(Iterator it=root.elementIterator("src");it.hasNext();){
					Element snode=(Element)it.next();
					DataSrc ds = new DataSrc();
					ds.setInfile(fileName);
					//由于索引都是小写，数据源ID强制转为小写
				    ds.setId(snode.attributeValue("id").toLowerCase());
				    ds.setName(snode.attributeValue("name"));
				    ds.setDesc(snode.attributeValue("description"));
				    String st = snode.attributeValue("sourceType");
				    if("1".equals(st)||"sql".equalsIgnoreCase(st)){
				    	ds.setSourceType(1);
				    	String sql = snode.elementText("sql");
						ds.setSql(sql);
				    }else if("2".equals(st)||"procedure".equalsIgnoreCase(st)){
				    	ds.setSourceType(2);
						ProcedureBean procedure = parseProcedure(snode.element("procedure"));
						ds.setProcedure(procedure);
				    }
				    if(snode.element("cols")!=null){
				    	Element fcnode =snode.element("cols");
					    if(fcnode!=null&&fcnode.elementIterator("col")!=null){
							List columns=new ArrayList();
							Map mfcols = new HashMap();
							for(Iterator cit=fcnode.elementIterator("col");cit.hasNext();){
								Element clnode=(Element)cit.next();
								Column col = new Column();
								col.setName(clnode.attributeValue("name"));
								col.setFldType(clnode.attributeValue("fldType"));
								String scanOrder = clnode.attributeValue("canOrder");
								String sIsFilter = clnode.attributeValue("isFilter");
								int canOrder = 0;
								try{
									canOrder = Integer.parseInt(scanOrder);
								}catch(Exception e){}
								int isFilter = 0;
								try{
									isFilter = Integer.parseInt(sIsFilter);
								}catch(Exception e){}
								col.setCanOrder(canOrder);
								col.setIsFilter(isFilter);
								col.setAnalyzer(clnode.attributeValue("analyzer"));
								col.setSearch_analyzer(clnode.attributeValue("search_analyzer"));
								columns.add(col);
								mfcols.put(col.getName(), col);
							}
							ds.setCols(columns);
							ds.setColMap(mfcols);
						}
					}
				    tslst.add(ds);
				    //由于索引都是小写，数据源ID强制转为小写，并以小写引用
				    tsmap.put(ds.getId().toLowerCase(), ds);
				}
		    }
		}catch(Exception e){
			log.error(e.toString());
		}
	}
	private ProcedureBean parseProcedure(Element proNode) {
		if(proNode==null)return null;
		ProcedureBean pro=new ProcedureBean();
		pro.setName(proNode.attributeValue("name"));
		int dsi=1;
		try{
			String sdIndex=proNode.attributeValue("datasetIndex");
			dsi=Integer.parseInt(sdIndex);
		}catch(Exception e){
			dsi=1;
		}
		pro.setDataSetIndex(dsi);
		int ti=1;
		try{
			String tIndex=proNode.attributeValue("totalIndex");
			ti=Integer.parseInt(tIndex);
		}catch(Exception e){
			ti=1;
		}
		pro.setTotalIndex(ti);
		
		int opIndex=1;
		try{
			String sOpIndex=proNode.attributeValue("outPutInfoIndex");
			opIndex=Integer.parseInt(sOpIndex);
		}catch(Exception e){
			opIndex=0;
		}
		pro.setOutPutInfoIndex(opIndex);
		
		//过程的输入参数
		if(proNode!=null&&proNode.elementIterator("in")!=null){
			List proIns=new ArrayList();
			for(Iterator iit=proNode.elementIterator("in");iit.hasNext();){
				ProParaIn ppi=new ProParaIn();
				Element piNode=(Element)iit.next();
				int refMode=1;
				try{
					refMode=Integer.parseInt(piNode.attributeValue("referMode"));
				}catch(Exception e){}
				ppi.setReferMode(refMode);
				
				if(refMode==1){
					ppi.setReferTo(piNode.attributeValue("referTo"));
				}else{
					ppi.setValue(piNode.attributeValue("value"));
					int piDt=0;
					String sPidt=piNode.attributeValue("dataType");
					if("int".equalsIgnoreCase(sPidt)||"1".equals(sPidt)){
						piDt=1;
					}else if("double".equalsIgnoreCase(sPidt)||"2".equals(sPidt)){
						piDt=2;
					}else if("cursor".equalsIgnoreCase(sPidt)||"3".equals(sPidt)){
						piDt=3;
					}
					ppi.setDataType(piDt);
				}									
				proIns.add(ppi);
			}
			pro.setInParas(proIns);
		}
		//过程的输出参数
		if(proNode!=null&&proNode.elementIterator("out")!=null){
			List proOuts=new ArrayList();
			for(Iterator oit=proNode.elementIterator("out");oit.hasNext();){
				ProParaOut ppo=new ProParaOut();
				Element poNode=(Element)oit.next();
				int poDt=0;
				String sPodt=poNode.attributeValue("dataType");
				if("int".equalsIgnoreCase(sPodt)||"1".equals(sPodt)){
					poDt=1;
				}else if("double".equalsIgnoreCase(sPodt)||"2".equals(sPodt)){
					poDt=2;
				}else if("cursor".equalsIgnoreCase(sPodt)||"3".equals(sPodt)){
					poDt=3;
				}
				ppo.setDataType(poDt);
				proOuts.add(ppo);
			}
			pro.setOutParas(proOuts);
		}
		return pro;
	}
}
