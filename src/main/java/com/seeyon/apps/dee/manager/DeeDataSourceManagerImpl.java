package com.seeyon.apps.dee.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.v3x.dee.common.db.resource.dao.DeeResourceDAO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.dee.util.Constants;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.bean.A8MetaDatasourceBean;
import com.seeyon.v3x.dee.bean.ConvertDeeResourceBean;
import com.seeyon.v3x.dee.bean.JDBCResourceBean;
import com.seeyon.v3x.dee.bean.JNDIResourceBean;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.a8version.A8VersionManager;
import com.seeyon.v3x.dee.common.base.util.DBUtil;
import com.seeyon.v3x.dee.common.db.resource.model.DeeResource;
import com.seeyon.v3x.dee.common.db.resource.model.DeeResourceBean;
import com.seeyon.v3x.dee.common.db.resource.util.DeeResourceEnum;
import com.seeyon.v3x.dee.common.db2cfg.GenerationCfgUtil;
import com.seeyon.v3x.dee.datasource.JNDIDataSource;

public class DeeDataSourceManagerImpl implements DeeDataSourceManager {
	/**
	 * DEE实例化
	 */
	private static final DEEConfigService configService = DEEConfigService.getInstance();

    /**
     * 日志
     */
    private static final Log log = LogFactory.getLog(DeeDataSourceManagerImpl.class);

	@Override
	public List<DeeResourceBean> findDataSourceList() throws TransformException {
		return  configService.getAllDataResList();
	}
	public List<DeeResourceBean> findDataSourceList(String condition,String byDis_name) throws TransformException{
		List<DeeResourceBean> DeeResourceList = configService.getAllDataResList();
		List<DeeResourceBean> resultList = new ArrayList<DeeResourceBean>();
		if(StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(byDis_name)){
			if("byDis_name".equals(condition)){
				for(DeeResourceBean deeResourceBean :DeeResourceList){
					if((deeResourceBean.getDis_name() == null?"":deeResourceBean.getDis_name()).contains(byDis_name)){
						resultList.add(deeResourceBean);
					}
				}
			}
		}
		return resultList;
	}

	public DeeResourceBean findById(String id) throws TransformException{
        return configService.getResByResId(id);
	}

	public void deleteDataSource(String id) throws TransformException{
        new DeeResourceDAO().delDsById(id);
	}


    /**
     * 判断是否有是否有被引用
     * true:返回被引用数据源名称；false：返回""空串
     *
     * @throws TransformException
     */
    @Override
    public boolean isQuoteByFlow(String drId) throws Exception {
        boolean flag = new DeeResourceDAO().findByRefId(drId);
        return flag;
    }

    @Override
    public FlipInfo dataSourceList(FlipInfo fi, Map<String, Object> param){
        String dis_name = "";
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            if (entry.getKey().equals("dis_name")) {
                Object tmp = entry.getValue();
                dis_name = tmp == null ? "" : tmp.toString();
            }
        }


