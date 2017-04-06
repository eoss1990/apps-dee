package com.seeyon.apps.dee.manager;

import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.redo.model.RedoBean;
import com.seeyon.v3x.dee.common.db.redo.model.SyncBean;
import com.seeyon.v3x.dee.common.db.redo.model.SyncBeanLog;
import com.seeyon.v3x.dee.context.AdapterKeyName;
import com.seeyon.v3x.dee.util.FileUtil;
import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author 作者: zhanggong
 * @version 创建时间：2012-5-30 上午04:54:43 类说明
 */

public class DeeSynchronLogManagerImpl implements DeeSynchronLogManager {
	
	
    /**
     * DEE实例化
     */
    private static final DEEConfigService configService = DEEConfigService
            .getInstance();

    public SyncBean findSynchronLog(String syncId) {
        return configService.getSyncBySyncId(syncId);
    }

    @SuppressWarnings({ "static-access", "null" })
    public FlipInfo findSynchronLog(FlipInfo fi, Map<String, Object> param) {
        
        String flowId = null;
        String condition = null;
        String textfield = null;
        String textfield1 = null;
        Map<String, Object> map = configService.getSyncLogList(flowId, fi.getPage(), fi.getSize());
        @SuppressWarnings("unchecked")
        List<SyncBean> syncBeans = (List<SyncBean>)map.get(configService.MAP_KEY_RESULT);
        Long total = (Long) map.get(configService.MAP_KEY_TOTALCOUNT);
        
        List<SyncBean> resultFlowBean = new ArrayList<SyncBean>();

        if (StringUtils.isNotBlank(condition)
                && (StringUtils.isNotBlank(textfield) || StringUtils
                        .isNotBlank(textfield1))) {
            if ("byTaskName".equals(condition)) {
                for (SyncBean bean : syncBeans) {
                    if ((bean.getFlow_dis_name() == null ? "" : bean
                            .getFlow_dis_name()).toLowerCase().contains(
                            textfield.toLowerCase())) {
                        resultFlowBean.add(bean);
                    }
                }
            } else if ("byTime".equals(condition)) {
                for (SyncBean bean : syncBeans) {
                    Date syncDate = Datetimes
                            .parseDatetime(bean.getSync_time());
                    if (StringUtils.isNotBlank(textfield)
                            && StringUtils.isNotBlank(textfield1)) {
                        if (syncDate.compareTo(Datetimes
                                .getTodayFirstTime(textfield)) > 0
                                && syncDate.compareTo(Datetimes
                                        .getTodayLastTime(textfield1)) < 0) {
                            resultFlowBean.add(bean);
                        }
                    } else if (StringUtils.isNotBlank(textfield)
                            && syncDate.compareTo(Datetimes
                                    .getTodayFirstTime(textfield)) > 0) {
                        resultFlowBean.add(bean);
                    } else if (StringUtils.isNotBlank(textfield1)
                            && syncDate.compareTo(Datetimes
                                    .getTodayLastTime(textfield1)) < 0) {
                        resultFlowBean.add(bean);
                    }
                }
            }
        } else {
            resultFlowBean.addAll(syncBeans);
        }
        
        fi.setData(syncBeans);
        fi.setTotal(total.intValue());
        return fi;

    }
    
    @SuppressWarnings({ "static-access", "null" })
    @AjaxAccess
    public FlipInfo findSynchronLogByConditions(FlipInfo fi, Map<String, Object> param) {
        Map<String, Object> map = configService.getSyncLogList(param, fi.getPage(), fi.getSize());
        @SuppressWarnings("unchecked")
        List<SyncBeanLog> syncBeans = (List<SyncBeanLog>)map.get(configService.MAP_KEY_RESULT);
        Long total = (Long) map.get(configService.MAP_KEY_TOTALCOUNT);
        
        fi.setData(syncBeans);
        fi.setTotal(total.intValue());
        return fi;

    }
    
