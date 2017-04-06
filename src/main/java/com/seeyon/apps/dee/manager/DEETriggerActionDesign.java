/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.dee.manager;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormTriggerActionBean;
import com.seeyon.ctp.form.bean.FormTriggerBean;
import com.seeyon.ctp.form.bean.FormTriggerBean.ParamType;
import com.seeyon.ctp.form.bean.FormValidateResultBean;
import com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.FormTriggerUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import org.apache.commons.logging.Log;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author NICED
 *
 */
public class DEETriggerActionDesign extends FormTriggerActionDesignManager {
    
    private static final Log LOGGER = CtpLogFactory.getLog(DEETriggerActionDesign.class);
    private DEEConfigService deeconfig = DEEConfigService.getInstance();
    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getId()
     */
    @Override
    public String getId() {
        return "taskDEE";
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getSort()
     */
    @Override
    public Integer getSort() {
        LOGGER.info("排序触发动作===" + this.getName());
        return 4;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#geti18nName()
     */
    @Override
    public String geti18nName() {
        return "form.trigger.triggerSet.exchangeTask.label";
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#needHighFormPlugin()
     */
    @Override
    public boolean needHighFormPlugin() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#canUse4FormType(com.seeyon.ctp.form.util.Enums.FormType)
     */
    @Override
    public boolean canUse4FormType(FormType type) {
        return true;
    }
    
    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#canUse()
     */
    @Override
    public boolean canUse() {
        if (super.canUse()){
            if (AppContext.hasPlugin("dee")){
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getActionTypeManagerName()
     */
    @Override
    public String getActionTypeManagerName() {
        return "deeTriggerActionManager";
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getActionFromXML(org.dom4j.Element, com.seeyon.ctp.form.bean.FormTriggerBean)
     */
    @Override
    public FormTriggerActionBean getActionFromXML(Element aelement, FormTriggerBean triggerBean)
            throws BusinessException {
        LOGGER.info("初始化触发动作===" + this.getName());
        FormTriggerActionBean actionBean = FormTriggerUtil.getActionFromXML(aelement, triggerBean);
        if (actionBean.getParam("taskName") == null ||
                "".equals(actionBean.getParam("taskName").toString().trim())) {
            Map<String, Object> param = actionBean.getParam();
            try {
                String flowDisName = deeconfig.getFlow(String.valueOf(actionBean.getParam("taskId"))).getDIS_NAME();
                param.put("taskName", flowDisName);
            } catch (Exception ex) {
                LOGGER.error(ex.toString());
            }
        }
        actionBean.setActionManager(this);
        return actionBean;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getActionXMLFromActionBean(com.seeyon.ctp.form.bean.FormTriggerActionBean, int)
     */
    @Override
    public String getActionXMLFromActionBean(FormTriggerActionBean actionBean, int aSpace, boolean needFormula) throws BusinessException {
        LOGGER.info("存储触发动作===" + this.getName());
        return FormTriggerUtil.getActionXMLFromActionBean(actionBean, aSpace,needFormula);
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getActionFromMap(java.util.Map)
     */
    @Override
    public FormTriggerActionBean getActionFromMap(Map<String, String> map) throws BusinessException {
        FormTriggerActionBean actionBean = new FormTriggerActionBean();
        String actionId=map.get("actionId");
        actionBean.setId(Strings.isBlank(actionId)?UUIDLong.longUUID():Long.parseLong(actionId));
        actionBean.setType(this.getId());
        actionBean.setName(this.getName());
        actionBean.addParam(ParamType.TaskId.getKey(), map.get("taskId"));
        actionBean.addParam(ParamType.TaskName.getKey(), map.get("taskName"));
        
        actionBean.setActionManager(this);
        LOGGER.info("新建触发动作===" + this.getName());
        return actionBean;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#getParamMap(com.seeyon.ctp.form.bean.FormTriggerActionBean, com.seeyon.ctp.form.bean.FormBean)
     */
    @Override
    public Map<String, Object> getParamMap(FormTriggerActionBean bean, FormBean fb) throws BusinessException {
        Map<String, Object> actionMap = new HashMap<String,Object>();
        actionMap.put("actionId", bean.getId());
        actionMap.put("actionType", bean.getType());
        Iterator<Map.Entry<String, Object>> itors = bean.getParam().entrySet().iterator();
        while(itors.hasNext()){
            Map.Entry<String, Object> enty = itors.next();
            actionMap.put(enty.getKey(), enty.getValue());
        }
        return actionMap;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#clone(com.seeyon.ctp.form.bean.FormTriggerActionBean, com.seeyon.ctp.form.bean.FormTriggerActionBean)
     */
    @Override
    public FormTriggerActionBean clone(FormTriggerActionBean newActionBean, FormTriggerActionBean oldActionBean)
            throws CloneNotSupportedException {
        return this.clone(newActionBean,oldActionBean, oldActionBean.getFormTriggerBean());
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#clone(com.seeyon.ctp.form.bean.FormTriggerActionBean, com.seeyon.ctp.form.bean.FormTriggerActionBean, com.seeyon.ctp.form.bean.FormTriggerBean)
     */
    @Override
    public FormTriggerActionBean clone(FormTriggerActionBean newActionBean, FormTriggerActionBean oldActionBean,
                                       FormTriggerBean triggerBean) throws CloneNotSupportedException {
        FormTriggerActionBean formTriggerActionBean = newActionBean;
        
        formTriggerActionBean.setId(oldActionBean.getId());
        formTriggerActionBean.setFormTriggerBean(triggerBean);
        return formTriggerActionBean;
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.form.modules.trigger.FormTriggerActionDesignManager#validateFormTriggerActionField(java.lang.String, java.lang.String, com.seeyon.ctp.form.bean.FormTriggerActionBean, com.seeyon.ctp.form.bean.FormTriggerBean, com.seeyon.ctp.form.bean.FormBean)
     */
    @Override
    public String validateFormTriggerActionField(String fieldName, String newInputType,
                                                 FormTriggerActionBean actionBean, FormTriggerBean triggerBean, FormBean fb) throws BusinessException {
        return "1";
    }

    @Override
    public List<FormValidateResultBean> validateFormTriggerAction4BizRedirect(FormTriggerActionBean actionBean, FormTriggerBean triggerBean, FormBean fb) throws BusinessException {
        List<FormValidateResultBean> list = super.validateFormTriggerAction4BizRedirect(actionBean, triggerBean, fb);
        FormValidateResultBean validateResultBean = new FormValidateResultBean();
        validateResultBean.setFirstReference(triggerBean.getId().toString());
        validateResultBean.setSecondReference(actionBean.getId().toString());
        validateResultBean.setThirdReference("dee");
        validateResultBean.setName4Show("DEE任务");
        validateResultBean.setName4System("dee");
        validateResultBean.setType4Show(triggerBean.getName() + "--" + this.getName());
        validateResultBean.setType4System(this.getId());
        validateResultBean.setLocation4Show(ResourceUtil.getString("form.trigger.triggerSet.dee.label"));
        validateResultBean.setLocation4System("dee");
        validateResultBean.setAllowRedirect(false);
        validateResultBean.setJsFunction("redirectDee(this)");
        validateResultBean.setValue4Show((String) actionBean.getParam("taskName"));
        validateResultBean.setValue4System((String) actionBean.getParam("taskId"));
        validateResultBean.setNewValue((String) actionBean.getParam("taskId"));
        validateResultBean.setAllowClear(true);
        validateResultBean.setNeedRedirect(true);
        list.add(validateResultBean);
        return list;
    }

    @Override
    public void updateFormTriggerActionSet4BizRedirect(FormTriggerActionBean actionBean, FormTriggerBean triggerBean, FormBean fb, Map<String, Object> map) throws BusinessException {
        if (Strings.isBlank((String) map.get("newValue"))){
            triggerBean.getActions().remove(actionBean);
        } else {
            actionBean.addParam("taskId",(String) map.get("newValue"));
            actionBean.addParam("taskName",(String) map.get("value4Show"));
        }
    }

    @Override
    public boolean needSortExecute() {
        return false;
    }

    @Override
    public Long getTargetFormId(FormTriggerActionBean actionBean) {
        return 0L;
    }
}
