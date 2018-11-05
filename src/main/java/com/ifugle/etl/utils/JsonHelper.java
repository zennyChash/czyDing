package com.ifugle.etl.utils;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import com.ifugle.etl.utils.entity.SimpleBean;
import com.ifugle.etl.utils.JSONStringObject;
import com.google.gson.*;

public class JsonHelper {
	
	private static Logger log = LoggerFactory.getLogger(JsonHelper.class);
	private static JsonHelper instance = null;
    public JsonHelper(){
    	
    }
    public static JsonHelper getJsonHelper(){
    	if(instance==null){
    		instance = new JsonHelper();
    	}
    	return instance;
    }
    /** 
     * 代理类时做的检查.返回应该检查的对象.
     * @param bean
     * @return
     */
    protected Object proxyCheck(Object bean){
        return bean;
    }

    public String toJSONString(Object obj) throws JSONException{
        return toJSONString(obj, false);
    }
    
    public String toJSONString(Object obj, boolean useClassConvert) throws JSONException{
        if(instance == null)
            instance = new JsonHelper();
        return instance.getJSONObject(obj, useClassConvert).toString();
    }
    /**
     * 有序集合对象的转换
    * @param arrayObj
    * @param useClassConvert
    * @return
    * @throws JSONException
     */
    private String getJSONArray(Object arrayObj, boolean useClassConvert) throws JSONException{
        if(arrayObj == null)
            return "null";
        arrayObj = proxyCheck(arrayObj);
        JSONArray jSONArray = new JSONArray();
        if(arrayObj instanceof Collection){//集合内元素循环
            Iterator iterator = ((Collection)arrayObj).iterator();
            while(iterator.hasNext()){
                Object rowObj = iterator.next();
                if(rowObj == null){
                    jSONArray.put(new JSONStringObject(null));
                } else if(rowObj.getClass().isArray() || rowObj instanceof Collection){//嵌套集合元素，递归
                    jSONArray.put(getJSONArray(rowObj, useClassConvert));
                }else{
                    jSONArray.put(getJSONObject(rowObj, useClassConvert));
                }
            }
        }
        if(arrayObj.getClass().isArray()){
            int arrayLength = Array.getLength(arrayObj);
            for(int i = 0; i < arrayLength; i ++){
                Object rowObj = Array.get(arrayObj, i);
                if(rowObj == null){
                    jSONArray.put(new JSONStringObject(null));
            	}else if(rowObj.getClass().isArray() || rowObj instanceof Collection){
                    jSONArray.put(getJSONArray(rowObj, useClassConvert));
                }else{
                    jSONArray.put(getJSONObject(rowObj, useClassConvert));
                }
            }
        }
        return jSONArray.toString();
    }

