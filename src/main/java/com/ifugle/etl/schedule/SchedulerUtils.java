package com.ifugle.etl.schedule;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.*;

import com.ifugle.etl.entity.component.Parameter;
import com.ifugle.etl.utils.JHelper;

public class SchedulerUtils {
	private static Logger log = LoggerFactory.getLogger(SchedulerUtils.class);
	private final static String day = "$DAY";
	private final static String mon = "$MON";
	private final static String year = "$YEAR";
	private final static String hour = "$HOUR";
	private final static String min = "$MIN";
	private final static String sec = "$SEC";
	private final static String date = "$DATE";
	private final static String time = "$NOW";
	public static String parseParam(Parameter p){
		if(p==null){
    		return "";
    	}
    	String exp = p.getExpression();
    	if(p.getType()==1){
    		return exp;
    	}else{
    		return parseParamExpression(exp);
    	}
	}
    private static String parseParamExpression(String exp){
    	String v = "";
    	Calendar now = Calendar.getInstance(); 
    	long tasktime=now.getTime().getTime();  
    	//设置日期输出的格式  
    	Context cx = Context.enter();
    	Scriptable scope = cx.initStandardObjects();
    	String [] funcExps = StringUtils.substringsBetween(exp,"('","')");
    	if(funcExps!=null&&funcExps.length>0){
    		String fmt = funcExps[0];
    		JHelper jh = new JHelper();
    		String vt = jh.format(fmt);
    		return vt;
    	}
    	int iyear = now.get(Calendar.YEAR);
        int imon = now.get(Calendar.MONTH )+ 1;
        int iday = now.get(Calendar.DAY_OF_MONTH);
        int ihour = now.get(Calendar.HOUR_OF_DAY);
        int imin = now.get(Calendar.MINUTE);
        int isec = now.get(Calendar.SECOND);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        String sdate = sf.format(now.getTime());
        scope.put(day, scope, iday);
        scope.put(mon, scope, imon);
        scope.put(year, scope, iyear);
        scope.put(hour, scope, ihour );
        scope.put(min, scope, imin);
        scope.put(sec, scope, isec);
        scope.put(date, scope, sdate);
        scope.put(time, scope, tasktime);
        try{
        	final Script script = cx.compileString(exp, "console", 1, null);
        	final Object returned = script.exec(cx, scope);
            if (returned != null && returned.toString().length() > 0 && ! (returned instanceof Undefined)) {
                v=returned.toString();
            }
	        if ("NaN".equalsIgnoreCase(v)||"Infinity".equalsIgnoreCase(v)) {
				v = "";
			}else{
				//多数情况下计算结果为数值，取整数部分。
				try{
					Double dv = new Double(v);
					int iv = dv.intValue();
					v = String.valueOf(iv);
				}catch(Exception e){
				}
			}
        }catch(Exception e){
        	log.info(e.toString());
        	v = parseStrParam(exp);
        }finally{
        	Context.exit();
        }
    	return v;
    }
    //如果用脚本方式解析出错，使用替换的方式。
    private static String parseStrParam(String exp){
    	String v = exp;
    	Calendar now = Calendar.getInstance(); 
    	int iyear = now.get(Calendar.YEAR);
        int imon = now.get(Calendar.MONTH )+ 1;
        int iday = now.get(Calendar.DAY_OF_MONTH);
        int ihour = now.get(Calendar.HOUR_OF_DAY);
        int imin = now.get(Calendar.MINUTE);
        int isec = now.get(Calendar.SECOND);
        v = StringUtils.replace(v,day, String.valueOf(iday));
        v = StringUtils.replace(v,mon, String.valueOf(imon));
        v = StringUtils.replace(v,year, String.valueOf(iyear));
        v = StringUtils.replace(v,hour, String.valueOf(ihour));
        v = StringUtils.replace(v,min, String.valueOf(imin));
        v = StringUtils.replace(v,sec, String.valueOf(isec));
        String smonth = imon>9?String.valueOf(imon):"0"+String.valueOf(imon);
        String sdate = iday>9?String.valueOf(iday):"0"+String.valueOf(iday);
        String shour = ihour>9?String.valueOf(ihour):"0"+String.valueOf(ihour);
        String smin = imin>9?String.valueOf(imin):"0"+String.valueOf(imin);
        String ssec = isec>9?String.valueOf(isec):"0"+String.valueOf(isec);
        v = StringUtils.replace(v,time, String.valueOf(iyear)+smonth+sdate+shour+smin+ssec);
    	return v;
    }
    /**
     * 解析参数。如果有外部传入的值，用值代替。
    * @param exp 要解析的表达式
    * @param paramVals 调度时解析过的参数名值对，供具体任务引用。
    * @return
     */
    public static String parseParamValue(String exp,Map paramVals){
    	if(StringUtils.isEmpty(exp)){
    		return "";
    	}
    	String s = exp;
    	String[] paras=StringUtils.substringsBetween(exp, "{", "}");
    	if(paras!=null&&paras.length>0){
    		for(int i=0;i<paras.length;i++){
    			String p = paras[i];
    			if(paramVals!=null&&paramVals.containsKey(p)){
    				String val = (String)paramVals.get(p);
    				s=exp.replaceAll("\\{\\w*\\}", val);
    			}
    		}
    	}
    	return s;
    }
    
    public static void main(String[] args){
    	String today = parseParamExpression("$DAY");
    	String yesterday = parseParamExpression("$DAY-1");
    	String fday = parseParamExpression("dateByFormat('yyyy/MM/dd hh:mm:ss')");
    	Context ct = Context.enter(); 
    	 Scriptable scope = ct.initStandardObjects(); 
    	 Object out = Context.javaToJS(System.out, scope); 
    	 ScriptableObject.putProperty(scope, "out", out); 
    	 ct.evaluateString(scope, "out.println('Successful!')", null, 1, null);
    	log.info("today:{},yesterday:{},formateDate:{}",today,yesterday,fday);
    }
}
