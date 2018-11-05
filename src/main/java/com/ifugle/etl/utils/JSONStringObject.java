package com.ifugle.etl.utils;
import org.json.JSONString;
import org.apache.commons.lang.StringUtils;
public class JSONStringObject implements JSONString{

    private String jsonString = null;
    
    public JSONStringObject(String jsonString){
        this.jsonString = jsonString;
    }
    public String toString(){
        return jsonString;
    }

    public String toJSONString(){
        return jsonString;
    }
}
