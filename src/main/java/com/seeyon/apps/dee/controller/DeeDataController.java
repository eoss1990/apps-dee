package com.seeyon.apps.dee.controller;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.dee.util.Constants;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.dee.bean.*;
import com.seeyon.ctp.form.dee.design.DeeDataManager;
import com.seeyon.ctp.form.modules.event.CollaborationEventTask;
import com.seeyon.ctp.form.modules.event.CollaborationFormBindEventListener;
import com.seeyon.ctp.form.service.FormCacheManager;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.util.Dom4jxmlUtils;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.dee.client.service.DEEConfigService;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

/**
 * Dee前台调用控制器
 * @author dengxj
 *
 */
public class DeeDataController extends BaseController {

	private FormCacheManager formCacheManager;
    private FormManager formManager;
    private DeeDataManager deeDataManager;
    private CollaborationFormBindEventListener collaborationFormBindEventListener;
    private static final Log LOGGER        = CtpLogFactory.getLog(DeeDataController.class);

    /**跳转到dee数据交换页面
     * @param request
     * @param response
     * @return
     */
    public ModelAndView selectDeeTaskResult(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
    	ModelAndView modelAndView = new ModelAndView("ctp/form/dee/design/selectDeeTaskResult");
    	return modelAndView;
    }



	public void selectDeeDataList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String formId = request.getParameter("formId");
		String fieldName = request.getParameter("fieldName");
		String isFirst = request.getParameter("isFirst");
		Long rightId = request.getParameter("rightId") == null ? 0l : Long.parseLong(request.getParameter("rightId").toString());
		Long contentDataId = Strings.isBlank(request.getParameter("contentDataId")) ? 0l : Long.parseLong(request.getParameter("contentDataId").toString());
		Long recordId = Strings.isBlank( request.getParameter("recordId") ) ? 0l : Long.parseLong(request.getParameter("recordId").toString());
		if (Strings.isBlank(formId)) {
			response.getWriter().print("selectAgain");
			return;
		}
		InfoPath_DeeTask deetask = null;
		FormBean formBean = formCacheManager.getForm(Long.valueOf(formId));
		if(formBean == null){
			response.getWriter().print("selectAgain");
			return;
		}
		FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
		if(formFieldBean == null){
			response.getWriter().print("selectAgain");
			return;
		}
		deetask = formFieldBean.getDeeTask();
		if(deetask == null){
			response.getWriter().print("selectAgain");
			return;
		}
		if( StringUtils.isNotBlank(deetask.getIsLoad()) && "on".equals(deetask.getIsLoad()) ){
			try {
				boolean flag = true;
				//判断当前选中的结果集是否存在从表，存在不回填
				String metaXml = DEEConfigService.getInstance().getFlowMeta(deetask.getId());
				if(metaXml !=null && metaXml.length()>0){
					Document doc = Dom4jxmlUtils.paseXMLToDoc(metaXml);
					Element root = doc.getRootElement();
					List apps = root.elements("App");
					for (Object item : apps) {
						Element appEle = (Element) item;
						InfoPath_DeeResultApp app = new InfoPath_DeeResultApp();
						app.loadFromXml(appEle);
						if (app != null && app.getName().equals(deetask.getName())) {
							List<InfoPath_DeeResultTable> tableList = app.getTableList();
							if (tableList != null && tableList.size() > 0) {
								for(InfoPath_DeeResultTable tb : tableList){
									if("slave".equals(tb.getTabletype())){
										flag = false;
										break;
									}
								}
							}
							
						}
					}
				}
				
				if(flag){
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("formId", formId);
					param.put("fieldName", fieldName);
					param.put("contentDataId", contentDataId);
					param.put("recordId", recordId);
					FlipInfo flipInfo = deeDataManager.getDeeMasterDataListByMasterId(new FlipInfo(), param);
		            /*
		            * 如果是第一次查询
					* 如果开启了自动加载并且查询结果只有一条
					* 则直接回填表单
					* */
					if (isFirst != null) {
						if (flipInfo.getTotal() == 1) {
							Map<String, Long> deeMap = new HashMap<String, Long>();
							deeMap = (Map) flipInfo.getData().get(0);
							Long masterId = deeMap.get("id");
							DeeMasterDataBean ddmb = deeDataManager.getSessionMasterDataBean(masterId);
							Map<String, List<DeeSubDataBean>> subMap = ddmb.getSubTables();
							if (subMap.size() == 0) {
								oneDeeDataFill4Form(contentDataId, Long.valueOf(formId), rightId, recordId, fieldName, Long.valueOf(masterId), response);
							} else {
								response.getWriter().print("selectAgain");
							}
						} else {
							response.getWriter().print("selectAgain");
						}
					}
				}else{
					response.getWriter().print("selectAgain");
				}
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
				DataContainer dc = new DataContainer();
				dc.add(FormConstant.SUCCESS, "false");
				response.getWriter().print(dc.getJson());
			}
		}else{
			response.getWriter().print("selectAgain");
		}
		
	}
 
	 
    /**
     * dee展现列表。将dee数据存入缓存。同时进行表格展现主表数据。
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	public ModelAndView selectDeeDeeDataList(HttpServletRequest request,HttpServletResponse response) throws Exception{
		ModelAndView modelAndView = null;
		String formId = request.getParameter("formId");
		String fieldName = request.getParameter("fieldName");
		Long contentDataId = request.getParameter("contentDataId")==null ? 0l : Long.parseLong(request.getParameter("contentDataId").toString());
		Long recordId = request.getParameter("recordId")==null ? 0l : Long.parseLong(request.getParameter("recordId").toString());
		if(Strings.isBlank(formId)){
			return null ;
		}
		
		Map<String,Object> result = deeDataManager.selectDeeDeeDataList(formId, fieldName, contentDataId, recordId);
		if(result == null)
			return null;
		Object listType = result.get("listType")!=null?result.get("listType").toString():"";
		if(Constants.ListType_TREE.equals(listType)){//树结构列表
			modelAndView = new ModelAndView("ctp/form/dee/design/deeMasterDataTreeList");
		}else{
			modelAndView = new ModelAndView("ctp/form/dee/design/deeMasterDataList");
		}
		
		if( result.size()>0 ){
			for(String key:result.keySet()){
				modelAndView.addObject(key, result.get(key));
			}
		}
		return modelAndView;
	}

	/*
    * 查询到dee任务的结果集只有一条，则调用该方法直接进行回填，不再弹出选择页面
    *
    * */
	private void oneDeeDataFill4Form(Long contentDataId, Long formId, Long rightId, Long recordId, String fieldName, Long masterId, HttpServletResponse response ) {
		response.setContentType("text/html;charset=UTF-8");
 
		String result = deeDataManager.deeDataFill4Form(formId, fieldName, contentDataId, rightId, recordId, masterId, null);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.println(result);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
		out.flush();
        out.close();
	}

	/**
	 * dee数据回填表单
	 * 第一步：将dee数据转换到表单缓存数据当中
     * 第二步：将前台展现的html写入resultMap结果集当中
     * 第三步：清空dee数据缓存
     * 第四步：调用前台方法回填
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ModelAndView deeDataFill4Form(HttpServletRequest request,HttpServletResponse response){
		response.setContentType("text/html;charset=UTF-8");
//		String masterIds = request.getParameter("masterIds");
		Long contentDataId = request.getParameter("contentDataId")==null ? 0l : Long.parseLong(request.getParameter("contentDataId").toString());
		Long formId = request.getParameter("formId")==null ? 0l : Long.parseLong(request.getParameter("formId").toString());
		Long rightId = request.getParameter("rightId")==null ? 0l : Long.parseLong(request.getParameter("rightId").toString());
		Long recordId = request.getParameter("recordId")==null ? 0l : Long.parseLong(request.getParameter("recordId").toString());
		String fieldName = request.getParameter("fieldName");
		Long masterId = request.getParameter("masterId") == null ? 0l : Long.parseLong(request.getParameter("masterId").toString());
		String detailRows = request.getParameter("detailRows");
		
		String result = deeDataManager.deeDataFill4Form(formId, fieldName, contentDataId, rightId, recordId, masterId, detailRows);
		
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.println(result);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
		out.flush();
        out.close();
		return null;
	}

	/**
     * dee重复表数据展示
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	public ModelAndView selectSubDeeDataList(HttpServletRequest request,HttpServletResponse response) throws BusinessException{
		ModelAndView mav = new ModelAndView("ctp/form/dee/design/deeSubDataList");
		String masterId = request.getParameter("masterId");
		if(masterId == null || "".equals(masterId))
			return mav;

		String formId = request.getParameter("formId");
		String fieldName = request.getParameter("fieldName");
		if(Strings.isBlank(formId)){
			return null ;
		}
		mav.addObject("slaveHtmlBean",deeDataManager.selectSubDeeDataList(Long.valueOf(formId),fieldName,masterId));
		return mav;
	}
	
	
	//忽略字段大小写替换Map取值
	private String getKey2MapValue(String inputKey,Map<String, String> fieldMap){
		if(inputKey == null ||fieldMap == null || fieldMap.size() < 1) return null;
		String retObj = null;
		for(Entry<String,String> dValue:fieldMap.entrySet()){
			if(dValue == null) continue;
			if(inputKey.equalsIgnoreCase(dValue.getKey())){
				retObj = (String) dValue.getValue();
				break;
			}
		}
		return retObj;
	}
	private Map<String,String> makeFieldNameAndDisplay(List<InfoPath_DeeField> fieldList){
		Map<String,String> fieldMap = new HashMap<String,String>();
		for(InfoPath_DeeField df:fieldList){
			if(df == null) continue;
			//主表过滤
			if("true".equalsIgnoreCase(df.getIsmaster())) continue;
			fieldMap.put(df.getName(), df.getDisplay());
		}
		return fieldMap;
	}

	/**
     * 获取动态列头信息
     * @param showFields
     * @param formBean
     * @return
     */
    private String getTheadStr(List<InfoPath_DeeField> fieldList4Query) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        int i = 0;
        for (InfoPath_DeeField showField : fieldList4Query) {
        	if("".equals(showField.getName())){
        		continue;
        	}
            sb.append("{");
            sb.append("\"").append("display\":\"").append(showField.getDisplay()).append("\",");
            sb.append("\"").append("name\":\"").append(showField.getName()).append("\",");
            sb.append("\"").append("sortable\":\"").append("false").append("\",");
            sb.append("\"").append("align\":\"").append("center").append("\",");
            sb.append("\"").append("width\":\"").append("100").append("\"");
            if (i == fieldList4Query.size() - 1) {
                sb.append("}");
            } else {
                sb.append("},");
            }
            i++;
        }
        sb.append("]");
        return sb.toString();
    }
	/**
     * dee判断主表记录是否有关联从表记录
     * @param request
     * @param response
     * @return
	 * @throws IOException
     * @throws Exception
     */
	public ModelAndView hasSubDeeData(HttpServletRequest request,HttpServletResponse response) throws BusinessException, IOException{
		String masterId = request.getParameter("masterId");
		JSONObject jsonObject = new JSONObject();
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		if(masterId == null || "".equals(masterId)){
			jsonObject.put("error", "未获取到主表ID");
			out.write(jsonObject.toString());
			return null;
		}
		boolean flag = deeDataManager.hasSubDeeData(Long.parseLong(masterId));
		if(flag){
			jsonObject.put("hasSub", "1");
		}else{
			jsonObject.put("hasSub", "0");
		}
//		
//		DeeMasterDataBean ddmb = deeDataManager.getSessionMasterDataBean(Long.parseLong(masterId));
//		if(ddmb != null && !ddmb.getSubTables().isEmpty()){
//			jsonObject.put("hasSub", "1");
//		}
//		else{
//			jsonObject.put("hasSub", "0");
//		}
		out.write(jsonObject.toString());
		return null;
	}

    public ModelAndView editEventBind(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/form/design/authDesign/highEventSet");
        String eventBindId = request.getParameter("eventBindId");
        if(Strings.isBlank(eventBindId)){
            eventBindId = String.valueOf(UUIDLong.longUUID());
        }else{
            mav.addObject("editFlag", "true");
        }
        mav.addObject("eventBindId", eventBindId);
        List<String[]> taskList = new ArrayList<String[]>();
        List<CollaborationEventTask> collaborationEventTaskList = collaborationFormBindEventListener.getAllCollaborationEvent();
        if(CollectionUtils.isNotEmpty(collaborationEventTaskList))
        {
            for (CollaborationEventTask collaborationEventTask : collaborationEventTaskList) {
                taskList.add(new String[]{String.valueOf(collaborationEventTask.getId()), collaborationEventTask.getLabel()});
            }
        }
        boolean isHasDee = AppContext.hasPlugin("dee");
        mav.addObject("authTypeValue", request.getParameter("authTypeValue"));
        mav.addObject("taskList", taskList);
        mav.addObject("isHasDee", isHasDee);
        return mav;
    }

    /**
     * 高级查询
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView advanceSearchView(HttpServletRequest request,HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("ctp/form/dee/design/advanceSearch");
    	String formId = request.getParameter("formId");
		String fieldName = request.getParameter("fieldName");
		String listType = request.getParameter("listType");
		if(Strings.isBlank(formId)){
			return null ;
		}
		InfoPath_DeeTask deetask = null;
		List<InfoPath_DeeField> fieldList = null;
		List<InfoPath_DeeField> searchList = new ArrayList<InfoPath_DeeField>();
		FormBean formBean = formCacheManager.getForm(Long.valueOf(formId));
		FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
		deetask = formFieldBean.getDeeTask();

		//查询字段列表,过滤不展示的查询字段
		fieldList = deetask.getTaskFieldList();
		if(fieldList!=null){
			Map<String,String> m = null;
			for(InfoPath_DeeField field:fieldList){
				if("true".equals(field.getChecked())  && "true".equals(field.getIsmaster())){
					if("tree".equals(listType)){
						if(!field.getName().equals(deetask.getListRefField()))
							searchList.add(field);
					}else{
						searchList.add(field);
					}
				}
			}
		}

		JSONArray j = new JSONArray();
		j.addAll(searchList);
		String jsonStr = j.toJSONString();
		mav.addObject("fieldsJson",jsonStr);
    	return mav;
    }

    public void setCollaborationFormBindEventListener(CollaborationFormBindEventListener collaborationFormBindEventListener) {
        this.collaborationFormBindEventListener = collaborationFormBindEventListener;
    }

    public void setFormManager(FormManager formManager) {
        this.formManager = formManager;
    }

    public void setFormCacheManager(FormCacheManager formCacheManager) {
        this.formCacheManager = formCacheManager;
    }

    public void setDeeDataManager(DeeDataManager deeDataManager) {
        this.deeDataManager = deeDataManager;
    }
}