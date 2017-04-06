package com.seeyon.apps.dee.controller;

import com.seeyon.apps.dee.util.Constants;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.dee.bean.*;
import com.seeyon.ctp.form.modules.engin.field.base.RelationInputAtt;
import com.seeyon.ctp.form.modules.engin.field.base.info.ISeeyonForm.TFieldDataType;
import com.seeyon.ctp.form.modules.engin.field.base.info.ISeeyonForm.TFieldInputType;
import com.seeyon.ctp.form.modules.engin.formula.FormulaEnums.FormulaVar;
import com.seeyon.ctp.form.service.FormCacheManager;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.util.Dom4jxmlUtils;
import com.seeyon.ctp.form.util.FormCharset;
import com.seeyon.ctp.form.util.IXmlNodeName;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;
import com.seeyon.v3x.dee.common.db.parameter.model.ParameterBean;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.web.servlet.ModelAndView;

import www.seeyon.com.utils.ReqUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dee设置页面的控制器
 * @author dengxj
 *
 */
public class DeeDesignController extends BaseController {
	
	private static final Log LOGGER        = CtpLogFactory.getLog(DeeDesignController.class);
	    
	 
	private FormCacheManager formCacheManager;
    private FormManager formManager;
   
    public void setFormCacheManager(FormCacheManager formCacheManager) {
		this.formCacheManager = formCacheManager;
	}
	public void setFormManager(FormManager formManager) {
		this.formManager = formManager;
	}
	/**
     * 跳转到数据交换任务设置页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView setDEETask(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("ctp/form/dee/design/setDeeTask");
    	String fieldName = request.getParameter("fieldName");
    	String inputType = request.getParameter("inputType");
    	String oldInputType = request.getParameter("oldInputType");
    	String highOperation = request.getParameter("highOperation");
    	FormBean formBean = formManager.getEditingForm();
    	if(StringUtils.isBlank(highOperation)){
    	      FormFieldBean fieldBean = formBean.getFieldBeanByName(fieldName);
    	       
    	        if(oldInputType!=null && oldInputType.equals(inputType) && fieldBean.getDeeTask()!=null){
    	            InfoPath_DeeTask deeTask = fieldBean.getDeeTask();
    	            List<InfoPath_DeeField> fieldList = deeTask.getTaskFieldList();
    	            List<InfoPath_DeeParam> paramList = deeTask.getTaskParamList();
    	             
    	            //组织参数字符串
    	            StringBuilder sb = new StringBuilder();
    	            if(paramList!=null && paramList.size()>0){
    	                for(InfoPath_DeeParam deeParam : paramList){
    	                    sb.append(deeParam.getName()).append(",").append(deeParam.getValue()).append(",")
    	                                    .append(deeParam.getDisplay()).append(",").append(deeParam.getRealValue()).append("|");//
    	                }
    	                sb.deleteCharAt(sb.length()-1);
    	            }
    	            String paramStr = sb.toString();
    	            sb.delete(0, sb.length());
    	            
    	            //组织字段列表字符串
    	            if(fieldList!=null && fieldList.size()>0){
    	                for(InfoPath_DeeField deeField : fieldList){
    	                    sb.append(deeField.getName()).append(",").append(deeField.getDisplay()).append(",")
    	                                    .append(deeField.getFieldtype()).append(",")//
    	                                    .append(deeField.getFieldlength()).append(",")//
    	                                    .append(deeField.getChecked()).append("|");//
    	                }
    	                sb.deleteCharAt(sb.length()-1);
    	            }
    	            String fieldStr = sb.toString();
    	            sb.delete(0, sb.length());
    	            mav.addObject("taskId", deeTask.getId());
    	            mav.addObject("taskResult", deeTask.getRefResult());
    	            mav.addObject("taskRelField", deeTask.getRefInputName());
    	            mav.addObject("taskParamStr", paramStr);
    	            mav.addObject("taskFieldStr", fieldStr);
    	            
    	            mav.addObject("treeResultOld", deeTask.getTreeResult());//结果集原值
    	            mav.addObject("treeIdOld", deeTask.getTreeId());//分类树id原值
    	            mav.addObject("treeNameOld", deeTask.getTreeName());//分类树name原值
    	            
    	            mav.addObject("treePidOld",deeTask.getTreePid());//分类树pid原值
    	            mav.addObject("listRefFieldOld", deeTask.getListRefField());//列表分类字段原值
    	            if(StringUtils.isBlank(deeTask.getListType())){
    	            	mav.addObject("listTypeOld", Constants.ListType_List);//列表类型原值
    	            }else{
    	            	mav.addObject("listTypeOld", deeTask.getListType());//列表类型原值
    	            }
    	            
    	            mav.addObject("isLoad", deeTask.getIsLoad());//是否自动载入
    	             

    	            
    	        }else{
    	            fieldBean.setDeeTask(null);
    	        }
    	}
  
    	return mav;
    }
    /**
     * 跳转到dee任务选择页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView taskDEEIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("ctp/form/design/triggerDesign/taskDEEIndex");
    	return mav;
    }
    public ModelAndView taskListFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("ctp/form/design/triggerDesign/taskListFrame");
    	return mav;
    }
    
    /**
	 * 获取dee数据对象
	 * @param request
	 * @param response
	 * @return null
	 * @author 舒杨
	 * @throws Exception
	 */
	public ModelAndView getDeeTask(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
				String taskId = request.getParameter("taskId");
				FlowBean task = DEEConfigService.getInstance().getFlow(taskId);
				if(task != null){
					List<ParameterBean> taskParamList = DEEConfigService.getInstance().getFlowPara(taskId);
					Map<String,String> taskParams = new LinkedHashMap<String,String>();//参数
					Map<String,String> taskResults = new LinkedHashMap<String, String>();//结果集
					Map<String,String> ResultTables = new HashMap<String, String>();//结果集下主表表名
					if(taskParamList!=null && taskParamList.size()>0){
						for(ParameterBean param : taskParamList){
							if(!"whereString".equals(param.getPARA_NAME())){
								taskParams.put(param.getPARA_NAME(), param.getPARA_VALUE()+","+param.getDIS_NAME());
							}
						}
					}
					String metaXml = task.getFLOW_META();
					if(metaXml != null && metaXml.length()>0){
						Document doc  = Dom4jxmlUtils.paseXMLToDoc(metaXml);
						Element root = doc.getRootElement();		
						List apps = root.elements("App");
						for (Object item : apps) {
							Element appEle = (Element) item;
							Attribute fattrib = appEle.attribute(IXmlNodeName.name);
							String name = "";
							if (fattrib != null){
								name = FormCharset.getInstance().selfXML2JDK(fattrib.getValue());
								taskResults.put(name, name);
							}
							InfoPath_DeeResultApp app = new InfoPath_DeeResultApp();
							app.loadFromXml(appEle);
							
							List<InfoPath_DeeResultTable> tableList = app.getTableList();
							if (tableList != null && tableList.size() > 0) {
								String tableNames = "";
								for(InfoPath_DeeResultTable tb : tableList){
									if("master".equals(tb.getTabletype())){
										tableNames += tb.getName()+"|";
									}
								}
								ResultTables.put(name, tableNames);
							}
						}
					}
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("taskParamNames", taskParams.keySet());
					jsonObject.put("taskParams", taskParams);
					jsonObject.put("taskResultIds", taskResults.keySet());
					jsonObject.put("taskResults", taskResults);
					jsonObject.put("resultTables", ResultTables);
					jsonObject.put("taskName", task.getDIS_NAME());
					jsonObject.put("taskId", task.getFLOW_ID());
					String string = jsonObject.toString();
					response.setContentType("text/html;charset=utf-8");
					PrintWriter out = response.getWriter(); 
					out.write(string);
				}else{
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("error", "error");//dee返回的錯誤信息
					String string = jsonObject.toString();
					PrintWriter out = response.getWriter(); 
					out.write(string);	
				}
				return null;
	}
	
