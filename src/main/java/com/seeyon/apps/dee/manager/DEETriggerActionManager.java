package com.seeyon.apps.dee.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.dee.util.FlowFormUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormTriggerActionBean;
import com.seeyon.ctp.form.bean.FormTriggerBean.ParamType;
import com.seeyon.ctp.form.modules.trigger.FormTriggerActionTypeManager;
import com.seeyon.ctp.form.modules.trigger.TriggerActionContext;
import com.seeyon.ctp.form.util.Enums;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;

/**
 * dee触发实现类
 * @author dengxj
 *
 */
public class DEETriggerActionManager extends FormTriggerActionTypeManager {
	private CollaborationApi collaborationApi;
	private static final Log LOGGER        = CtpLogFactory.getLog(DEETriggerActionManager.class);

    @Override
    public String getId() {
        return Enums.TriggerType.DEE.getKey();
    }

    @Override
	protected void execute(TriggerActionContext context) throws BusinessException {
        FormDataMasterBean masterBean = context.getMasterBean();
        FormTriggerActionBean actionBean = context.getActionBean();
        FormBean formBean = context.getFormBean();
        Set<Long> errorReceivers = context.getErrorReceivers();

		String deeTaskId = actionBean.getParam(ParamType.TaskId.getKey()).toString();
    	String deeTaskName = actionBean.getParam(ParamType.TaskName.getKey()).toString();
    	LOGGER.info("触发Dee:" + deeTaskId + "___________" + deeTaskName);
    	try {
			Map<String, String> formFlow_Data = FlowFormUtil.getFlowFormData(collaborationApi, 
					masterBean.getId(), context.getConditionState().getText());
			DEETriggerTaskManager event = new DEETriggerTaskManager(formBean,masterBean,deeTaskId,formFlow_Data);
			//去掉线程，任务按顺序执行
//			Thread thread = new Thread(event);
//			thread.start();
			event.runOrder();
		} catch (Exception e) {
			LOGGER.error(formBean.getFormName()+"触发DEE任务有误---"+"deeTaskName", e);
			messageManager.sendSystemMessage(MessageContent.get(formBean.getFormName()+"触发DEE任务有误!!!"), 
					ApplicationCategoryEnum.form, V3xOrgEntity.CONFIG_SYSTEM_AUTO_TRIGGER_ID, MessageReceiver.get(-1l, errorReceivers));
		} finally {
            try {
                super.getNewRecord(context, actionBean.getId(), actionBean.getId().toString());
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
    }
    
    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
}
