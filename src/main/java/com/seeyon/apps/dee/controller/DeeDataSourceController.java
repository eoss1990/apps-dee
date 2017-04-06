package com.seeyon.apps.dee.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.dee.manager.DeeDataSourceManager;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.bean.A8MetaDatasourceBean;
import com.seeyon.v3x.dee.bean.ConvertDeeResourceBean;
import com.seeyon.v3x.dee.bean.JDBCResourceBean;
import com.seeyon.v3x.dee.bean.JNDIResourceBean;
import com.seeyon.v3x.dee.common.db.resource.model.DeeResource;
import com.seeyon.v3x.dee.common.db.resource.model.DeeResourceBean;
import com.seeyon.v3x.dee.common.db.resource.util.DeeResourceEnum;
import com.seeyon.v3x.dee.common.db.resource.util.SourceType;
import com.seeyon.v3x.dee.common.db.resource.util.SourceUtil;

/**
 * @author 作者: XQ
 * @version 创建时间：2012-7-12
 * 类说明 
 */
public class DeeDataSourceController extends BaseController {
	/**
	 * 日志
	 */
	private static final Log log = LogFactory.getLog(DeeDataSourceController.class);

    public static final String JDBC = Integer.toString(DeeResourceEnum.JDBCDATASOURCE.ordinal());
    public static final String JNDI = Integer.toString(DeeResourceEnum.JNDIDataSource.ordinal());

	private DeeDataSourceManager deeDataSourceManager;
	
	public DeeDataSourceManager getDeeDataSourceManager() {
		return deeDataSourceManager;
	}

	public void setDeeDataSourceManager(DeeDataSourceManager deeDataSourceManager) {
		this.deeDataSourceManager = deeDataSourceManager;
	}

    /**
	 * 功能：跳转到上下结构的frame页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView dataSourceFrame(HttpServletRequest request, HttpServletResponse response) throws Exception{
		//跳转到上下布局页面
		return new ModelAndView("plugin/dee/dataSource/dataSourceShowFrame");
	}
	
	/**
	 * 功能：获取数据跳转列表页
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView showDataSourceList(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView view = new ModelAndView("plugin/dee/dataSource/dataSourceList");
        
        Map param = new HashMap();
        FlipInfo fi = deeDataSourceManager.dataSourceList(new FlipInfo(), param);
        request.setAttribute("fflistDataSourceTable", fi);
        return view;
    }

	/**
	 * 功能：获取数据跳转数据源详细信息页面
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView showDataSourceDetail(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return loadDataSourceView(request, response, "plugin/dee/dataSource/dataSourceDetail");
	}

	/**
	 * 功能：获取数据跳转数据源修改页面
     *
	 * @param request 请求
	 * @param response 返回
	 * @return ModelAndView
	 * @throws Exception
	 */
	public ModelAndView showDataSourceUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return loadDataSourceView(request, response, "plugin/dee/dataSource/dataSourceUpdate");
    }

	public void dataSourceDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json; charset=UTF-8");
        String resource_ids = request.getParameter("id");// 获取数据是以冒号隔开的字符串
        String[] arrId = resource_ids.split(":");
        try {
            String retMsg = delDsById(arrId);
            if (retMsg == null)
                retMsg = "success";
            response.getWriter().write(retMsg);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
	}


    private String delDsById(String[] drId) throws Exception {
        boolean flag = false;
        StringBuilder dsName = new StringBuilder();
        for (String id : drId) {
            flag = deeDataSourceManager.isQuoteByFlow(id);
            if (flag) {
                DeeResourceBean dsb = deeDataSourceManager.findById(id);
                dsName.append(dsb.getDis_name()).append(",");
            }else {
                deeDataSourceManager.deleteDataSource(id);
            }
        }
        if (dsName.length() > 0) {
            return dsName.toString().substring(0,dsName.toString().length()-1);
        } else {
            return null;
        }
    }

    /**
     * 载入数据源数据
     * @param response
     * @param request
     * @param url
     * @return
     * @throws TransformException
     */
    private ModelAndView loadDataSourceView(HttpServletRequest request, HttpServletResponse response, String url) throws TransformException {
        ModelAndView view = new ModelAndView(url);

        // 获取要修改的数据源ID
        String id = request.getParameter("id");
        // 获得数据源对象
        DeeResourceBean deeResource = deeDataSourceManager.findById(id);
        List<SourceType> sourceTypes =  SourceUtil.getDataSourceType();
        String dsType = JDBC;
        String metaFlag = Boolean.FALSE.toString();
        DeeResource deeResourceSubBean = new ConvertDeeResourceBean(deeResource).getResource();

        if (deeResourceSubBean instanceof JDBCResourceBean) {
            dsType = JDBC;
        } else if (deeResourceSubBean instanceof JNDIResourceBean) {
            dsType = JNDI;
        } else if (deeResourceSubBean instanceof A8MetaDatasourceBean) {
            metaFlag = Boolean.TRUE.toString();
            if ("".equals(((A8MetaDatasourceBean) deeResourceSubBean).getJndi())) {
                dsType = JDBC;
            } else {
                dsType = JNDI;
            }
        }
        // 获取的对象放入request
        view.addObject("sourceTypes", sourceTypes);
        view.addObject("deeResourceSubBean", deeResourceSubBean);
        view.addObject("deeResource", deeResource);
        view.addObject("dsType", dsType);
        view.addObject("metaFlag", metaFlag);
        view.addObject("successFlag", "");

        return view;
    }
}
