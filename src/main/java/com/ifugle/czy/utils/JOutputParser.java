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

import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.FilterField;
import com.ifugle.czy.utils.bean.template.JOutput;
import com.ifugle.czy.utils.bean.template.OrderField;
import com.ifugle.czy.utils.bean.template.ValuedDs;

public class JOutputParser {
	private static Logger log = Logger.getLogger(JOutputParser.class);
	private static JOutputParser tmpParser;
	private JOutputParser(){
	}
	public static JOutputParser getParser(){
		if(tmpParser==null){
			tmpParser=new JOutputParser();
		}
		return tmpParser;
	}
	public void parseTemplateToJOutput(String tInfo,List tslst,Map tsmap,String fileName) {
		if(StringUtils.isEmpty(tInfo))return;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tInfo.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return ;
		    if(root.elementIterator("joutput")!=null){
				for(Iterator it=root.elementIterator("joutput");it.hasNext();){
					Element snode=(Element)it.next();
					JOutput jp = new JOutput();
					jp.setInfile(fileName);
					jp.setId(snode.attributeValue("id"));
					jp.setName(snode.attributeValue("name"));
					jp.setDesc(snode.attributeValue("description"));
					String t = snode.attributeValue("type");
					if("1".equals(t)||"option".equalsIgnoreCase(t)){
						jp.setType(1);
					}else{
						jp.setType(0);
					}
					String optype = snode.attributeValue("optionsType");
					if("1".equals(optype)||"range".equalsIgnoreCase(optype)){
						jp.setOptionsType(1);
					}else{
						jp.setOptionsType(0);
					}
					jp.setjTemplate(snode.elementText("jTemplate"));
					if(snode.elementIterator("vDs")!=null){
						List vdses = new ArrayList();
						for(Iterator ids=snode.elementIterator("vDs");ids.hasNext();){
							Element dsNode=(Element)ids.next();
							ValuedDs vds = new ValuedDs();
							vds.setName(dsNode.attributeValue("name"));
							//由于索引都是小写，引用数据源ID强制转为小写
							String refDs = dsNode.attributeValue("refDtSrc");
							//有指明refDtSrc的，用该属性做实际的数据源名。如无指定，默认等于数据源名。
							refDs = StringUtils.isEmpty(refDs)?vds.getName():refDs;
							vds.setRefDtSrc(refDs.toLowerCase());
							String paging = dsNode.attributeValue("paging");
							if("true".equals(paging)||"1".equals(paging)){
								vds.setPaging(true);
							}else{
								vds.setPaging(false);
							}
							vds.setStartParam(StringUtils.isEmpty(dsNode.attributeValue("startParam"))?"from":dsNode.attributeValue("startParam"));
							vds.setSizeParam(StringUtils.isEmpty(dsNode.attributeValue("sizeParam"))?"size":dsNode.attributeValue("sizeParam"));
							vds.setFields(dsNode.attributeValue("fields"));
							if(dsNode.elementIterator("filter")!=null){
								List filterFlds = new ArrayList();
								Map filterFldsMap = new HashMap();
								for(Iterator iflt=dsNode.elementIterator("filter");iflt.hasNext();){
									Element fNode=(Element)iflt.next();
									FilterField ffld = new FilterField();
									ffld.setName(fNode.attributeValue("name"));
									
									String refParam =fNode.attributeValue("refParam");
									String rp = StringUtils.isEmpty(refParam)?ffld.getName():refParam;
									ffld.setRefParam(rp);
									
									ffld.setValue(fNode.attributeValue("value"));
									ffld.setFtype(StringUtils.isEmpty(fNode.attributeValue("ftype"))?"term":fNode.attributeValue("ftype"));
									String sdtype = fNode.attributeValue("dataType");
									if("1".equals(sdtype)||"integer".equalsIgnoreCase(sdtype)){
										ffld.setDataType(1);
									}else if("2".equals(sdtype)||"double".equalsIgnoreCase(sdtype)){
										ffld.setDataType(2);
									}else{
										ffld.setDataType(0);
									}
									filterFlds.add(ffld);
									filterFldsMap.put(ffld.getName(), ffld);
								}
								vds.setFilterFlds(filterFlds);
								vds.setFilterFldsMap(filterFldsMap);
							}
							if(dsNode.elementIterator("orderBy")!=null){
								List orderByFlds = new ArrayList();
								for(Iterator iob=dsNode.elementIterator("orderBy");iob.hasNext();){
									Element oNode=(Element)iob.next();
									OrderField ofld = new OrderField();
									ofld.setName(oNode.attributeValue("name"));
									String sdtype = oNode.attributeValue("dataType");
									if("1".equals(sdtype)||"integer".equalsIgnoreCase(sdtype)){
										ofld.setDataType(1);
									}else if("2".equals(sdtype)||"double".equalsIgnoreCase(sdtype)){
										ofld.setDataType(2);
									}else{
										ofld.setDataType(0);
									}
									ofld.setDir(oNode.attributeValue("dir"));
									orderByFlds.add(ofld);
								}
								vds.setOrderByFlds(orderByFlds);
							}
							vdses.add(vds);
						}
						jp.setValuedDs(vdses);
					}
					tslst.add(jp);
				    tsmap.put(jp.getId(), jp);
				}
		    }
		}catch(Exception e){
			log.error(e.toString());
		}
	}
}
