package com.ifugle.etl.schedule.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;

import com.ifugle.etl.entity.ScheduledJob;
import com.ifugle.etl.entity.SchedulerInfo;
import com.ifugle.etl.entity.component.TriggerInfo;
import com.ifugle.etl.schedule.jobs.CollectionJob;
import com.ifugle.etl.utils.TemplateLoader;

public class SchedulService {
	private static Logger log = LoggerFactory.getLogger(SchedulService.class);
	private Scheduler sched;
	private boolean schedulerIsReady = false;
	private List schedulersInfos = null;
	private static SchedulService sdService;
	private SchedulService(){}
	public static SchedulService getSchedulService() {
		if(sdService==null){
			sdService = new SchedulService();
		}
		return sdService;
	}
	public SchedulService(List schedulersInfos){
		this.schedulersInfos = schedulersInfos;
	}
	public void setSchedulers(List schedulersInfos){
		this.schedulersInfos = schedulersInfos;
	}
	public  void readyScheduler() throws Exception {
        try {
        	SchedulerFactory sf = new StdSchedulerFactory();
        	sched = sf.getScheduler();
            if(this.schedulersInfos==null||this.schedulersInfos.size()==0){
            	log.info("未定义任务调度信息，或任务调度信息格式解析失败！");
            	return;
            }
            //调度可能会分多个文件定义。但这里只用一个调度器工厂。逐个读取，循环注册
            for(int i=0;i<schedulersInfos.size();i++){
            	SchedulerInfo sduInfo = (SchedulerInfo)schedulersInfos.get(i);
            	if(sduInfo==null||sduInfo.getJobs()==null){
            		continue;
            	}
            	String schedId = sduInfo.getId();
            	log.info("初始化调度器："+schedId);
            	List jobs = sduInfo.getJobs();
            	//调度器内的任务
            	for(int j=0;j<jobs.size();j++){
            		ScheduledJob sdjob = (ScheduledJob)jobs.get(j);
            		if(sdjob==null){
            			log.error("第"+i+"个调度文件中的第"+j+"个调度任务缺少定义！");
            			continue;
            		}
            		if(sdjob.getDisabled()==1){
            			log.info("调度工作单元"+sdjob.getJobId()+"为禁用状态，不被调度执行。");
            			continue;
            		}
            		JobDetail jcDt = null;
        			//同步多任务的
        			String groupid = schedId;
    				jcDt = newJob(CollectionJob.class).withIdentity(sdjob.getJobId(), groupid)
    					      .build(); 
    				jcDt.getJobDataMap().put("schedulerId", schedId);
    				jcDt.getJobDataMap().put("jobId",sdjob.getJobId());
            		//jcDt.requestsRecovery(true);
            		//触发器类型
    				TriggerInfo tinfo = sdjob.getTrigger();
    				Trigger tg = getTrigger(tinfo,sdjob.getJobId(),groupid);
    				sched.scheduleJob(jcDt, tg);
            	}
            	schedulerIsReady=true;
            }
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
	}
	public Scheduler getScheduler(){
		return this.sched;
	}
	public void shutdown(){
		try{
			sched.shutdown(true);
		} catch (SchedulerException se) {
            se.printStackTrace();
        }	
	}
	public Trigger getTrigger(TriggerInfo tinfo,String sdJobId,String groupId){
		Trigger tg = null;
		if(tinfo.getType()==0){
			String startTime = tinfo.getStartTime();
			Date stDate = new Date(System.currentTimeMillis());
			if(!StringUtils.isEmpty(startTime)&&!"now".equalsIgnoreCase(startTime)){
				try{
					SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					stDate = sdf.parse(startTime);
					log.info("触发器开始时间:{}，格式化为：{}",stDate,startTime);
				}catch(Exception e){
					log.error("触发器开始时间配置格式错误，任务ID："+sdJobId+",时间表达式："+startTime+"。应符合yyyy-MM-dd HH:mm:ss格式。");
				}
			}
			int rCount = tinfo.getRepeat();
			int rInterval = tinfo.getInterval()<=0?1:tinfo.getInterval();
			int iu = tinfo.getIntervalUnit(); 
			TriggerBuilder tb_tg = newTrigger().withIdentity("smpTrigger"+sdJobId,groupId);
			if("now".equalsIgnoreCase(startTime)){
				tb_tg = tb_tg.startNow();
			}else{
				tb_tg = tb_tg.startAt(stDate);
			}
			if(iu==2){
				if(rCount>0){
					tb_tg = tb_tg.withSchedule(simpleSchedule().repeatHourlyForTotalCount(rCount+1, rInterval)); 
				}else if(rCount<0){
					tb_tg = tb_tg.withSchedule(simpleSchedule().repeatHourlyForever(rInterval));
				}
			}else if(iu==1){
				if(rCount>0){
					tb_tg = tb_tg.withSchedule(simpleSchedule().repeatMinutelyForTotalCount(rCount+1, rInterval)); 
				}else if(rCount<0){
					tb_tg = tb_tg.withSchedule(simpleSchedule().repeatMinutelyForever(rInterval));
				}
			}else{
				if(rCount>0){
					tb_tg = tb_tg.withSchedule(simpleSchedule().repeatSecondlyForTotalCount(rCount+1, rInterval)); 
				}else if(rCount<0){
					tb_tg = tb_tg.withSchedule(simpleSchedule().repeatSecondlyForever(rInterval));
				}
			}
			tg = tb_tg.build();
		}else{
			try{
				String timeExp = tinfo.getExpression();
				tg = newTrigger().withIdentity("cronTrigger"+sdJobId, groupId).withSchedule(cronSchedule(timeExp)).build(); 
			}catch(Exception e){
				log.error("触发器时间配置格式错误，任务ID："+sdJobId+",时间表达式："+tinfo.getExpression());
			}
		}
		return tg;
	}
    public static void main(String[] args) {
        SchedulService example = new SchedulService();
        TemplateLoader tl = TemplateLoader.getLoader();
		List scdl = tl.getETLObjectTemplates(2); 
		example.setSchedulers(scdl);
        try{
        	example.readyScheduler();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }
    public void startScheduler(){
    	if(schedulerIsReady&&sched!=null){
    		log.info("-------调度器开始调度 ----------------");
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		log.info(df.format(new Date()));
    		try{
    			sched.start();
    		} catch (SchedulerException se) {
                se.printStackTrace();
            }
    	}
    }
}