	/**
	 * 根据选择的dee任务结果集获取对应的字段列表
	 * @param request
	 * @param response
	 * @return null
	 * @throws Exception
	 * @author 舒杨
	 */
	public ModelAndView getDeeTaskField(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String taskId = request.getParameter("taskId");
		String resultId  = request.getParameter("resultId");
		String extendName = request.getParameter("extendName");
		RelationInputAtt relationInputAtt = null;
		try{
			String metaXml = DEEConfigService.getInstance().getFlowMeta(taskId);
			if(metaXml !=null && metaXml.length()>0){
				Document doc = Dom4jxmlUtils.paseXMLToDoc(metaXml);
				Element root = doc.getRootElement();
				List apps = root.elements("App");
				for (Object item : apps) {
					Element appEle = (Element) item;
					InfoPath_DeeResultApp app = new InfoPath_DeeResultApp();
					app.loadFromXml(appEle);
					if (app != null && app.getName().equals(resultId)) {
						relationInputAtt = new RelationInputAtt();
						relationInputAtt.setRefObjName(app.getName());
						List<InfoPath_DeeResultTable> tableList = app.getTableList();
						if (tableList != null && tableList.size() > 0) {
							//取任务数据集字段
							String subTableNames = "";
							for(InfoPath_DeeResultTable tb:tableList){
								if(tb != null){
									boolean isMasterField = "master".equals(tb.getTabletype());
									for(InfoPath_DeeField df:tb.getFieldList()){
										//在字段名前加上表名，以区分主从表相同字段（表名@字段名）
										String fieldDisplay = (tb.getDisplay() == null || "".equals(tb.getDisplay()))?df.getDisplay():(tb.getDisplay()+"-"+df.getDisplay());
										relationInputAtt.putAll(df.getName(),getFormTypeByDEEType(df.getFieldtype()), TFieldInputType.fitText,df.getFieldlength(), "0", Boolean.toString(isMasterField),tb.getName(),fieldDisplay);
									}
									if(isMasterField && tb.getToRelMasterField() != null && !"".equals(tb.getToRelMasterField())){
										relationInputAtt.setRefMasterField(tb.getToRelMasterField());
									}
									//存放重复表中英文名称及表名和display
									subTableNames += tb.getName()+"="+tb.getDisplay()+"|";
								} 
							}
							relationInputAtt.setSubTablesDisplay(subTableNames);
						}
				
					}
				}
			}
		} catch (Exception e){
			LOGGER.info("taskId为" + taskId + "的任务不存在！");
		}
			if(relationInputAtt != null){
				response.setContentType("text/html;charset=utf-8");
				PrintWriter out = response.getWriter(); 
				out.write(relationInputAtt.toJSONobject().toString());				
			}else{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", "error");
				String string = jsonObject.toString();
				PrintWriter out = response.getWriter(); 
				out.write(string);	
			}
		
		return null;
	}
	