    @SuppressWarnings({ "static-access", "null" })
    @AjaxAccess
    public List<Map<String, Object>> getAdapterListTree(Map<String, String> map) {
        AdapterKeyName adapterKeyName = AdapterKeyName.getInstance();
    	String id = map.get("sysId");
    	String flowId = map.get("flowId");
    	List<Map<String, Object>> wapperMapList = new ArrayList<Map<String, Object>>();
    	String path = "";
        boolean isA8 = FileUtil.isA8Home();
        if(isA8){
        	path = adapterKeyName.getA8Home() + "/base/dee/flowLogs/" + adapterKeyName.getFlowMap().get(flowId) + "_" + flowId + "/";
        }else{
        	path = adapterKeyName.getDeeHome() + "/flowLogs/" + adapterKeyName.getFlowMap().get(flowId) + "_" + flowId + "/";
        }
    	Properties prop = new Properties();
        try {
            path = path + id + ".properties";
            File file = new File(path);
            if (!file.exists()){
            	return wapperMapList;
            }
            InputStream fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();
        	Map<String, Object> rootMap = new HashMap<String, Object>();
        	rootMap.put("id", 0);
        	String flowName = adapterKeyName.getFlowMap().get(flowId);
        	rootMap.put("name", flowName);
        	rootMap.put("parentId", -1);
			wapperMapList.add(rootMap);
			Map<String, Object> startMap = new HashMap<String, Object>();
			startMap.put("id", "startData");
			startMap.put("name", "来源数据");
			startMap.put("parentId", 0);
			startMap.put("state", -1);
			wapperMapList.add(startMap);
        	Map<Integer, Map<String, Object>> adapterMap = new HashMap<Integer, Map<String, Object>>();
        	Enumeration enums = prop.propertyNames();
        	while(enums.hasMoreElements()){
        	    String key = (String)enums.nextElement();
        	    if(key.contains("_") && key.contains(".state")){
    				Map<String, Object> tmpMap = new HashMap<String, Object>();
    				tmpMap.put("id", key);
    				String str =  key.split(",")[1];
            		String sId = str.substring(0, str.length() - 6);
        			tmpMap.put("name", key.split(",")[1].replace(".state", ""));
    				tmpMap.put("parentId", 0);
    				tmpMap.put("state", prop.getProperty(key));
    				adapterMap.put(Integer.parseInt(key.split(",")[0].replace("adapter_", "")), tmpMap);
        	    }
        	}
        	for(int i = 1; i <= adapterMap.size(); i++){
        		wapperMapList.add(adapterMap.get(i));
        	}
        	Map<String, Object> endMap = new HashMap<String, Object>();
        	endMap.put("id", "endData");
        	endMap.put("name", "输出数据");
        	endMap.put("parentId", 0);
			endMap.put("state", -1);
			wapperMapList.add(endMap);
        } catch (Exception e) {
			e.printStackTrace();
		}

        return wapperMapList;
    }
    
