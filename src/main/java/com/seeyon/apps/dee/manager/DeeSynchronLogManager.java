package com.seeyon.apps.dee.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.common.db.redo.model.RedoBean;
import com.seeyon.v3x.dee.common.db.redo.model.SyncBean;


/** 
 * @author 作者: zhanggong 
 * @version 创建时间：2012-5-30 上午04:53:24 
 * 类说明 
 */

public interface DeeSynchronLogManager {
	
	/**
	 * 功能：根据id获取 日志
	 * @param flowId
	 * @return
	 */
	public SyncBean findSynchronLog(String flowId);
	/**
	 * 查询同步历史
	 * @param flowId 同步任务ID
	 * @param condition
	 * @param textfield
	 * @return SyncBean集合
	 */
	public FlipInfo findSynchronLog(FlipInfo fi, Map<String, Object> param);
	/**
	 * 根据条件查询同步历史
	 * @param condition
	 * @return SyncBean集合
	 */
	public FlipInfo findSynchronLogByConditions(FlipInfo fi, Map<String, Object> param);
	/**
     * 获取任务的适配器树
     *
     * @param map 查询条件
     * @return java.util.List
     */
    public List<Map<String, Object>> getAdapterListTree(Map<String, String> map);
	/**
	 * 查询重发列表
	 * @param syncId 同步历史ID，用于查找相应批次的重发列表
	 * @param redoState 查询的重发记录的状态数组
	 * @param condition 
	 * @param textfield
	 * @return RedoBean集合
	 */
	public List<RedoBean> findRedoList(String syncId, String[] redoStates, String condition, String textfield, String textfield1);
	public FlipInfo findRedoList(FlipInfo fi, Map<String, Object> param);
	/**
	 * 功能 ：根据SyncID删除整个重发任务信息
	 * @param syncId
	 * @throws TransformException
	 */
	public Map<String, String> delSyncBySyncId(String syncId) throws TransformException;
	/**
	 * 功能 ：根据SyncID删除任务信息及日志信息
	 * @param syncId, flowId
	 * @throws TransformException
	 */
	public Map<String, String> delAllBySyncId(String syncId, String flowId) throws TransformException;
	/**
	 * 功能 ：根据redosId批量重发
	 * @param syncId
	 * @throws TransformException
	 */
	public Map<String, String> deeRedoOrIgnoreMore(String redosId);
	/**
	 * 功能：根据redoId删除任务下的子任务
	 * @param ids
	 * @throws TransformException
	 */
	public Map<String, String> delSyncByRedoId(Map<String, String> map) throws TransformException;
	/**
	 * 功能:根据id查询Redo
	 * @param redoId
	 * @return
	 */
	public RedoBean findRedoById(String redoId);
	/**
	 * 功能：更新Redo
	 * @param rb
	 * @return
	 */
	public Boolean updateRedoBean(RedoBean rb);
	/**
     * 功能：查询异常信息
     * @param rb
     * @return
     */
    
   
  
	
}
