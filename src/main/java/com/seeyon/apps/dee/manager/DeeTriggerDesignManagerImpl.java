package com.seeyon.apps.dee.manager;

import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.code.model.FlowTypeBean;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeeTriggerDesignManagerImpl implements DeeTriggerDesignManager {

    @Override
    public List<Map<String, Object>> getFlowListTree(Map<String, String> map) {
        List<FlowTypeBean> flowTypeList = DEEConfigService.getInstance().getFlowTypeList();
        if (flowTypeList == null) {
            flowTypeList = new ArrayList<FlowTypeBean>();
        }

        List<Map<String, Object>> wapperMapList = new ArrayList<Map<String, Object>>();
        for (FlowTypeBean flowTypeBean : flowTypeList) {
            Map<String, Object> tmpMap = new HashMap<String, Object>();
            tmpMap.put("id", flowTypeBean.getFLOW_TYPE_ID());
            tmpMap.put("name", flowTypeBean.getFLOW_TYPE_NAME());
            tmpMap.put("parentId", flowTypeBean.getPARENT_ID());
            wapperMapList.add(tmpMap);
        }

        return wapperMapList;
    }

    @Override
    public FlipInfo getFlowList(FlipInfo flipInfo, Map<String, Object> param) {
        String flowType = getValue(param, "flowType");
        String flowName = getValue(param, "flowName");
        flowName = Functions.urlDecoder(flowName);

        Map<String, Object> listObj = DEEConfigService.getInstance().getFlowList(
                ("-1".equals(flowType) ? null : flowType), null,
                flowName, flipInfo.getPage(), flipInfo.getSize());

        if (listObj != null) {
            List<FlowBean> flowList = (List<FlowBean>) listObj.get(DEEConfigService.MAP_KEY_RESULT);
            flowList = DEEConfigService.getInstance().getFlowList(flowList);
            flipInfo.setData(flowList);

            int rowCount = Integer.parseInt(listObj.get(DEEConfigService.MAP_KEY_TOTALCOUNT).toString());
            flipInfo.setTotal(rowCount);
        }

        return flipInfo;
    }

    /**
     * 从参数键值对中，取出key关联的value，返回值不会为null
     *
     * @param param 键值对
     * @param key   key
     * @return value
     */
    private String getValue(Map<String, Object> param, String key) {
        if (param != null) {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                if (entry.getKey().equals(key)) {
                    Object tmp = entry.getValue();
                    return tmp == null ? "" : tmp.toString();
                }
            }
        }
        return "";
    }
}

