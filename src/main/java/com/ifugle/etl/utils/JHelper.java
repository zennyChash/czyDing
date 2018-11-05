package com.ifugle.etl.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JHelper {
	public String format(long ltime,String fmt){
		String str = "";
		Date d = new Date();
		d.setTime(ltime);
		SimpleDateFormat sf = new SimpleDateFormat(fmt); 
		str = sf.format(d);
		return str;
	}
	public String format(){
		String str = "";
		Date d = new Date();
		str = format(d.getTime(),"yyyy-MM-dd hh:mm:ss");
		return str;
	}
	public String format(String format){
		String str = "";
		Date d = new Date();
		str = format(d.getTime(),format);
		return str;
	}
}
