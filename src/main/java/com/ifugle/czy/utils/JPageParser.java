package com.ifugle.czy.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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
import com.ifugle.czy.utils.bean.template.JPage;
import com.ifugle.czy.utils.bean.template.OrderField;
import com.ifugle.czy.utils.bean.template.ValuedDs;

public class JPageParser {
	private static Logger log = Logger.getLogger(JPageParser.class);
	private static JPageParser tmpParser;
	private JPageParser(){
	}
	public static JPageParser getParser(){
		if(tmpParser==null){
			tmpParser=new JPageParser();
		}
		return tmpParser;
	}
	public void parseTemplateToJPage(String tInfo,List tslst,Map tsmap) {
		if(StringUtils.isEmpty(tInfo))return;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tInfo.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return ;
		    if(root.elementIterator("jpage")!=null){
				for(Iterator it=root.elementIterator("jpage");it.hasNext();){
					Element snode=(Element)it.next();
					JPage jp = new JPage();
					jp.setId(snode.attributeValue("id"));
					jp.setName(snode.attributeValue("name"));
					jp.setDesc(snode.attributeValue("description"));
					jp.setjTemplate(snode.elementText("jTemplate"));
					if(snode.elementIterator("vDs")!=null){
						List vdses = new ArrayList();
						for(Iterator ids=snode.elementIterator("vDs");ids.hasNext();){
							Element dsNode=(Element)ids.next();
							ValuedDs vds = new ValuedDs();
							vds.setName(dsNode.attributeValue("name"));
							vds.setRefDtSrc(dsNode.attributeValue("refDtSrc"));
							if(dsNode.elementIterator("filter")!=null){
								List filterFlds = new ArrayList();
								for(Iterator iflt=dsNode.elementIterator("filter");iflt.hasNext();){
									Element fNode=(Element)iflt.next();
									FilterField ffld = new FilterField();
									ffld.setName(fNode.attributeValue("name"));
									ffld.setValue(fNode.attributeValue("value"));
									filterFlds.add(ffld);
								}
								vds.setFilterFlds(filterFlds);
							}
							if(dsNode.elementIterator("orderBy")!=null){
								List orderByFlds = new ArrayList();
								for(Iterator iob=dsNode.elementIterator("orderBy");iob.hasNext();){
									Element oNode=(Element)iob.next();
									OrderField ofld = new OrderField();
									ofld.setName(oNode.attributeValue("name"));
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
