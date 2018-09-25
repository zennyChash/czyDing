package com.ifugle.czy.utils.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
public class TreeNodeCheckableMapper implements RowMapper<TreeNode> {  
    @Override  
    public TreeNodeCheckable mapRow(ResultSet rs, int rowNum) throws SQLException {  
    	TreeNodeCheckable tn = new TreeNodeCheckable();  
        tn.setId(rs.getString("ID"));
        tn.setText(rs.getString("NAME"));
        tn.setLeaf(rs.getBoolean("ISLEAF"));
        tn.setPid(rs.getString("PID"));
        try{
        	tn.setChecked(rs.getBoolean("CHECKED"));
        }catch(Exception e){
        }
        try{
        	tn.setExpanded(rs.getBoolean("EXPANDED"));
        }catch(Exception e){
        }
        try{
        	tn.setCls(rs.getString("CLS"));
        }catch(Exception e){
        }
        return tn;  
    }  
}
