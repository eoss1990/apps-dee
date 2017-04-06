package com.seeyon.apps.dee.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.common.db.resource.model.DeeResourceBean;



/**
 * 功能说明：对Dee数据源的操作
 * @author XQ
 *
 */
public interface DeeDataSourceManager {
	public List<DeeResourceBean> findDataSourceList() throws TransformException;
	
	public List<DeeResourceBean> findDataSourceList(String condition,String byDis_name) throws TransformException;
	
	public DeeResourceBean findById(String id) throws TransformException;
	
	public void deleteDataSource(String id) throws TransformException;

    public FlipInfo dataSourceList(FlipInfo fi, Map<String, Object> param);

    /**
     * 更新数据源
     *
     * @param map
     * @return
     */
    public Map<String, String> updateDataSource(Map<String, String> map);

    /**
     * 连接测试
     *
     * @param map
     * @return
     */
    public Integer testCon(Map<String, String> map);


    /*
    * 是否被引用
    * @param String
    * */
    public boolean isQuoteByFlow(String drId) throws Exception;
}