    public JSONStringObject getJSONObject(Object value, boolean useClassConvert) throws JSONException{
        //处理原始类型
        if (value == null) {
            return new JSONStringObject("null");
        }
        value = proxyCheck(value);
        if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString)value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            throw new JSONException("Bad value from toJSONString: " + o);
        }
        if (value instanceof Number) {
            return new JSONStringObject(JSONObject.numberToString((Number) value));
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return new JSONStringObject(value.toString());
        }
        if (value instanceof String)
            return new JSONStringObject(JSONObject.quote(value.toString()));
        if (value instanceof Map) {
            JSONObject jSONObject = new JSONObject();
            Iterator iterator = ((Map)value).keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next().toString();
                Object valueObj = ((Map)value).get(key);
                jSONObject.put(key, getJSONObject(valueObj, useClassConvert));
            }
            return new JSONStringObject(jSONObject.toString());
        }
        //class
        if(value instanceof Class)
            return new JSONStringObject(JSONObject.quote(((Class)value).getName()));
        //数组
        if (value instanceof Collection || value.getClass().isArray()) {
            return new JSONStringObject(getJSONArray(proxyCheck(value), useClassConvert));
        }
        return reflectObject(value, useClassConvert);
    }

    private JSONStringObject reflectObject(Object bean, boolean useClassConvert){
        JSONObject jSONObject = new JSONObject();

        Class klass = bean.getClass();
        Method[] methods = klass.getMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                String name = method.getName();
                String key = "";
                if (name.startsWith("get")) {
                    key = name.substring(3);
                } else if (name.startsWith("is")) {
                    key = name.substring(2);
                }
                if (key.length() > 0 &&
                        Character.isUpperCase(key.charAt(0)) &&
                        method.getParameterTypes().length == 0) {
                    if (key.length() == 1) {
                        key = key.toLowerCase();
                    } else if (!Character.isUpperCase(key.charAt(1))) {
                        key = key.substring(0, 1).toLowerCase() +
                            key.substring(1);
                    }
                    Object elementObj = method.invoke(bean, null);
                    if(!useClassConvert && elementObj instanceof Class)
                        continue;

                    jSONObject.put(key, getJSONObject(elementObj, useClassConvert));
                }
            } catch (Exception e) {
                /**//* forget about it */
            }
        }
        return new JSONStringObject(jSONObject.toString());
    }
    
	public String getTreeNodesOfJson(List items,int loadAll,String pid)throws Exception {
		if(items==null)return "";
		JsonArray jItems=new JsonArray();
		try{
			if(loadAll==1){//一次载入，从顶级编码开始，递归的加入子节点
				for(int i=0;i<items.size();i++){
					SimpleBean oi=(SimpleBean)items.get(i);
					if(oi.getPid()==null||"".equals(oi.getPid())){
						jItems.add(parseOptionItem(items,oi,null,0));
					}
				}
			}else{
				for(int i=0;i<items.size();i++){
					SimpleBean oi=(SimpleBean)items.get(i);
					JsonObject ji=new JsonObject();
					ji.addProperty("id", oi.getBm());
					ji.addProperty("pid", oi.getPid());
					ji.addProperty("text", oi.getMc());
					ji.addProperty("leaf", oi.getIsLeaf()==1);
					ji.addProperty("cls",oi.getIsLeaf()==1?"file":"folder");
					ji.addProperty("expanded", oi.getExpand()==1);
					ji.addProperty("autoid", oi.getAutoid());
					ji.addProperty("href", oi.getHref());
					ji.addProperty("hrefTarget", oi.getTarget());
					if(!StringUtils.isEmpty(oi.getNodeIcon())){
						ji.addProperty("icon", oi.getNodeIcon());
					}
					jItems.add(ji);
				}
			}
		}catch(Exception e){
			throw new Exception("编码解析为JSON格式时发生错误："+e.toString());
		}
		return jItems.toString();
	}
	
	private JsonObject parseOptionItem(List initItems,SimpleBean oi,Map mcids,int checkById)throws Exception{
		if(oi==null)return null;
		JsonObject ji=new JsonObject();
		ji.addProperty("id", oi.getBm());
		ji.addProperty("pid", oi.getPid());
		ji.addProperty("text", oi.getMc());
		ji.addProperty("leaf", oi.getIsLeaf()==1);
		ji.addProperty("cls",oi.getIsLeaf()==1?"file":"folder");
		ji.addProperty("expanded", oi.getExpand()==1);
		ji.addProperty("autoid", oi.getAutoid());
		ji.addProperty("href", oi.getHref());
		ji.addProperty("hrefTarget", oi.getTarget());
		if(mcids!=null){
			if(checkById==1){
				ji.addProperty("checked", mcids.containsKey(String.valueOf(oi.getBm())));
			}else{
				ji.addProperty("checked", mcids.containsKey(String.valueOf(oi.getAutoid())));
			}
		}
		//检查当前节点的子节点
		String nextPid=oi.getBm();
		JsonArray cArray=new JsonArray();
		for(int i=0;i<initItems.size();i++){
			SimpleBean item=(SimpleBean)initItems.get(i);
			if(item!=null&&nextPid.equals(item.getPid())){//找到子节点，分别递归形成子节点分支的集合。
				cArray.add(parseOptionItem(initItems,item,mcids,checkById));
			}
		}
		if(cArray.size()>0){
			ji.add("children", cArray);
		}
		return ji;
	}
}