	//根据DEE字段类型获取表单字段类型
	private TFieldDataType getFormTypeByDEEType(String deeFieldType){
		TFieldDataType fdt = TFieldDataType.VARCHAR;
		if("varchar".equalsIgnoreCase(deeFieldType)){
			fdt = TFieldDataType.VARCHAR;
		}
		else if("decimal".equalsIgnoreCase(deeFieldType)){
			fdt = TFieldDataType.DECIMAL;
		}
		else if("timestamp".equalsIgnoreCase(deeFieldType)){
			fdt = TFieldDataType.TIMESTAMP;
		}
		else if("longtext".equalsIgnoreCase(deeFieldType)){
			fdt = TFieldDataType.LONGTEXT;
		}
		else if("datetime".equalsIgnoreCase(deeFieldType)){
			fdt = TFieldDataType.DATETIME;
		}
		return fdt;
	}
	/**
	 * 跳转到dee任务参数设置
	 * @param request
	 * @param response
	 * @return mav
	 * @throws Exception
	 * @author 舒杨
	 */
	public ModelAndView setTaskParam(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("ctp/form/dee/design/dataset");
		long formId = ReqUtil.getLong(request, "formId", 0L);
		String formulaStr = ReqUtil.getString(request,"formulaStr","");
		String fieldName = request.getParameter("fieldName");
		FormBean form = null;
		if(formId==0){form = formManager.getEditingForm();
		}else{form = formCacheManager.getForm(formId);}
		//找到表单对应的字段列表
    	List<FormFieldBean> fieldList = form.getAllFieldBeans();
//    	if(fieldName!=null && !"".equals(fieldName)){
//    		for(FormFieldBean field: fieldList){
//    			if(fieldName.equals(field.getName())){
//    				fieldList.remove(field);
//    				break;
//    			}
//    		}
//    	}
    	//系统数据域
    	Map<String,String> sys = new LinkedHashMap<String,String>();
    	for(FormulaVar e : FormulaVar.values()) {
            sys.put(e.getKey(), e.getText());
        }
//        for(SystemDataField e : SystemDataField.values()) {
//        	if(e.isSupport(form.getFormType())){//该系统变量是否支持当前的表单类型
//        		sys.put(e.getKey(), e.getText());
//        	}
//        }
//        //流水号要作为系统变量的一种
//        List<FormSerialNumberBean> numberList = formCacheManager.getAccountUnuseSerialNumberList(form.getId());
        mav.addObject("form",form);//表单信息
        mav.addObject("fieldList",fieldList);//表单信息
        mav.addObject("sys",sys);//系统数据域
		return mav;
	}
}