        try {
            List<DeeResourceBean> datas = new ArrayList<DeeResourceBean>();
            List<DeeResourceBean> result = findDataSourceList();
            List<DeeResourceBean> resultList = new ArrayList<DeeResourceBean>();

            if(Strings.isNotBlank(dis_name))
            {
                for (DeeResourceBean db : result) {
                    if ((db.getDis_name() == null ? "" : db.getDis_name()).contains(dis_name)) {
                        resultList.add(db);
                    }
                }
            }else
            {
                resultList=result;
            }

            for (int i = 0; i < resultList.size(); i++) {
                //这里进行内存分页
                if (i >= (fi.getPage() - 1) * fi.getSize() && i < fi.getPage() * fi.getSize()) {
                    datas.add(resultList.get(i));
                }
            }
            for (DeeResourceBean bean : datas) {
                DeeResource deeResourceSubBean = new ConvertDeeResourceBean(bean).getResource();
                bean.setDr(deeResourceSubBean);
            }
            fi.setData(datas);
            fi.setTotal(resultList.size());
        } catch (TransformException e) {
            e.printStackTrace();
        }
        return fi;
    }

    /**
     * 更新数据源
     *
     * @param map 传入参数
     * @return
     */
    @Override
    public Map<String, String> updateDataSource(Map<String, String> map) {
        String flag =  map.get("isA8Meta");
        boolean state = false;
        if(flag == null){
            state = true;
        }
        if (Boolean.TRUE.toString().equals(flag)){
            Connection con = null;
            try{
                JDBCResourceBean rb = new JDBCResourceBean();
                rb.setDriver(map.get("driver"));
                rb.setUrl(map.get("url"));
                rb.setUser(map.get("user"));
                rb.setPassword(map.get("password"));
                Class.forName(rb.getDriver());
                con = DriverManager.getConnection(map.get("url"), map.get("user"), map.get("password"));
                if (con != null) {
                    String version = DBUtil.getA8VersionStr(con);
                    if (A8VersionManager.getInstance().exchange(version) != null) {
                        state = true;
                    }
                }
            }catch(Exception e){
                log.info(e.getMessage(), e);
                state = true;
            } finally {
                if (con != null) {
                    try{
                        con.close();
                    }catch(Exception e){
                        log.info(e.getMessage(), e);
                    }
                }
            }
        }
        Map<String, String> retMap = new HashMap<String, String>();
        if(state){
            DeeResourceBean bean = new DeeResourceBean();
            bean.setResource_id(map.get("resource_id"));
            bean.setDis_name(map.get("dis_name"));
            bean.setResource_template_id(map.get("resource_template_id"));
            if (map.get("isA8Meta") != null) {
                bean.setResource_template_id(Integer.toString(DeeResourceEnum.A8MetaDatasource.ordinal()));
            }
            setSubBean(map, bean);
            try {
                configService.updateRes(bean);
                // 更新Dee配置文件，重新加载
                GenerationCfgUtil.getInstance().generationMainFile(GenerationCfgUtil.getDEEHome());
                // 刷新DEE上下文
                DEEClient client = new DEEClient();
                client.refreshContext();
                retMap.put("ret_code", "2000");
            } catch (TransformException e) {
                log.error(e.getLocalizedMessage(), e);
                retMap.put("ret_code", "2001");
                retMap.put("ret_desc", e.getLocalizedMessage());
            } catch (Throwable e) {
                log.error("DEE数据源刷新出错：", e);
                retMap.put("ret_code", "2001");
                retMap.put("ret_desc", e.getLocalizedMessage());
            }
        }else{
            retMap.put("ret_code", "2002");
        }
        return retMap;
    }

    /**
     * 连接测试
     *
     * @param map 传入参数
     * @return
     */
    @Override
    public Integer testCon(Map<String, String> map) {
        Constants.DATASOURCE_CONNECTION_STATE state = Constants.DATASOURCE_CONNECTION_STATE.FAILURE;
        try {
            String a8Meta = map.get("a8Meta");
            String type = map.get("type");
            if (Boolean.TRUE.toString().equals(a8Meta)) {
                A8MetaDatasourceBean rb = new A8MetaDatasourceBean();
                rb.setDriver(map.get("driver"));
                rb.setUrl(map.get("url"));
                rb.setUser(map.get("user"));
                rb.setPassword(map.get("password"));
                rb.setJndi(map.get("jndi"));
                state = testA8MetaCon(rb);
            } else {
                if (String.valueOf(DeeResourceEnum.JDBCDATASOURCE.ordinal()).equals(type)) {
                    JDBCResourceBean rb = new JDBCResourceBean();
                    rb.setDriver(map.get("driver"));
                    rb.setUrl(map.get("url"));
                    rb.setUser(map.get("user"));
                    rb.setPassword(map.get("password"));
                    state = testJdbcCon(rb);
                } else if (String.valueOf(DeeResourceEnum.JNDIDataSource.ordinal()).equals(type)) {
                    JNDIResourceBean rb = new JNDIResourceBean();
                    rb.setJndi(map.get("jndi"));
                    state = testJNDICon(rb);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return state.ordinal();
    }

    private void setSubBean(Map<String, String> map, DeeResourceBean bean) {
        if (Integer.parseInt(bean.getResource_template_id()) == DeeResourceEnum.JDBCDATASOURCE.ordinal()) {
            JDBCResourceBean rb = new JDBCResourceBean();
            rb.setDriver(map.get("driver"));
            rb.setUrl(map.get("url"));
            rb.setUser(map.get("user"));
            rb.setPassword(map.get("password"));
            bean.setDr(rb);
        } else if (Integer.parseInt(bean.getResource_template_id()) == DeeResourceEnum.JNDIDataSource.ordinal()) {
            JNDIResourceBean rb = new JNDIResourceBean();
            rb.setJndi(map.get("jndi"));
            bean.setDr(rb);
        } else if (Integer.parseInt(bean.getResource_template_id()) == DeeResourceEnum.A8MetaDatasource.ordinal()) {
            A8MetaDatasourceBean rb = new A8MetaDatasourceBean();
            rb.setDriver(map.get("driver"));
            rb.setUrl(map.get("url"));
            rb.setUser(map.get("user"));
            rb.setPassword(map.get("password"));
            rb.setJndi(map.get("jndi"));
            bean.setDr(rb);
        }
    }

    /**
     * JDBC连接测试
     *
     * @param jdbcBean
     * @return 0：成功，1：失败
     * @throws Exception
     */
    private Constants.DATASOURCE_CONNECTION_STATE testJdbcCon(JDBCResourceBean jdbcBean) throws Exception {
        Connection con = null;
        try {
            Class.forName(jdbcBean.getDriver());
            //定义超时时间，单位为秒
            DriverManager.setLoginTimeout(5);
            con = DriverManager.getConnection(jdbcBean.getUrl(), jdbcBean.getUser(), jdbcBean.getPassword());
            if (con != null) {
                return Constants.DATASOURCE_CONNECTION_STATE.SUCCESS;
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return Constants.DATASOURCE_CONNECTION_STATE.FAILURE;
    }

    /**
     * JNDI连接测试
     *
     * @param jndiBean
     * @return 0：成功，1：失败
     * @throws Exception
     */
    private Constants.DATASOURCE_CONNECTION_STATE testJNDICon(JNDIResourceBean jndiBean) throws Exception {
        Connection con = null;
        try {
            JNDIDataSource ds = new JNDIDataSource();
            ds.setJndi(jndiBean.getJndi());
            con = ds.getConnection();

            if (con != null) {
                return Constants.DATASOURCE_CONNECTION_STATE.SUCCESS;
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return Constants.DATASOURCE_CONNECTION_STATE.FAILURE;
    }

    /**
     * A8元数据的数据源连接测试
     *
     * @param bean javaBean
     * @return 0：成功，1：数据源连接失败，2：数据源连接成功，但不是A8元数据
     * @throws Exception
     */
    private Constants.DATASOURCE_CONNECTION_STATE testA8MetaCon(A8MetaDatasourceBean bean) throws Exception {
        if (bean != null) {
            Connection con = null;
            try {
                if (bean.getDriver() != null) {             // JDBC
                    Class.forName(bean.getDriver());
                    // 定义超时时间，单位为秒
                    DriverManager.setLoginTimeout(5);
                    con = DriverManager.getConnection(bean.getUrl(), bean.getUser(), bean.getPassword());
                } else if (bean.getJndi() != null) {        // JNDI
                    JNDIDataSource jndiDataSource = new JNDIDataSource();
                    jndiDataSource.setJndi(bean.getJndi());
                    con = jndiDataSource.getConnection();
                }
                if (con != null) {
                    String version = DBUtil.getA8VersionStr(con);
                    if (A8VersionManager.getInstance().exchange(version) != null) {
                        return Constants.DATASOURCE_CONNECTION_STATE.SUCCESS;
                    } else {
                        return Constants.DATASOURCE_CONNECTION_STATE.NOT_A8META;
                    }
                }
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }
        return Constants.DATASOURCE_CONNECTION_STATE.FAILURE;
    }
}

