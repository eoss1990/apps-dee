package com.seeyon.apps.dee.manager;

import java.util.Map;

/**
 * 任务删除管理器
 *
 * @author zhangfb
 */
public interface DeeDeleteManager {
    /**
     * 删除任务
     *
     * @param flowIds 任务ID列表，如：“id1,id2,id3”
     * @return 提示信息
     * @throws Exception
     */
    Map<String, String> deleteFlow(String flowIds) throws Exception;

    public String getFlowName(String id);
}
