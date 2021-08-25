package com.ifugle.czyDing.utils.bean.template;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 存储过程的输出参数
 */
public class ProParaOut {
    //dataType:参数的数据类型。0：string；1：int；2：double；3：cursor
	private int dataType;
    /**
     * 参数的数据类型。
     * 0：string；1：int；2：double；3：cursor
     * @return 参数的数据类型
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
}
