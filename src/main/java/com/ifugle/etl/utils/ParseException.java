package com.ifugle.etl.utils;

public class ParseException extends Exception {
	public ParseException(){}
	  /**
	   * 模板解析过程中的异常。
	   * @param msg 错误信息。
	   */
	  public ParseException(String msg)
	  {
	    super(msg);
	  }
}
