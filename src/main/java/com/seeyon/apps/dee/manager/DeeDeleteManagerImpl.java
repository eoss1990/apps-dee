package com.seeyon.apps.dee.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.dee.model.DeeFlowBind;
import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormTriggerActionBean;
import com.seeyon.ctp.form.bean.FormTriggerBean;
import com.seeyon.ctp.form.bean.Operation_BindEvent;
import com.seeyon.ctp.form.dee.bean.InfoPath_DeeTask;
import com.seeyon.ctp.form.formreport.bo.FormReportBean;
import com.seeyon.ctp.form.service.FormCacheManager;
import com.seeyon.ctp.report.design.po.ReportCfg;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.bean.A8EnumReaderBean;
import com.seeyon.v3x.dee.bean.A8EnumWriterBean;
import com.seeyon.v3x.dee.bean.ConvertDeeResourceBean;
import com.seeyon.v3x.dee.bean.JDBCReaderBean;
import com.seeyon.v3x.dee.bean.JDBCWriterBean;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;
import com.seeyon.v3x.dee.common.db.resource.model.DeeResourceBean;
import com.seeyon.v3x.dee.common.db.resource.util.DeeResourceEnum;
import com.seeyon.v3x.dee.common.db2cfg.GenerationCfgUtil;
import com.seeyon.v3x.dee.schedule.QuartzManager;

/**
 * 任务删除管理器
 *
 * @author zhangfb
 */
public class DeeDeleteManagerImpl implements DeeDeleteManager {
    /**
     * 日志
     */
    private static final Log log = LogFactory.getLog(DeeDeleteManagerImpl.class);

    /**
     * DEE实例化
     */
    private static final DEEConfigService configService = DEEConfigService.getInstance();

    private static final String baseName = "com.seeyon.apps.dee.resources.i18n.DeeResources";

    private FormCacheManager formCacheManager;
    private DeeSectionManager deeSectionManager;
    

    public void setFormCacheManager(FormCacheManager formCacheManager) {
		this.formCacheManager = formCacheManager;
	}

	public void setDeeSectionManager(DeeSectionManager deeSectionManager) {
		this.deeSectionManager = deeSectionManager;
	}

	/**
     * 删除任务
     *
     * @param flowIds 任务ID列表，如：“id1,id2,id3”
     * @return 提示信息
     * @throws Exception
     */
    @Override
    public Map<String, String> deleteFlow(String flowIds) throws Exception {
        List<String> flowIdList = new ArrayList<String>();
        Map<String, DeeFlowBind> resultMap = new HashMap<String, DeeFlowBind>();
        String[] flowIdArray = flowIds.trim().split(",");

        for (String flowId : flowIdArray) {
            if (flowId != null) {
                flowId = flowId.trim();
                if (!"".equals(flowId)) {
                    flowIdList.add(flowId);
                    DeeFlowBind deeFlowBind = new DeeFlowBind();
                    deeFlowBind.setFlowBean(configService.getFlow(flowId));
                    resultMap.put(flowId, deeFlowBind);
                }
            }
        }

        List<FormBean> formBeans = formCacheManager.getFormList();
        for (FormBean formBean : formBeans) {
            // 表单控件
            checkFormComponent(formBean, flowIdList, resultMap);
            //表单统计
            checkFormStatistics(formBean, flowIdList, resultMap);
            // 触发设置
            checkFormTrigger(formBean, flowIdList, resultMap);
            // 开发高级
            checkOperationBind(formBean, flowIdList, resultMap);
        }

        // Portal栏目是否绑定
        checkDeePortal(flowIdList, resultMap);

        // 声明未使用的数据源
        Map<String, Boolean> dataSourceMap = new HashMap<String, Boolean>();

        Boolean isRefreshSchedule = false;
        List<String> flowList=new ArrayList<String>();
        for (Map.Entry<String, DeeFlowBind> entry : resultMap.entrySet()){
            flowList.add(entry.getValue().getFlowBean().getFLOW_ID());
        }
        for (Map.Entry<String, DeeFlowBind> entry : resultMap.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue().getUse())) {
                try {
                    Map<String, Boolean> singleDataSourceMap = getRefDataSourceMap(entry.getKey());
                    if(configService.getFlowWithSchedule(flowList))
                    {
                        isRefreshSchedule=true;
                    }
                    configService.delFlow(entry.getKey());
                    dataSourceMap.putAll(singleDataSourceMap);
                    entry.getValue().setDeleteFlag(Boolean.TRUE);
                } catch (TransformException e) {
                    log.error(e.getMessage(), e);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }

        // 删除未使用的数据源
        deleteRefDataSource(dataSourceMap);
        Map<String, String> out = null;

        try {
            //重新载入dee.xml
            GenerationCfgUtil.getInstance().generationMainFile(GenerationCfgUtil.getDEEHome());
            DEEClient client = new DEEClient();
            client.refreshContext();
            if (isRefreshSchedule) {
                QuartzManager.getInstance().refresh();
            }
            out = getTipInfo(resultMap);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            out = new HashMap<String, String>();
            out.put("ret_code", "2002");
            out.put("info", "刷新DEE上下文失败");
        }

        return out;
    }