    @SuppressWarnings({ "static-access", "null" })
    @AjaxAccess
    public Map<String, String> getAdapterDetail(String str) {
        AdapterKeyName adapterKeyName = AdapterKeyName.getInstance();
    	String[] strs = str.split(",");
    	String id = strs[0];
    	String adapterName = strs[1];
    	String flowId = "";
    	if(strs.length == 3){
        	flowId = strs[2];
    	}else if(strs.length == 4){
    		flowId = strs[3];
    	}
    	Map<String, String> map = new HashMap<String, String>();
    	String path = "";
        boolean isA8 = FileUtil.isA8Home();
        if(isA8){
        	path = adapterKeyName.getA8Home() + "/base/dee/flowLogs/" + adapterKeyName.getFlowMap().get(flowId) + "_" + flowId + "/";
        }else{
        	path = adapterKeyName.getDeeHome() + "/flowLogs/" + adapterKeyName.getFlowMap().get(flowId) + "_" + flowId + "/";
        }
    	Properties prop = new Properties();
        try {
            path = path + id + ".properties";
            File file = new File(path);
            if (!file.exists()){
            	return map;
            }
            InputStream fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();
            if(adapterName.contains("_")){
                String name = adapterName + "," + strs[2].substring(0, strs[2].length() - 6);
            	map.put("adapterName", adapterName);
                map.put("data", prop.getProperty( name + ".data"));
                map.put("parm", prop.getProperty( name + ".parm"));
            } else {
            	map.put("adapterName", adapterName);
            	map.put("data", prop.getProperty(adapterName + ".data"));
                map.put("parm", prop.getProperty(adapterName + ".parm"));
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
        return map;
    }

    @SuppressWarnings({ "unchecked", "static-access" })
    public List<RedoBean> findRedoList(String syncId, String[] redoStates,String condition, String textfield, String textfield1) {
        List<RedoBean> resultList = new ArrayList<RedoBean>();
        List<RedoBean> redoBeans = new ArrayList<RedoBean>();
        try{
        	for (String redoState : redoStates) {
                Map<String, Object> map = configService.getRedoList(syncId,
                        redoState, 1, 1);
                Long totalCount = (Long) map.get(configService.MAP_KEY_TOTALCOUNT);
                List<RedoBean> tempList = new ArrayList<RedoBean>();
                tempList = (List<RedoBean>) configService.getRedoList(syncId,
                        redoState, 1, totalCount.intValue()).get(
                        configService.MAP_KEY_RESULT);
                for (RedoBean rb : tempList) {
                    redoBeans.add(rb);
                }
            }
            if (StringUtils.isNotBlank(condition)
                    && StringUtils.isNotBlank(textfield)) {
                if ("byTaskId".equals(condition)) {
                    for (RedoBean redoBean : redoBeans) {
                        if (redoBean.getRedo_id().contains(textfield)) {
                            resultList.add(redoBean);
                        }
                    }
                } else if ("byStatus".equals(condition)) {
                    for (RedoBean redoBean : redoBeans) {
                        if (redoBean.getState_flag().equals(textfield)) {
                            resultList.add(redoBean);
                        }
                    }
                }
            } else {
                resultList.addAll(redoBeans);
            }
        }catch(Exception e){
        	 Log.error("DEE异常信息列表详细获取失败：", e);
        }
        

        return resultList;
    }

    @SuppressWarnings({ "unchecked", "static-access" })
    public FlipInfo findRedoList(FlipInfo fi, Map<String, Object> param) {
        String[] redoStates = { RedoBean.STATE_FLAG_FAILE,
                RedoBean.STATE_FLAG_SKIP };
        List<RedoBean> list = new ArrayList<RedoBean>();
        List<RedoBean> tempList = new ArrayList<RedoBean>();
        String syncId = null;
        Long totalCount = 0L;
        Long total = 0L;
        try{
        	 Object syncIdObj = param.get("syncId");
             if (syncIdObj != null) {
                 syncId = String.valueOf(syncIdObj);
             }
             for (String redoState : redoStates) {
                 Map<String, Object> map = configService.getRedoList(syncId,
                         redoState, fi.getPage(), fi.getSize());
                 totalCount = (Long) map.get(configService.MAP_KEY_TOTALCOUNT);
                 tempList = (List<RedoBean>) map.get(configService.MAP_KEY_RESULT);
                 total += totalCount;
                 list.addAll(tempList);
             }
        }catch(Exception e){
        	 Log.error("DEE异常信息列表详细获取失败：", e);
        }
        fi.setData(list);
        fi.setTotal( total.intValue());
        return fi;
    }

    public RedoBean findRedoById(String redoId) {
        return configService.getRedoByRedoId(redoId);
    }

    public Boolean updateRedoBean(RedoBean rb) {
        return configService.updateRedoBean(rb);
    }

    public Map<String, String> delSyncBySyncId(String syncId)  {
        Map<String, String> retMap = new HashMap<String, String>();
        try {
            configService.delSyncBySyncId(syncId);
            retMap.put("ret_code", "2000");
        }catch (Exception e){
            Log.error("DEE数据源刷新出错：", e);
            retMap.put("ret_code", "2001");
            retMap.put("ret_desc", e.getLocalizedMessage());
        }
        return retMap;
    }
    
    @AjaxAccess
    public Map<String, String> delAllBySyncId(String syncId, String flowId)  {
        AdapterKeyName adapterKeyName = AdapterKeyName.getInstance();
        Map<String, String> retMap = new HashMap<String, String>();
        try {
        	String[] syncIds = syncId.split(",");
        	String[] flowIds = flowId.split(",");
            boolean isA8 = FileUtil.isA8Home();
        	String path = "";
        	String id = "";
        	String fid = "";
        	for(int i = 0; i < syncIds.length; i++){
            	id = syncIds[i];
            	fid = flowIds[i];
            	if(!"".equals(id)){
            		if(isA8){
            			path = adapterKeyName.getA8Home() + "/base/dee/flowLogs/" + adapterKeyName.getFlowMap().get(fid) + "_" + fid + "/";
                    }else{
                    	path = adapterKeyName.getDeeHome() + "/flowLogs/" + adapterKeyName.getFlowMap().get(fid) + "_" + fid + "/";
                    }
                    path = path + id + ".properties";
                    File file = new File(path);
                    if (file.exists()){
                    	file.delete();
                    }
            	}
        	}
            
            configService.delSyncBySyncId(syncId);
            retMap.put("ret_code", "2000");
        }catch (Exception e){
            Log.error("DEE数据源刷新出错：", e);
            retMap.put("ret_code", "2001");
            retMap.put("ret_desc", e.getLocalizedMessage());
        }
        return retMap;
    }
    
    @AjaxAccess
    public Map<String, String> deeRedoOrIgnoreMore(String redosId)  {
    	String[] redoIds = redosId.split(",");
    	// 重新发起失败任务(多态)
        Map<String, String> hm = configService.redo(redoIds);
        String baseName = "com.seeyon.apps.dee.resources.i18n.DeeResources";
        String retMsg = "";
        Map<String, String> retMap = new HashMap<String, String>();
        retMsg = ResourceBundleUtil.getString(baseName, "dee.synchronLog.redoSuccess.label");
        retMsg +="," + "success";
        retMap.put("ret_code", "2000");
        if (hm != null) {
            int i = 0;
            int flag = -1;
            for (String redoId : redoIds) {
                if (StringUtils.isNotBlank(hm.get(redoId))) {
                	if(i == 0){
                        retMsg = ResourceBundleUtil.getString(baseName, "dee.synchronLog.redoFailed.label");
                        retMap.put("ret_code", "2001");
                        flag = 1;
                	}
                    i++;
                }
            }
            if(flag == 1){
                String failedNum = ResourceBundleUtil.getString(baseName, "dee.synchronLog.redoFailedNum.label");
                retMsg +=failedNum+i+"," + "failed";
            }
        }
        retMap.put("ret_desc", retMsg);
        return retMap;
    }

    public Map<String, String> delSyncByRedoId(Map<String, String> map)
            throws TransformException {
        String syncId = map.get("syncId");
        String redoId = map.get("redoId");
        Map<String, String> retMap = new HashMap<String, String>();
        try{
            configService.delSyncByRedoId(syncId, redoId);
            retMap.put("ret_code", "2000");
        }catch (Exception e){
            Log.error("DEE数据源刷新出错：", e);
            retMap.put("ret_code", "2001");
            retMap.put("ret_desc", e.getLocalizedMessage());
        }
        return retMap;
    }
}
