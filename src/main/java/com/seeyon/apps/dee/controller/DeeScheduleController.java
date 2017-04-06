package com.seeyon.apps.dee.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.dee.manager.DeeScheduleManager;
import com.seeyon.apps.dee.util.Request2BeanUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.schedule.model.ScheduleBean;
import com.seeyon.v3x.dee.common.db2cfg.GenerationCfgUtil;
import com.seeyon.v3x.dee.schedule.QuartzManager;

public class DeeScheduleController extends BaseController {
    /**
     * 日志
     */
    private static final Log log = LogFactory.getLog(DeeScheduleController.class);

    /**
     * 国际化
     */
    private static final String baseName = "com.seeyon.apps.dee.resources.i18n.DeeResources";

    /**
     * DEE实例化
     */
    private static final DEEConfigService configService = DEEConfigService.getInstance();

    /**
     * 定时器管理
     */
    private DeeScheduleManager deeScheduleManager;

    /**
     * 功能：跳转到定时器首页
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView scheduleFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView view = new ModelAndView("plugin/dee/schedule/scheduleFrame");
        FlipInfo fi = deeScheduleManager.findScheduleList(new FlipInfo(), new HashMap());
        request.setAttribute("ffscheduleTable", fi);
        return view;
    }

    /**
     * 功能：获取数据跳转定时器详细信息页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showScheduleDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView view = new ModelAndView("plugin/dee/schedule/scheduleDetail");
        // 获取flowId
        String flowId = request.getParameter("id");
        ScheduleBean bean = configService.getScheduleByFlowId(flowId);

        view.addObject("bean", bean);
        view.addObject("retFixed", getISFixed(bean.getQuartz_code()));
        if (bean.getFlow_id() != null) {
            view.addObject("flow", configService.getFlow(bean.getFlow_id()));
        }
        return view;
    }

    /**
     * 功能：获取数据跳转定时器修改页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showScheduleUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView view = new ModelAndView("plugin/dee/schedule/scheduleUpdate");
        // 获取flowId
        String flowId = request.getParameter("id");
        ScheduleBean bean = configService.getScheduleByFlowId(flowId);

        view.addObject("bean", bean);
        view.addObject("retFixed", getISFixed(bean.getQuartz_code()));
        if (bean.getFlow_id() != null) {
            view.addObject("flow", configService.getFlow(bean.getFlow_id()));
        }
        return view;
    }

    /**
     * 功能：定时器信息更新
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */

    public ModelAndView scheduleUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView view = new ModelAndView("plugin/dee/schedule/scheduleDetail");
        String retMsg = ResourceBundleUtil.getString(this.baseName, "dee.schedule.updateSucceed.label");
        ScheduleBean bean = new ScheduleBean();
        Request2BeanUtil.parseRequest(request, bean);
        try {
            bean.setEnable("1".equals(request.getParameter("isEnable")));
            // add by dkywolf 2012-03-15 增加描述信息的保存
            bean.setSchedule_desc(request.getParameter("resource_desc") == null ? "" : request.getParameter("resource_desc"));
            if (!configService.updateSchedule(bean)) {
                retMsg = ResourceBundleUtil.getString(this.baseName, "dee.schedule.updateError.label");
                bean = configService.getScheduleByFlowId(bean.getSchedule_id());
            }
            view.addObject("bean", bean);
            view.addObject("retFixed", getISFixed(bean.getQuartz_code()));
            if (bean.getFlow_id() != null) {
                view.addObject("flow", configService.getFlow(bean.getFlow_id()));
            }
            //更新Dee配置文件，重新加载
            GenerationCfgUtil.getInstance().generationMainFile(GenerationCfgUtil.getDEEHome());
            QuartzHolder.deleteQuartzJobByGroup(QuartzManager.JOB_GROUP_NAME);
            QuartzManager.getInstance().refresh();
        } catch (TransformException e) {
            log.error(e.getMessage(), e);
            retMsg = ResourceBundleUtil.getString(this.baseName, "dee.schedule.updateError.label") + e.getLocalizedMessage();
        } catch (Throwable e) {
            log.error("引擎刷新上下文异常" + e.getLocalizedMessage());
            retMsg = ResourceBundleUtil.getString(this.baseName, "dee.schedule.updateError.label") + e.getLocalizedMessage();
        }
        view.addObject("retMsg", retMsg);
        return view;
    }

    private int getISFixed(String dateCode) {
        if (dateCode != null && "1".equalsIgnoreCase(dateCode.substring(0, 1))) {
            return 1;
        } else {
            return 0;
        }
    }

    public DeeScheduleManager getDeeScheduleManager() {
        return deeScheduleManager;
    }

    public void setDeeScheduleManager(DeeScheduleManager deeScheduleManager) {
        this.deeScheduleManager = deeScheduleManager;
    }
}

