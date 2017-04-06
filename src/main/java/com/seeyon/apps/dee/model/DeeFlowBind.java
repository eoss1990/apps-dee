package com.seeyon.apps.dee.model;

import java.util.HashSet;
import java.util.Set;

import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;

/**
 * DEE任务绑定表单对象
 *
 * @author zhangfb
 */
public class DeeFlowBind {

    /**
     * 任务Bean
     */
    private FlowBean flowBean;

    /**
     * 是否使用，true：使用，false，未使用
     */
    private Boolean use;

    /**
     * 是否成功删除
     */
    private Boolean deleteFlag;

    /**
     * 使用任务的表单列表
     */
    private Set<FormBean> formBeans;

    /**
     * 使用任务的Portal栏目列表
     */
    private Set<DeeSectionDefine> deeSectionDefines;

    /**
     * 构造函数
     */
    public DeeFlowBind() {
        use = Boolean.FALSE;
        deleteFlag = Boolean.FALSE;
        formBeans = new HashSet<FormBean>();
        deeSectionDefines = new HashSet<DeeSectionDefine>();
    }

    public FlowBean getFlowBean() {
        return flowBean;
    }

    public void setFlowBean(FlowBean flowBean) {
        this.flowBean = flowBean;
    }

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public Set<FormBean> getFormBeans() {
        return formBeans;
    }

    public void setFormBeans(Set<FormBean> formBeans) {
        this.formBeans = formBeans;
    }

    public Set<DeeSectionDefine> getDeeSectionDefines() {
        return deeSectionDefines;
    }

    public void setDeeSectionDefines(Set<DeeSectionDefine> deeSectionDefines) {
        this.deeSectionDefines = deeSectionDefines;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}
