package com.ifugle.etl.task.extract.entity;

import java.io.*;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifugle.etl.entity.component.*;
import com.ifugle.etl.utils.ETLFileUtils;

public class FileWriter {
	private static Logger log = LoggerFactory.getLogger(FileWriter.class);
	//存储目录
	private String dir;
	private String rootDir;
	private String subDir;
	private boolean deleteDuplicate;
	//记录总数
	private int recordCount;
	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	public boolean isDeleteDuplicate() {
		return deleteDuplicate;
	}

	public void setDeleteDuplicate(boolean deleteDuplicate) {
		this.deleteDuplicate = deleteDuplicate;
	}

	//文件数，分批保存为文件时使用
	private int fileCount = 0;
	//保存编码
	private String encode = "GBK";
	//每个文件的记录数
	private int rowsPerFile = 10000;
	//实际的文件中行数，计数器。
	private int rowsInFile = 0;
	private int outputCount = 0;
	//输出列的配置
	private List saveColumns;
	private File dataFile;
	private FileOutputStream dataOutputStream;
	private String colSeparator="|";

	/**
	 * 按文件输出记录行
	* @param dataList
	* @return
	* @throws Exception
	 */
	public boolean write(List dataList) throws IOException {
		try {
			if (dataFile == null) {
				this.dir=ETLFileUtils.getInstance().createSubDir(deleteDuplicate,rootDir,subDir);
				dataFile = new File(this.dir + "Fugle_" + (++fileCount) + ".txt");
				dataFile.createNewFile();
				dataOutputStream = new FileOutputStream(dataFile);
				rowsInFile = 0;
			}
			for (int i = 0; i<dataList.size(); i++) {
				List row = (List) dataList.get(i);
				if(row==null||row.size()==0)
					continue;
				String content = this.buildRowString(row);
				dataOutputStream.write(content.getBytes(encode));
				dataOutputStream.write("\r\n".getBytes(encode));
				rowsInFile++;
				outputCount++;
				if(rowsPerFile>0&&rowsInFile >= rowsPerFile){//rowsPerFile设置为非负数，才处理分文件。
					dataOutputStream.close();
					if(outputCount<recordCount){
						dataFile = new File(this.dir + "Fugle_" + (++fileCount)+ ".txt");
						dataFile.createNewFile();
						dataOutputStream = new FileOutputStream(dataFile);
						rowsInFile = 0;
					}
				}
				if(outputCount>=recordCount){
					dataOutputStream.close();
				}
			}
		} catch (IOException ex) {
			dataOutputStream.close();
			log.error("将数据写入文件时发生错误：{} ", ex);
			return false;
		}
		return true;
	}

	/**
	 * 记录输出结束，输出一个文件，记录总体情况。
	 * 
	 * @throws Exception
	 */
	public void finishWriteFile() throws Exception {
		try {
			// 开始写info.txt
			File infoFile = new File(this.dir + "Fugle_Info.txt");
			infoFile.createNewFile();
			FileOutputStream infoStream = new FileOutputStream(infoFile);
			StringBuffer info = new StringBuffer();
			info.append("[Infos]").append("\r\n");
			info.append("RecordCount=").append(this.recordCount).append("\r\n");
			info.append("RecordPerFile=").append(this.rowsPerFile).append("\r\n");
			info.append("DataFileCount=").append(this.fileCount ).append("\r\n");
			info.append("ColumnSeparator=").append(this.colSeparator).append("\r\n");
			info.append("FiledsName=");
			for (int i = 0, n = saveColumns.size(); i < n; i++) {
				Column col = (Column)saveColumns.get(i);
				if (i > 0) {
					info.append(colSeparator);
				}
				info.append(col.getDest());
			}
			info.append("\r\n");
			infoStream.write(info.toString().getBytes(encode));
			infoStream.close();
		} catch (IOException ex) {
			throw new Exception("输出文件元数据信息时发生错误：{}", ex);
		}
	}
	/**
	 * 按记录构造行
	* @param row
	* @return
	 */
	private String buildRowString(List row) {
		String[] s = new String[saveColumns.size()];
		for (int i = 0;i<saveColumns.size();i++) {
			String value = (String)row.get(i);
			if (value == null) {
				s[i] = "null";
			} else {
				s[i] = value;
			}
		}
		return StringUtils.join(s, colSeparator);
	}

	public String getDir() {
		return dir;
	}

	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public int getRowsPerFile() {
		return rowsPerFile;
	}
	public void setRowsPerFile(int rowsPerFile) {
		this.rowsPerFile = rowsPerFile;
	}
	public List getSaveColumns() {
		return saveColumns;
	}
	public void setSaveColumns(List saveColumns) {
		this.saveColumns = saveColumns;
	}
	public String getColSeparator() {
		return colSeparator;
	}

	public void setColSeparator(String colSeparator) {
		this.colSeparator = colSeparator;
	}
	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}
}
