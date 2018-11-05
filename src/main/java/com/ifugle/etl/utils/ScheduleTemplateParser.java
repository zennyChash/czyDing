package com.ifugle.etl.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.ifugle.etl.entity.ScheduledTask;
import com.ifugle.etl.entity.SchedulerInfo;
import com.ifugle.etl.entity.ScheduledJob;
import com.ifugle.etl.entity.component.Parameter;
import com.ifugle.etl.entity.component.TriggerInfo;

public class ScheduleTemplateParser {
	private static ScheduleTemplateParser tmpParser;
	private ScheduleTemplateParser(){
	}
	public static ScheduleTemplateParser getParser(){
		if(tmpParser==null)
			tmpParser=new ScheduleTemplateParser();
		return tmpParser;
	}
	public SchedulerInfo parseTemplate(String tmpContent)throws ParseException{
		if(tmpContent==null||"".equals(tmpContent))return null;
		SchedulerInfo sdu = null;
		try{
			SAXReader reader = new SAXReader();
		    Document doc = reader.read(new ByteArrayInputStream(tmpContent.getBytes("utf-8")));
		    Element root = doc.getRootElement();
		    if(root==null){
		    	return null;
		    }
		    sdu = new SchedulerInfo();
		    sdu.setId(root.attributeValue("id"));
		    sdu.setName(root.attributeValue("name"));
		    sdu.setDesc(root.attributeValue("description"));
		    if(root!=null&&root.elementIterator("job")!=null){
				List jobs=new ArrayList();
				Map jobsMap = new HashMap();
				for(Iterator it=root.elementIterator("job");it.hasNext();){
					Element jnode=(Element)it.next();
					ScheduledJob js = new ScheduledJob();
					js.setJobId(jnode.attributeValue("id"));
					js.setJobMc(jnode.attributeValue("mc"));
					String sDisabled = jnode.attributeValue("disabled");
					if("true".equalsIgnoreCase(sDisabled)||"1".equals(sDisabled)){
						js.setDisabled(1);
					}else{
						js.setDisabled(0);
					}
				    if(jnode!=null&&jnode.elementIterator("task")!=null){
					    List tasks = new ArrayList();
					    Map taskMap = new HashMap();
				    	for(Iterator tit=jnode.elementIterator("task");tit.hasNext();){
				    		Element tnode=(Element)tit.next();
				    		ScheduledTask st = new ScheduledTask();
				    		st.setTaskId(tnode.attributeValue("id"));
							int tFailRetry=0;
							try{
								tFailRetry = Integer.parseInt(tnode.attributeValue("onFailRetry"));
							}catch(Exception e){}
							st.setOnFailRetry(tFailRetry);
							st.setOnFail(tnode.attributeValue("onFail"));
							st.setOnSuccess(tnode.attributeValue("onSuccess"));
							String sdisabled = tnode.attributeValue("disabled");
						    int d = 0;
						    try{
						    	d = Integer.parseInt(sdisabled);
						    }catch(Exception e){}
						    st.setDisabled(d);
						    
				    		tasks.add(st);
				    		taskMap.put(st.getTaskId(), st);
				    	}
					    js.setIsMultiTask(tasks.size()>1?1:0);
				    	ScheduledTask t = parseNextTask((ScheduledTask)tasks.get(0),tasks,taskMap);
				    	js.setHeadTask(t);
				    	js.setTaskMap(taskMap);
				    }
				    Element psnode = jnode.element("params");
				    parseParam(js,psnode);
				    Element tgnode = jnode.element("trigger");
				    parseTrigger(js,tgnode);
				    jobs.add(js);
				    jobsMap.put(js.getJobId(), js);
				}
				sdu.setJobs(jobs);
				sdu.setJobsMap(jobsMap);
		    }
		}catch(Exception e){
			throw new ParseException(e.toString());
		}
		return sdu;
	}
	
	private ScheduledTask parseNextTask(ScheduledTask task,List tasks,Map taskMap){
		String stid = task.getOnSuccess();
		String ftid = task.getOnFail();
		if(!StringUtils.isEmpty(stid)){
			for(int i=0;i<tasks.size();i++){
				ScheduledTask nt = (ScheduledTask)tasks.get(i);
				if(nt.getTaskId().equals(stid)){
					task.setTaskOnSuccess(parseNextTask(nt,tasks,taskMap));
					break;
				}
				
			}
		}
		if(!StringUtils.isEmpty(ftid)){
			for(int i=0;i<tasks.size();i++){
				ScheduledTask nt = (ScheduledTask)tasks.get(i);
				if(nt.getTaskId().equals(ftid)){
					task.setTaskOnFail(parseNextTask(nt,tasks,taskMap));
					break;
				}
			}
		}
		return task;
	}
	
	private void parseParam(ScheduledJob js,Element psnode){
		if(psnode!=null&&psnode.elementIterator("param")!=null){
	    	List params = new ArrayList();
	    	Map paramMap = new HashMap();
	    	for(Iterator pit=psnode.elementIterator("param");pit.hasNext();){
	    		Element pnode=(Element)pit.next();
				Parameter pa = new Parameter();
				pa.setName(pnode.attributeValue("name"));
				pa.setMc(pnode.attributeValue("mc"));
				String sType = pnode.attributeValue("type");
				if("1".equals(sType)||"static".equalsIgnoreCase(sType)){
					pa.setType(1);
				}else{
					pa.setType(0);
				}
				String dtype = pnode.attributeValue("dataType");
				if("int".equalsIgnoreCase(dtype)||"1".equals(dtype)){
					pa.setDataType(1);
				}else if("double".equalsIgnoreCase(dtype)||"2".equals(dtype)){
					pa.setDataType(2);
				}else{
					pa.setDataType(0);
				}
				pa.setExpression(pnode.getText());
				params.add(pa);
				paramMap.put(pa.getName(), pa);
	    	}
	    	js.setParameters(params);
	    	js.setParamMap(paramMap);
	    }
	}
	private void parseTrigger(ScheduledJob js,Element tgnode){
		if(tgnode!=null){
	    	TriggerInfo tg = new TriggerInfo();
	    	String ttype= tgnode.attributeValue("type");
	    	if(StringUtils.isEmpty(ttype)||"simple".equalsIgnoreCase(ttype)||"0".equals(ttype)){
	    		tg.setType(0);
	    	}else{
	    		tg.setType(1);
	    	}
	    	int repeat = 0;
	    	try{
	    		repeat = Integer.parseInt(tgnode.attributeValue("repeat"));
	    	}catch(Exception e){
	    	}
	    	tg.setRepeat(repeat);
	    	String iu = tgnode.attributeValue("intervalUnit");
	    	if("m".equals(iu)||"1".equals(iu)){
	    		tg.setIntervalUnit(1);
	    	}else if("h".equals(iu)||"2".equals(iu)){
	    		tg.setIntervalUnit(2);
	    	}else{
	    		tg.setIntervalUnit(0);
	    	}
	    	int interval = 0;
	    	try{
	    		interval = Integer.parseInt(tgnode.attributeValue("interval"));
	    	}catch(Exception e){
	    	}
	    	tg.setInterval(interval);
	    	tg.setExpression(tgnode.elementText("timeExp"));
	    	String st = "";
	    	try{
	    		st = tgnode.elementText("startTime");
	    	}catch(Exception e){}
	    	tg.setStartTime(st);
	    	js.setTrigger(tg);
	    }
	}
}
