package com.ifugle.czy.utils.bean.template;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 存储过程的输入参数
 */
public class ProParaIn {
	//输入参数值是否引用外部参数。0：不引用外部参数，静态值。1：引用外部参数值
	private int referMode;
	//引用哪个外部参数
	private String referTo;
	//参数值。referMode=0时，直接从文本取，否则从参数定义集合中取
	private String value;
	//参数类型，referMode=0时，直接从文本中取dataType属性，否则参照引用的外部参数的类型
	private int dataType;
	/**
	 * 获取参数的数据类型。
	 * 如果是直接引用其它参数的，数据类型参照引用参数，此处不需要制定。
	 * 0：string；1：int；2：double；3：cursor
	 * @return 数据类型
	 */
	public int getDataType() {
		return dataType;
	}
	/**
	 * 
	 * @param dataType
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	/**
	 * 获取参数的引用方式。
	 * 0：不引用外部参数，是一个静态值。1：引用其他参数。
	 * @return 参数引用方式。
	 */
	public int getReferMode() {
		return referMode;
	}
	/**
	 * 
	 * @param referMode
	 */
	public void setReferMode(int referMode) {
		this.referMode = referMode;
	}
	/**
	 * 获取参数引用的内容。
	 * 引用的参数名，referMode=1时有意义。
	 * @return 获取参数引用的内容
	 */
	public String getReferTo() {
		return referTo;
	}
	/**
	 * 
	 * @param referTo
	 */
	public void setReferTo(String referTo) {
		this.referTo = referTo;
	}
	/**
	 * 获取参数的静态值。
	 * 参数的值，referMode=0时有意义。
	 * @return 参数值。
	 */
	public String getValue() {
		return value;
	}
	/**
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