    /**
     * 根据flowId，获取关联的数据源id列表
     *
     * @param flowId flowId
     * @return Map&lt;String, Boolean&gt;  key-->数据源ID，value-->数据源是否可以删除
     */
    private Map<String, Boolean> getRefDataSourceMap(String flowId) {
        Map<String, Boolean> dataSourceMap = new HashMap<String, Boolean>();

        List<DeeResourceBean> list = configService.getResListByFlowId(flowId);
        if (list != null) {
            for (DeeResourceBean bean : list) {
                if (Integer.toString(DeeResourceEnum.JDBCREADER.ordinal()).equals(bean.getResource_template_id())) {
                    JDBCReaderBean jdbcReaderBean = (JDBCReaderBean) new ConvertDeeResourceBean(bean).getResource();
                    dataSourceMap.put(jdbcReaderBean.getDataSource(), Boolean.TRUE);
                } else if (Integer.toString(DeeResourceEnum.JDBCWRITER.ordinal()).equals(bean.getResource_template_id())) {
                    JDBCWriterBean jdbcWriterBean = (JDBCWriterBean) new ConvertDeeResourceBean(bean).getResource();
                    dataSourceMap.put(jdbcWriterBean.getDataSource(), Boolean.TRUE);
                }else if (Integer.toString(DeeResourceEnum.A8EnumReader.ordinal()).equals(bean.getResource_template_id())) {
                    A8EnumReaderBean a8EnumReaderBean = (A8EnumReaderBean) new ConvertDeeResourceBean(bean).getResource();
                    dataSourceMap.put(a8EnumReaderBean.getDataSource(), Boolean.TRUE);
                }else if (Integer.toString(DeeResourceEnum.A8EnumWriter.ordinal()).equals(bean.getResource_template_id())) {
                    A8EnumWriterBean a8EnumWriterBean = (A8EnumWriterBean) new ConvertDeeResourceBean(bean).getResource();
                    dataSourceMap.put(a8EnumWriterBean.getDataSource(), Boolean.TRUE);
                }
            }
        }

        return dataSourceMap;
    }

