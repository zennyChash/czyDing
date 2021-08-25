package com.ifugle.czyDing.utils.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
public class TreeNodeMapper implements RowMapper<TreeNode> {  
    @Override  
    public TreeNode mapRow(ResultSet rs, int rowNum) throws SQLException {  
    	TreeNode tn = new TreeNode();  
        tn.setId(rs.getString("ID"));
        tn.setText(rs.getString("NAME"));
        tn.setLeaf(rs.getBoolean("ISLEAF"));
        tn.setPid(rs.getString("PID"));
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
