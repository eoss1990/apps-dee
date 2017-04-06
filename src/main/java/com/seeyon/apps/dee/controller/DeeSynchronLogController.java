package com.seeyon.apps.dee.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.dee.manager.DeeSynchronLogManager;
import com.seeyon.apps.dee.util.Request2BeanUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.redo.model.RedoBean;
import com.seeyon.v3x.dee.common.db.redo.model.SyncBean;

/**
 * DEE日志同步控制器
 *
 * @author 作者： zhanggong
 * @version 创建时间：2012-5-22 上午02:50:48
 */
public class DeeSynchronLogController extends BaseController {

	private static final String baseName = "com.seeyon.apps.dee.resources.i18n.DeeResources";

	/**
	 * 日志
	 */
	private static final Log log = LogFactory.getLog(DeeSynchronLogController.class);
	
	/**
	 * DEE实例化
	 */
	private static final DEEConfigService configService = DEEConfigService.getInstance();
	
	private ArrayList[] syncidArray;
	
	private DeeSynchronLogManager deeSynchronLogManager;
	

	public DeeSynchronLogManager getDeeSynchronLogManager() {
		return deeSynchronLogManager;
	}

	public void setDeeSynchronLogManager(DeeSynchronLogManager deeSynchronLogManager) {
		this.deeSynchronLogManager = deeSynchronLogManager;
	}
	public ArrayList[] getSyncidArray() {
        return syncidArray;
    }
	
	public void setSyncidArray(ArrayList[] syncidArray) {
        this.syncidArray = syncidArray;
    }

    /**
     * 跳转日志显示Frame
     *
     * @param request request
     * @param response response
     * @return ModelAndView
     * @throws Exception
     */
    public ModelAndView synchronLogFrame(HttpServletRequest request, HttpServletResponse response) throws Exception{
		// 跳转到上下布局页面
		return new ModelAndView("plugin/dee/exceptionLog/synchronLogFrame");
	}