    /**
     * 删除关联数据源
     *
     * @param dataSourceIds 数据源ID
     */
    private void deleteRefDataSource(Map<String, Boolean> dataSourceIds) {
        // 获取所有的资源
        List<DeeResourceBean> deeResourceBeans = configService.getAllResList();

        for (DeeResourceBean deeResourceBean : deeResourceBeans) {
            if (Integer.toString(DeeResourceEnum.JDBCREADER.ordinal()).equals(deeResourceBean.getResource_template_id())) {
                // 资源为JDBC来源
                JDBCReaderBean jdbcReaderBean = (JDBCReaderBean) new ConvertDeeResourceBean(deeResourceBean).getResource();
                if (dataSourceIds.get(jdbcReaderBean.getDataSource()) != null) {
                    dataSourceIds.put(jdbcReaderBean.getDataSource(), Boolean.FALSE);
                }
            } else if (Integer.toString(DeeResourceEnum.JDBCWRITER.ordinal()).equals(deeResourceBean.getResource_template_id())) {
                // 资源为JDBC目标
                JDBCWriterBean jdbcWriterBean = (JDBCWriterBean) new ConvertDeeResourceBean(deeResourceBean).getResource();
                if (dataSourceIds.get(jdbcWriterBean.getDataSource()) != null) {
                    dataSourceIds.put(jdbcWriterBean.getDataSource(), Boolean.FALSE);
                }
            } else if (Integer.toString(DeeResourceEnum.A8EnumReader.ordinal()).equals(deeResourceBean.getResource_template_id())) {
                // 资源为枚举导出
                A8EnumReaderBean a8EnumReader = (A8EnumReaderBean) new ConvertDeeResourceBean(deeResourceBean).getResource();
                if (dataSourceIds.get(a8EnumReader.getDataSource()) != null) {
                    dataSourceIds.put(a8EnumReader.getDataSource(), Boolean.FALSE);
                }
            } else if (Integer.toString(DeeResourceEnum.A8EnumWriter.ordinal()).equals(deeResourceBean.getResource_template_id())) {
                // 资源为枚举导入
                A8EnumWriterBean a8EnumWriterBean = (A8EnumWriterBean) new ConvertDeeResourceBean(deeResourceBean).getResource();
                if (dataSourceIds.get(a8EnumWriterBean.getDataSource()) != null) {
                    dataSourceIds.put(a8EnumWriterBean.getDataSource(), Boolean.FALSE);
                }
            }
        }

        List<String> ids = new ArrayList<String>();
        for (Map.Entry<String, Boolean> entry : dataSourceIds.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                ids.add(entry.getKey());
            }
        }
        try {
            // 执行删除操作
            configService.delResources(ids.toArray(new String[0]));
        } catch (TransformException e) {
            log.info(e.getMessage(), e);
        }
    }
    /**
     * 校验表单统计是否绑定了DEE任务
     *
     * @param formBean 表单Bean
     * @param flowIdList flowId列表
     * @param resultMap 结果映射
     */
    private void checkFormStatistics(FormBean formBean,List<String> flowIdList,Map<String,DeeFlowBind> resultMap){
        List<FormReportBean> formReportBeans = formBean.getFormReportList();
        for(FormReportBean formReportBean : formReportBeans){
            ReportCfg reportCfg = formReportBean.getReportCfg();
            if(reportCfg != null){
                for(String flowId : flowIdList){
                    if (Boolean.FALSE.equals(resultMap.get(flowId).getUse())) {
                        if (flowId.equals(reportCfg.getDeeId())) {
                            resultMap.get(flowId).getFormBeans().add(formBean);
                            resultMap.get(flowId).setUse(Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }
    /**
     * 校验表单控件是否绑定了DEE任务
     *
     * @param formBean 表单Bean
     * @param flowIdList flowId列表
     * @param resultMap 结果映射
     */
    private void checkFormComponent(FormBean formBean, List<String> flowIdList, Map<String, DeeFlowBind> resultMap) {
        List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
        for (FormFieldBean formFieldBean : formFieldBeans) {
            InfoPath_DeeTask deeTask = formFieldBean.getDeeTask();
            if (deeTask != null) {
                for (String flowId : flowIdList) {
                    if (Boolean.FALSE.equals(resultMap.get(flowId).getUse())) {
                        if (deeTask.getId().equals(flowId)) {
                            resultMap.get(flowId).getFormBeans().add(formBean);
                            resultMap.get(flowId).setUse(Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验触发设置是否绑定了DEE任务
     *
     * @param formBean 表单Bean
     * @param flowIdList flowId列表
     * @param resultMap 结果映射
     */
    private void checkFormTrigger(FormBean formBean, List<String> flowIdList, Map<String, DeeFlowBind> resultMap) {
        for (Map.Entry<Long, FormTriggerBean> entry : formBean.getTriggerConfigMap().entrySet()) {
            FormTriggerBean formTriggerBean = entry.getValue();
            if (formTriggerBean != null) {
                List<FormTriggerActionBean> actions = formTriggerBean.getActions();
                if (actions != null) {
                    for (FormTriggerActionBean action : actions) {
                        Object taskId = action.getParam("taskId");
                        for (String flowId : flowIdList) {
                            if (Boolean.FALSE.equals(resultMap.get(flowId).getUse())) {
                                if (flowId.equals(taskId)) {
                                    resultMap.get(flowId).getFormBeans().add(formBean);
                                    resultMap.get(flowId).setUse(Boolean.TRUE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验开发高级是否绑定了DEE任务
     *
     * @param formBean 表单Bean
     * @param flowIdList flowId列表
     * @param resultMap 结果映射
     */
    private void checkOperationBind(FormBean formBean, List<String> flowIdList, Map<String, DeeFlowBind> resultMap) {
        // 开发高级是否绑定了DEE任务
        List<FormAuthViewBean> formAuthViewBeanList = formBean.getAllFormAuthViewBeans();
        for (FormAuthViewBean authViewBean : formAuthViewBeanList) {
            List<Operation_BindEvent> bindEventList = authViewBean.getOperationBindEvent();
			for (Operation_BindEvent event : bindEventList) {
                if (event != null) {
                    for (String flowId : flowIdList) {
                        if (Boolean.FALSE.equals(resultMap.get(flowId).getUse())) {
                            if (event.getTaskId().equals(flowId)) {
                                resultMap.get(flowId).getFormBeans().add(formBean);
                                resultMap.get(flowId).setUse(Boolean.TRUE);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验Portal栏目是否绑定
     *
     * @param flowIdList flowId列表
     * @param resultMap 结果映射
     */
    private void checkDeePortal(List<String> flowIdList, Map<String, DeeFlowBind> resultMap) {
    
        List<DeeSectionDefine> deeSections = deeSectionManager.findAllDeeSection();
        for (DeeSectionDefine deeSection : deeSections) {
            for (String flowId : flowIdList) {
                if (Boolean.FALSE.equals(resultMap.get(flowId).getUse())) {
                    if ((deeSection.getFlowId() + "").equals(flowId)) {
                        resultMap.get(flowId).getDeeSectionDefines().add(deeSection);
                        resultMap.get(flowId).setUse(Boolean.TRUE);
                    }
                }
            }
        }
    }

    /**
     * 提示信息
     *
     * @param resultMap 结果集
     * @return 提示信息字符串
     */
    private Map<String, String> getTipInfo(Map<String, DeeFlowBind> resultMap) {
        final String successTip = ResourceBundleUtil.getString(this.baseName, "dee.deltask.deltip.success") + " ";
        final String successTipSuffix = ResourceBundleUtil.getString(this.baseName, "dee.deltask.deltip.success.suffix");
        final String failureTip = ResourceBundleUtil.getString(this.baseName, "dee.deltask.deltip.failure") + " ";
        final String failureTipSuffix = ResourceBundleUtil.getString(this.baseName, "dee.deltask.deltip.failure.suffix");
        final String inUseTip = ResourceBundleUtil.getString(this.baseName, "dee.deltask.deltip.detail.failure.inuse.suffix");
        final String noExistTip = ResourceBundleUtil.getString(this.baseName, "dee.deltask.deltip.detail.failure.noexist.suffix");

        Map<String, String> returnMap = new HashMap<String, String>();
        // 成功条数
        int successCount = 0;
        // 失败条数
        int failureCount = 0;
        // 成功信息
        StringBuffer successBuffer = new StringBuffer("");
        // 失败信息
        StringBuffer failureBuffer = new StringBuffer("");
        StringBuffer buffer = new StringBuffer("");

        for (Map.Entry<String, DeeFlowBind> entry : resultMap.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue().getDeleteFlag())) {
                successBuffer.append(successCount + 1).append("：")
                        .append(entry.getValue().getFlowBean().getDIS_NAME()).append("<br/>");
                successCount++;
            }
        }

        successBuffer.insert(0, successTip + successCount + successTipSuffix);

        for (Map.Entry<String, DeeFlowBind> entry : resultMap.entrySet()) {
            if (entry.getValue().getFlowBean() == null) {
                failureBuffer.append(entry.getKey()).append(noExistTip);
                failureCount++;
                continue;
            }
            if (Boolean.FALSE.equals(entry.getValue().getDeleteFlag())) {
                failureBuffer.append(failureCount + 1).append("：")
                        .append(entry.getValue().getFlowBean().getDIS_NAME()).append("，");
                if (entry.getValue().getFormBeans().size() > 0) {
                    for (FormBean formBean : entry.getValue().getFormBeans()) {
                        failureBuffer.append(formBean.getFormName()).append(inUseTip);
                        failureCount++;
                    }
                } else if (entry.getValue().getDeeSectionDefines().size() > 0) {
                    for (DeeSectionDefine deeSectionDefine : entry.getValue().getDeeSectionDefines()) {
                        failureBuffer.append(deeSectionDefine.getDeeSectionName()).append(inUseTip);
                        failureCount++;
                    }
                }
            }
        }
        failureBuffer.insert(0, failureTip + failureCount + failureTipSuffix);
        if (successCount > 0) {
            buffer.append(successBuffer);
        }
        if (failureCount > 0) {
            buffer.append(failureBuffer);
        }

        returnMap.put("info", buffer.toString());
        if (successCount > 0 && failureCount == 0) {
            returnMap.put("ret_code", "2000");
        }
        if (successCount > 0 && failureCount > 0) {
            returnMap.put("ret_code", "2001");
        }
        if (successCount == 0 && failureCount > 0) {
            returnMap.put("ret_code", "2002");
        }

        return returnMap;
    }

    public String getFlowName(String id)
    {
        FlowBean flow = configService.getFlow(id);
        String disName = flow.getDIS_NAME();
        return disName;
    }
}

