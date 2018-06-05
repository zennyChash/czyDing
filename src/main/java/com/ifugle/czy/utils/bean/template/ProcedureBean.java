package com.ifugle.czy.utils.bean.template;
import java.util.*;

public class ProcedureBean {
	 //存储过程名称
	private String name;       
    //存储过程在本系统中通常用于提供数据集合，dataSetIndex表示记录集游标位于输出参数的第几个。
	//输出参数索引以1开始
	private int dataSetIndex; 
	private int outPutInfoIndex;
	//用于取报表数据时，如果报表分页，且取数前分页，由存储过程负责输出总记录数
	//该属性用于获取报表总记录数的输出参数序号。所有输出参数在输入参数之后，输出参数索引以1开始。
	private int totalIndex;  
	
	public int getTotalIndex() {
		return totalIndex;
	}
	public void setTotalIndex(int totalIndex) {
		this.totalIndex = totalIndex;
	}
	/**
	 * 输入参数集合
	 */
	private List inParas;    
	/**
	 * 输出参数集合
	 */
	private List outParas;
	/**
	 * 存储过程返回参数中，记录集游标的索引号。
	 * 存储过程用于取数时，第几个输出参数是返回的记录集游标。<br>
	 * 这里的索引号从1开始计。
	 * @return 游标类型参数的索引号。
	 */
	public int getDataSetIndex() {
		return dataSetIndex;
	}
	/**
	 * 
	 * @param dataSetIndex
	 */
	public void setDataSetIndex(int dataSetIndex) {
		this.dataSetIndex = dataSetIndex;
	}
	/**
	 * 存储过程的输入参数集合。
	 * @return 输入参数集合
	 * @see com.datanew.query.report.ProParaIn
	 */
	public List getInParas() {
		return inParas;
	}
	/**
	 * 
	 * @param inParas
	 */
	public void setInParas(List inParas) {
		this.inParas = inParas;
	}
	/**
	 * 存储过程的名称。
	 * @return  存储过程名。
	 */
	public String getName() {
		return name;
	}
	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 存储过程的输出参数集合。
	 * @return  输出参数集合
	 * @see com.datanew.query.report.ProParaOut
	 */
	public List getOutParas() {
		return outParas;
	}
	public void setOutParas(List outParas) {
		this.outParas = outParas;
	}
	/**
	 * 用于参数加工过程，用过程加工参数时，参数转化后的内容，在输出参数中的索引
	 * @return 加工后的参数内容，在存储过程的输出参数中的序号。以1为起始。
	 */
	public int getOutPutInfoIndex() {
		return outPutInfoIndex;
	}
	/**
	 * 
	 * @param outPutInfoIndex
	 */
	public void setOutPutInfoIndex(int outPutInfoIndex) {
		this.outPutInfoIndex = outPutInfoIndex;
	}
}
