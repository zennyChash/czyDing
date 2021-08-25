package com.ifugle.czyDing.utils;

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

import com.ifugle.czyDing.router.bean.*;

public class ApiRouterParser {
	private static Logger log = Logger.getLogger(ApiRouterParser.class);
	private static ApiRouterParser tmpParser;
	private ApiRouterParser(){
		
	}
	public static ApiRouterParser getParser(){
		if(tmpParser==null){
			tmpParser=new ApiRouterParser();
		}
		return tmpParser;
	}
	
	public void parseTemplateToApiRouter(String tInfo,List tslst,Map tsmap,String fileName) {
		if(StringUtils.isEmpty(tInfo))return;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tInfo.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null)
		    	return ;
		    if(root.elementIterator("service")!=null){
		    	for(Iterator it=root.elementIterator("service");it.hasNext();){
					Element snode=(Element)it.next();
					AppService svr = new AppService();
				    svr.setId(snode.attributeValue("id").toLowerCase());
				    svr.setName(snode.attributeValue("name"));
				    svr.setDesc(snode.attributeValue("description"));
				    svr.setRootURI(snode.attributeValue("rootURI"));
				    if(snode.elementIterator("method")!=null){
						List methods=new ArrayList();
						Map methodsMap = new HashMap();
						for(Iterator cit=snode.elementIterator("method");cit.hasNext();){
							Element cmnode=(Element)cit.next();
							AppMethod m = new AppMethod();
							m.setName(cmnode.attributeValue("name"));
							if(cmnode.elementIterator("request")!=null){
								List requests = new ArrayList();
								Map requestsMap = new HashMap();
								for(Iterator rsit=cmnode.elementIterator("request");rsit.hasNext();){
									Element rnode=(Element)rsit.next();
									ProxyRequest preq = new ProxyRequest();
									preq.setSubURI(rnode.attributeValue("subURI"));
									preq.setDoBefore(rnode.attributeValue("before"));
									preq.setReturnProperty(rnode.attributeValue("returnProperty"));
									preq.setMethod(rnode.attributeValue("method"));
									preq.setSocketTimeout(rnode.attributeValue("socketTimeout"));
									preq.setConnTimeout(rnode.attributeValue("connTimeout"));
									if(rnode.elementIterator("property")!=null){
										Map properties = new HashMap();
										for(Iterator pit=snode.elementIterator("property");pit.hasNext();){
											Element ppnode=(Element)pit.next();
											String pname = ppnode.attributeValue("name");
											String pval  = ppnode.attributeValue("value");
											properties.put(pname,pval);
										}
										preq.setProperties(properties);
									}
									requests.add(preq);
									requestsMap.put(preq.getSubURI(), preq);
								}
								m.setRequests(requests);
								m.setRequestsMap(requestsMap);
							}
							if(cmnode.element("response")!=null){
								ProxyResponse response = new ProxyResponse();
								Element ppnode = cmnode.element("response");
								response.setDoBefore(ppnode.attributeValue("before"));
								m.setResponse(response);
							}
							methods.add(m);
							methodsMap.put(m.getName(), m);
						}
						svr.setMethods(methods);
						svr.setMethodsMap(methodsMap);
				    }
				    tslst.add(svr);
				    tsmap.put(svr.getId(), svr);
		    	}
		    }
		}catch(Exception e){}
	}
}
