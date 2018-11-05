package com.ifugle.etl.entity.component;
import com.ifugle.etl.entity.base.ETLFile;
public class InPutFile extends ETLFile{
	//xml或excel格式导入时使用。可以控制导入步骤。
	//0/db：导入到数据库（默认）；1/file：只转化为txt文件；2/db_file:既导入数据库，也产生txt文件输出
	private int doType;
	//xml或excel格式导入时使用。产生中间txt数据文件放在哪个目录下。
	private String transDir;
	private String dataRootNode;
	private int startRow;
	private int endRow;
	public int getEndRow() {
		return endRow;
	}

	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	private int sheetIndex;
	public int getSheetIndex() {
		return sheetIndex;
	}

	public void setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}
	public int getDoType() {
		return doType;
	}

	public String getDataRootNode() {
		return dataRootNode;
	}

	public void setDataRootNode(String dataRootNode) {
		this.dataRootNode = dataRootNode;
	}

	public void setDoType(int doType) {
		this.doType = doType;
	}

	public String getTransDir() {
		return transDir;
	}

	public void setTransDir(String transDir) {
		this.transDir = transDir;
	}
}
