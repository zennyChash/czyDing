package com.ifugle.etl.utils;

public class TaskException extends Exception {
	public TaskException(){}
	  /**
	   * @param msg 错误信息。
	   */
	  public TaskException(String msg)
	  {
	    super(msg);
	  }
}
