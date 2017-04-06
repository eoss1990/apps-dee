package com.seeyon.apps.dee.controller;

import com.seeyon.apps.dee.manager.DeeTriggerDesignManager;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.code.model.FlowTypeBean;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DEE触发设置页面的控制器
 * @author dengxj
 *
 */
public class DEETriggerDesignController extends BaseController {

    DeeTriggerDesignManager deeTriggerDesignManager;

    public DeeTriggerDesignManager getDeeTriggerDesignManager() {
        return deeTriggerDesignManager;
    }

    public void setDeeTriggerDesignManager(DeeTriggerDesignManager deeTriggerDesignManager) {
        this.deeTriggerDesignManager = deeTriggerDesignManager;
    }

    public ModelAndView triggerDEETask(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/form/dee/trigger/deeTaskTree");
        request.setAttribute("ffdeeTree", deeTriggerDesignManager.getFlowListTree(null));
        request.setAttribute("ffdeeTable",
                deeTriggerDesignManager.getFlowList(new FlipInfo(), new HashMap<String, Object>()));
        return mav;
    }

    @Deprecated
	public ModelAndView triggerDEETaskList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("ctp/form/dee/trigger/taskListFrame");
		return mav;
	}

    @Deprecated
	public ModelAndView taskTree(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("ctp/form/dee/trigger/taskTree");
		List<FlowTypeBean> flowTypeList = DEEConfigService.getInstance().getFlowTypeList();
		if(flowTypeList==null){
			flowTypeList = new ArrayList<FlowTypeBean>();
		}
		mav.addObject("typeList", flowTypeList);
		return mav;
	}

    @Deprecated
	public ModelAndView taskList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("ctp/form/dee/trigger/taskList");
		String flowType = request.getParameter("type_id");
		String flowName = request.getParameter("flowName");
		String taskType = request.getParameter("taskType");
		if(flowName!=null){
			flowName = com.seeyon.ctp.common.taglibs.functions.Functions.urlDecoder(flowName);
		}
		String model = DEEConfigService.MODULENAME_FORM;
		if(Strings.isNotBlank(taskType)){
			model = DEEConfigService.MODULENAME_DATA;
		}
		List<FlowBean> flowList = new ArrayList<FlowBean>();//
		int pageNumber = Strings.isBlank(request.getParameter("page")) ? 1 : Integer.parseInt(request.getParameter("page"));
		Map<String,Object> listObj = DEEConfigService.getInstance().getFlowList("-1".equals(flowType)?null:flowType, model, flowName, pageNumber, com.seeyon.ctp.common.dao.paginate.Pagination.getMaxResults());
		if(listObj!=null){
			flowList = (List<FlowBean>)listObj.get(DEEConfigService.MAP_KEY_RESULT);
			int rowCount = Integer.parseInt(listObj.get(DEEConfigService.MAP_KEY_TOTALCOUNT).toString());
			com.seeyon.ctp.common.dao.paginate.Pagination.setRowCount(rowCount);
		}
		mav.addObject("flowList", flowList);
		return mav;
	}
	
}