    /**
     * Dee控制台-同步历史new
     *
     * @param request request
     * @param response response
     * @return ModelAndView
     * @throws Exception
     */
    public ModelAndView deeSynchronLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView view;
        FlipInfo fi=new FlipInfo();
        view = new ModelAndView("plugin/dee/exceptionLog/synchronLogFrame");
        deeSynchronLogManager.findSynchronLog(fi, new HashMap<String, Object>());
        request.setAttribute("ffscheduleTable", fi);
        return view;
    }

    /**
     * Dee控制台-查看同步异常详情new
     *
     * @param request request
     * @param response response
     * @return ModelAndView
     * @throws Exception
     */
	
    public ModelAndView showDeeExceptionDetail(HttpServletRequest request, HttpServletResponse response) throws Exception{
	    String syncId = request.getParameter("syncId");
	    ModelAndView view = new ModelAndView("plugin/dee/exceptionLog/synchronLogDetail");
		view.addObject("syncId", syncId);
		return view;
	}

    /**
     * Dee控制台-处理异常数据(重新同步&&忽略异常)new
     *
     * @param request request
     * @param response response
     * @return ModelAndView
     * @throws Exception 
     */
	public ModelAndView deeRedoOrIgnore(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String redoIds[] = request.getParameterValues("redosId");
        String syncId = request.getParameter("syncId");
       
       // String flowName = request.getParameter("flowName");
        
        String retMsg = "";
                // 重新发起失败任务(多态)
                Map<String, String> hm = configService.redo(redoIds);
                retMsg = ResourceBundleUtil.getString(this.baseName, "dee.synchronLog.redoSuccess.label");
                retMsg +="," + "success";
                if (hm != null) {
                    for (String redoId : redoIds) {
                        if (StringUtils.isNotBlank(hm.get(redoId))) {
                            retMsg = ResourceBundleUtil.getString(this.baseName, "dee.synchronLog.redoFailed.label");
                            retMsg +="," + "failed";
                            break;
                        }
                    }
                }
        request.setAttribute("retMsg", retMsg);
        request.setAttribute("syncId", syncId);
        return showDeeExceptionDetail(request, response);
    }

	/**
	 * 删除sync记录
     *
	 * @param request request
	 * @param response response
	 * @return ModelAndView
	 * @throws Exception
	 */
	public ModelAndView synchronLogDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    String syncId = request.getParameter("syncId");
		String retMsg = ResourceBundleUtil.getString(this.baseName,"dee.synchronLog.delSuccess.label");
		try {
			deeSynchronLogManager.delSyncBySyncId(Strings.join(",", syncId));
		} catch (TransformException e) {
			log.error("删除数据源出错：" + e.getLocalizedMessage(), e);
			retMsg = ResourceBundleUtil.getString(this.baseName,"dee.synchronLog.delFailed.label") + e.getLocalizedMessage();
		}
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.print("<script>alert('"+ retMsg +"');</script>");
		out.flush();

		//return super.redirectModelAndView("/deeSynchronLogController.do?method=deeSynchronLog");
		return new ModelAndView("plugin/dee/exceptionLog/synchronLogFrame");
		
	}
	/**
	 * 删除redo记录
     *
	 * @param request request
	 * @param response response
	 * @return ModelAndView
	 * @throws Exception
	 */
	public ModelAndView exceptionDetailDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
		/*String syncId = request.getParameter("syncId");
		String flowName = request.getParameter("flowName");
		String redoId = request.getParameter("id");// 获取数据是以逗号隔开的字符串
	
		String retMsg1 = ResourceBundleUtil.getString(this.baseName,"dee.synchronLog.delSuccess.label");
		try {
			deeSynchronLogManager.delSyncByRedoId(syncId,Strings.join(",", redoId));
		} catch (TransformException e) {
			log.error("删除出错：" + e.getLocalizedMessage(), e);
			retMsg1 = ResourceBundleUtil.getString(this.baseName,"dee.synchronLog.delFailed.label") + e.getLocalizedMessage();
		}

        response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		
		//out.print("<script> alert('"+ retMsg +"');</script>");
		out.print(retMsg1);
		out.flush();
   
        return super.redirectModelAndView("/deeSynchronLogController.do?method=showDeeExceptionDetail&syncId="+syncId+"&flowName="+flowName);*/
	}

	/**
	 * 获得redo的详细参数信息，跳转到弹出窗口
     *
	 * @param request request
	 * @param response response
	 * @return ModelAndView
	 */
	public ModelAndView exceptionDetaiOpeanWin(HttpServletRequest request, HttpServletResponse response){
	    
        ModelAndView view = new ModelAndView("plugin/dee/exceptionLog/openRedo");
        
        RedoBean bean = new RedoBean();
        Request2BeanUtil.parseRequest(request, bean);
        bean = deeSynchronLogManager.findRedoById(bean.getRedo_id());
        view.addObject("bean", bean);
        SyncBean syBean = deeSynchronLogManager.findSynchronLog(bean.getSync_id());
        if (syBean != null) {
            view.addObject("flowRealName", syBean.getFlow_dis_name());
        }
        return view;
    }

	/**
	 * 修改重发任务里的参数
     *
	 * @param request request
	 * @param response response
	 * @return ModelAndView
	 */
	public ModelAndView exceptionDetaiOpeanUpdate(HttpServletRequest request, HttpServletResponse response){
        ModelAndView view = new ModelAndView("plugin/dee/exceptionLog/openRedo");
        try {
            RedoBean bean = deeSynchronLogManager.findRedoById(request.getParameter("redo_id"));
            bean.setDoc_code(request.getParameter("doc_code"));
            deeSynchronLogManager.updateRedoBean(bean);
            view.addObject("bean", deeSynchronLogManager.findRedoById(bean.getRedo_id()));
            SyncBean syBean = deeSynchronLogManager.findSynchronLog(bean.getSync_id());
            if (syBean != null)
                view.addObject("flowRealName", syBean.getFlow_dis_name());
            view.addObject("retMsg", ResourceBundleUtil.getString(this.baseName, "dee.synchronLog.saveSuccess.label"));
        } catch (Exception e) {
            view.addObject("retMsg", ResourceBundleUtil.getString(this.baseName, "dee.synchronLog.saveFailed.label"));
            log.error("请检查数据是否存在" + e.getMessage(), e);
        }
        return view;
    }

    /**
     * 检测异常任务可否重新执行
     *
     * @param syncIds 同步IDs
     * @return true or false
     */
    public boolean checkSynchonException(String[] syncIds) {
        boolean result = true;
        for (String syncId : syncIds) {
            String[] redoStates = {RedoBean.STATE_FLAG_FAILE};
            //获取同批次重发列表中失败的记录
            List<RedoBean> redoBeansFaile = deeSynchronLogManager.findRedoList(syncId, redoStates, null, null, null);
            if (CollectionUtils.isNotEmpty(redoBeansFaile)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 分页工具方法
     *
     * @param list
     * @param <T>
     * @return
     */
	private <T> List<T> pagenate(List<T> list) {
		if (null == list || list.size() == 0) {
			return new ArrayList<T>();
        }
		Integer first = Pagination.getFirstResult();
		Integer pageSize = Pagination.getMaxResults();
		Pagination.setRowCount(list.size());
		List<T> subList = null;
		if (first + pageSize > list.size()) {
			subList = list.subList(first, list.size());
		} else {
			subList = list.subList(first, first + pageSize);
		}
		return subList;
	}

}
