package com.seeyon.apps.dee.manager;

import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;


/**
 * DEE 触发管理器
 *
 * @author zhangfb
 */
public interface DeeTriggerDesignManager {
    /**
     * 获取任务树
     *
     * @param map 查询条件
     * @return java.util.List
     */
    public List<Map<String, Object>> getFlowListTree(Map<String, String> map);

    /**
     * 获取任务列表
     *
     * @param flipInfo 分页信息
     * @param param    查询条件
     * @return FlipInfo
     */
    public FlipInfo getFlowList(FlipInfo flipInfo, Map<String, Object> param);
}
