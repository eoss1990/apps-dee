package com.seeyon.apps.dee.manager;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.dee.util.Constants;
import com.seeyon.apps.dee.util.FlowFormUtil;
import com.seeyon.apps.dee.util.TransDateUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.form.bean.*;
import com.seeyon.ctp.form.dee.bean.*;
import com.seeyon.ctp.form.dee.design.DeeDataManager;
import com.seeyon.ctp.form.modules.engin.base.formData.FormDataManager;
import com.seeyon.ctp.form.modules.engin.formula.FormulaEnums.FormulaVar;
import com.seeyon.ctp.form.modules.engin.relation.FormRelationEnums.ToRelationAttrType;
import com.seeyon.ctp.form.po.FormRelation;
import com.seeyon.ctp.form.service.FormCacheManager;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.util.Dom4jxmlUtils;
import com.seeyon.ctp.form.util.Enums.FieldType;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.form.util.FormUtil;
import com.seeyon.ctp.report.core.bo.Report;
import com.seeyon.ctp.report.data.bo.DataObject;
import com.seeyon.ctp.report.data.bo.DataSet;
import com.seeyon.ctp.report.design.po.ColumnCfg;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformFactory;
import com.seeyon.v3x.dee.client.service.DEEConfigService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

/**
 * dee数据管理类，这里主要是为了缓存dee数据
 * @author Dengxj
 *
 */
public class DeeDataManagerImpl implements DeeDataManager {
	private DeeDataManager deeDataManager;
	private FormDataManager formDataManager;
	private FormManager formManager;
	private FormCacheManager formCacheManager;
	private CollaborationApi collaborationApi;
    private static final Log log = CtpLogFactory.getLog(DeeDataManagerImpl.class);
    
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
	   * 复制InfoPath_DeeField
	   * @param field
	   * @return
	   */
  	private InfoPath_DeeField copyInfoPathDeeField(InfoPath_DeeField field){
  		if(field == null)
  			return field;
  		InfoPath_DeeField copyField = new InfoPath_DeeField();
  		copyField.setChecked(field.getChecked());
  		copyField.setDisplay(field.getDisplay());
  		copyField.setFieldlength(field.getFieldlength());
  		copyField.setFieldtype(field.getFieldtype());
  		copyField.setIsmaster(field.getIsmaster());
  		copyField.setName(field.getName());
  		copyField.setToRelFormField(field.getToRelFormField());
  		copyField.setValue(field.getValue());
  		return copyField;
  	}

  	
    /**
     * 替换列头
     * @param oneRow
     * @param field
     * @return
     */
  	private InfoPath_DeeField exChgDeeFieldName(Map<String,Object> oneRow, InfoPath_DeeField field){
  		if(oneRow == null || oneRow.size() < 1)
  			return field;
  		InfoPath_DeeField copyField = copyInfoPathDeeField(field);
  		for(String fieldName:oneRow.keySet()){
  			if(fieldName != null && fieldName.equalsIgnoreCase(copyField.getName())){
  				copyField.setName(fieldName);
  				break;
  			}
  		}
  		return copyField;
  	}
  	
    
    /**
     * dee展现列表。将dee数据存入缓存。同时进行表格展现主表数据
     */
    public Map<String, Object> selectDeeDeeDataList(String formId,
    		String fieldName, Long contentDataId, Long recordId) {
    	
    	if(Strings.isBlank(formId)){
			return null ;
		}
    	Map<String,Object> result = new HashMap<String, Object>();
		InfoPath_DeeTask deetask = null;
		List<InfoPath_DeeField> fieldList = null;
		FormBean formBean = formCacheManager.getForm(Long.valueOf(formId));
		FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
		deetask = formFieldBean.getDeeTask();
		if(deetask == null)
			return null;
		fieldList = deetask.getTaskFieldList();
		try {
			String treeResultName = "";
			String listType = deetask.getListType();
			if(StringUtils.isBlank(listType)){
				listType = Constants.ListType_List;
			}
			result.put("listType",listType);
			if(Constants.ListType_TREE.equals(listType)){//树结构列表
				result.put("treeId",deetask.getTreeId());
				result.put("treeName",deetask.getTreeName());
				result.put("treePid",deetask.getTreePid());
				result.put("listRefField",deetask.getListRefField());
				//获取树结果集的名称
				String metaXml = DEEConfigService.getInstance().getFlowMeta(deetask.getId());
				if(metaXml !=null && metaXml.length()>0){
					Document doc = Dom4jxmlUtils.paseXMLToDoc(metaXml);
					Element root = doc.getRootElement();
					List apps = root.elements("App");
					for (Object item : apps) {
						Element appEle = (Element) item;
						InfoPath_DeeResultApp app = new InfoPath_DeeResultApp();
						app.loadFromXml(appEle);
						if (app != null && app.getName().equals(deetask.getTreeResult())) {
							List<InfoPath_DeeResultTable> tableList = app.getTableList();
							if (tableList != null && tableList.size() > 0) {
								treeResultName = tableList.get(0).getName();
								break;
							}
						}
					}
				}
			} 
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("formId", formId);
			param.put("fieldName", fieldName);
			param.put("contentDataId", contentDataId);
			param.put("recordId", recordId);
			param.put("treeResultName", treeResultName);
			param.put("listType", listType);//列表类型
			FlipInfo flipInfo = deeDataManager.getDeeMasterDataListByMasterId(new FlipInfo(), param);
			if(Constants.ListType_TREE.equals(listType)){//树结构列表
				Map m = flipInfo.getParams();
				if(m!=null){
					List<Map<String,String>> treeData = m.get("treeDataList")!=null?(List<Map<String,String>>)m.get("treeDataList"):null;
					JSONArray j = new JSONArray();
					j.addAll(treeData);
					String jsonStr = j.toJSONString();
					result.put("treeData", jsonStr);
				}
			}
			//将翻页数据存表单缓存中。
			Map<String, Object> fData = new HashMap<String, Object>();
			fData.put("data", flipInfo.getData());
			fData.put("count", flipInfo.getTotal());
			fData.put("params", flipInfo.getParams());
			deeDataManager.putSessionFlipInfo(fData);
			Map<String,Object> oneRow = new HashMap<String, Object>();
			if(flipInfo != null && flipInfo.getData() != null && flipInfo.getData().size() > 0){
				oneRow = (Map<String, Object>) flipInfo.getData().get(0);
			}
			List<InfoPath_DeeField> fieldList4Query = new ArrayList<InfoPath_DeeField>();
			if (fieldList != null && fieldList.size() > 0) {
				InfoPath_DeeField field = null;
				for (int j = 0; j < fieldList.size(); j++) {
					field = fieldList.get(j);
					//显示被选择的主表列
					if (Boolean.valueOf(field.getChecked()) && Boolean.valueOf(field.getIsmaster())) {
						fieldList4Query.add(exChgDeeFieldName(oneRow,field));
					}
				}
			}
	
			String columnHeader = getTheadStr(fieldList4Query);
			result.put("fieldlist", columnHeader);
			result.put("searchFieldList",fieldList4Query);
			result.put("formId", formId);
			result.put("fieldName", fieldName);
			result.put("contentDataId", contentDataId);
			result.put("recordId", recordId);
			result.put(FormConstant.SUCCESS, "true");
		}catch(Exception e){
			log.error(e.getMessage(),e);
			result.put(FormConstant.SUCCESS, "false");
			result.put(FormConstant.ERRORMSG, e.getMessage());
			result.put("fieldlist", "0");
		}
    	return result;
    }
    /**
     * dee数据回填调用接口
     */
    public String deeDataFill4Form(Long formId, String fieldName,
    		Long contentDataId, Long rightId, Long recordId, Long masterId,
    		String detailRows)  {
    	
    	FormBean form = formCacheManager.getForm(formId);
        FormFieldBean fieldBean = form.getFieldBeanByName(fieldName);
		//1、获取前台缓存数据
        FormDataMasterBean cacheMasterData = formManager.getSessioMasterDataBean(contentDataId);
        FormAuthViewBean authViewBean = null;
        
       //首先考虑从缓存的FormDataMasterBean中获取权限，因为FormDataMasterBean存放的权限是合并之后的权限
        if(cacheMasterData.getExtraMap().containsKey(FormConstant.viewRight)){
            authViewBean = (FormAuthViewBean)cacheMasterData.getExtraAttr(FormConstant.viewRight);
        }
        if(authViewBean==null){
            authViewBean = form.getAuthViewBeanById(rightId);
        }
        
        String oldAccess = "";
		FormAuthViewFieldBean auth= authViewBean.getFormAuthorizationField(fieldName);
		if(auth!=null){
			oldAccess = auth.getAccess();
			auth.setAccess("edit");
		}
        DataContainer dc = new DataContainer();
        Map<String,Object> resultMap = new DataContainer();
		try {
            //对resultMap进行填返回值
            transDeeData4FormData(form,fieldBean,authViewBean,masterId,cacheMasterData,resultMap,recordId,detailRows);
            dc.add(FormConstant.SUCCESS, "true");
            if(null!=cacheMasterData.getExtraAttr(FormConstant.viewRight)){
            	FormAuthViewBean currentAuth = (FormAuthViewBean)cacheMasterData.getExtraAttr(FormConstant.viewRight);
            	dc.add(FormConstant.viewRight, String.valueOf(currentAuth.getId()));
            }
			if(FormUtil.isH5()){
				FormUtil.putInfoToThreadContent(cacheMasterData,form,authViewBean);
			}
            dc.put(FormConstant.RESULTS, resultMap);
            if(!"".equals(oldAccess)) auth.setAccess(oldAccess);
		} catch (BusinessException e) {
            dc.add(FormConstant.RESULTS, "false");
            dc.add(FormConstant.ERRORMSG, e.getMessage());
            if(!"".equals(oldAccess)) auth.setAccess(oldAccess);
        } finally {
            //清除当前dee数据缓存
    		String deeMasterIds = cacheMasterData.getExtraAttr("deeMasterIds") == null ? null : cacheMasterData.getExtraAttr("deeMasterIds").toString();
    		if(deeMasterIds != null && !"".equals(deeMasterIds)) deeDataManager.removeCurrentDeeCache(deeMasterIds);
        }
		return dc.getJson();
    }
    
 
    public List<DeeSlaveHtmlBean> selectSubDeeDataList(long formId,
    		String fieldName, String masterId) throws BusinessException {
		 
		InfoPath_DeeTask deetask = null;
//		List<InfoPath_DeeField> fieldList = null;
		FormBean formBean = formCacheManager.getForm(formId);
		FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
		deetask = formFieldBean.getDeeTask();
//		fieldList = deetask.getTaskFieldList();
//		Map<String,String> fieldMap = makeFieldNameAndDisplay(fieldList);
		DeeMasterDataBean ddmb = deeDataManager.getSessionMasterDataBean(Long.parseLong(masterId));
		Map<String,List<DeeSubDataBean>> subMap = ddmb.getSubTables();
		DeeSlaveHtmlBean dshb = null;
		List<DeeSlaveHtmlBean> slaveHtmlBeanList = new ArrayList<DeeSlaveHtmlBean>();
		List<String> columnHeader = null;
		List<Map<String,Object>> dataSet = null;
//		Map<String,Object> data4HeaderKey = null;
		Map<String,Object> dValue = null;//字段名称-字段值
		List<String> names = null;
		Map<String, InfoPath_DeeResultTable> tables = new HashMap<String, InfoPath_DeeResultTable>();
		String metaXml = DEEConfigService.getInstance().getFlowMeta(deetask.getId());
		
		//获取dee任务绑定的列表结果集，并获取结果集中的主表和从表信息
		if(metaXml !=null && metaXml.length()>0){
			Document doc = Dom4jxmlUtils.paseXMLToDoc(metaXml);
			Element root = doc.getRootElement();
			List apps = root.elements("App");
			for (Object item : apps) {
				Element appEle = (Element) item;
				InfoPath_DeeResultApp app = new InfoPath_DeeResultApp();
				app.loadFromXml(appEle);
				//判断当前app是否是dee任务绑定的结果集
				if (app != null && app.getName().equals( deetask.getRefResult() ) ){
					  List<InfoPath_DeeResultTable> tableList = app.getTableList();
					  if (tableList != null && tableList.size() > 0) {
						  for(InfoPath_DeeResultTable tb:tableList){
							  tables.put(tb.getName(),tb);
						  }
					  }
					  break;
				}
			}
		}
		
		//构建子表的头，数据信息
		for(Entry<String, List<DeeSubDataBean>> map : subMap.entrySet()){//获取子表
			
			String subName = map.getKey();
			columnHeader = new ArrayList<String>();
			dataSet = new ArrayList<Map<String,Object>>();
			names = new ArrayList<String>();//key为字段name，value为displayName
			InfoPath_DeeResultTable table = tables.get(subName);
			if(table!=null){
				//判断当前表是否是主表，如果是主表，忽略，不是主表，而是从表，则进行头，数据构建
				if(!table.getTabletype().equals("master")){
					//获取子表字段
					List<InfoPath_DeeField> fs = table.getFieldList();
					if( fs!=null && fs.size() > 0 ){
						//表头设置
						for(InfoPath_DeeField df:fs){
							String fieldDisplay = (table.getDisplay() == null || "".equals(table.getDisplay()))?df.getDisplay():(table.getDisplay()+"-"+df.getDisplay());
							columnHeader.add(fieldDisplay );
							names.add(df.getName());
						}	
					}
					
					for(DeeSubDataBean dsdb : map.getValue()){//循环构建子表数据
						if(dsdb.getDataValue() != null){
							dValue = new LinkedHashMap<String,Object>();
							for(String name : names){
								dValue.put(name, dsdb.getDataValue().get(name));
							}
							if(dValue.size() > 0)
								dataSet.add(dValue);
						}
					}
					//子表html构建
					// dshb = new DeeSlaveHtmlBean(ddmb.getSubTablesDisplay().get(map.getKey()), subName,columnHeader, dataSet);
					dshb = new DeeSlaveHtmlBean(subName,columnHeader, dataSet);
					slaveHtmlBeanList.add(dshb);
					
				}
			}
			
		}
 
//		mav.addObject("slaveHtmlBean",slaveHtmlBeanList);
		return slaveHtmlBeanList;
    }
  
    @Override
    public boolean hasSubDeeData(long masterId) {
    	DeeMasterDataBean ddmb = getSessionMasterDataBean(masterId);
		if(ddmb != null && !ddmb.getSubTables().isEmpty()){
			return true;
		}
    	return false;
    }
    
    /**
	 * 获取dee表格主表数据,做实时翻页
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getDeeMasterDataListByMasterId(final FlipInfo flipInfo, Map<String, Object> params) throws BusinessException{
		//已经执行该方法获取DEE数据后，将不再执行本方法
		//返回该分页数据，并移除缓存数据
		Map fData = getSessionFlipInfo();
		if(fData != null){
			revSessionFlipInfo();
			flipInfo.setData((List)fData.get("data"));
			flipInfo.setTotal((Integer)fData.get("count"));
			flipInfo.setParams((Map)fData.get("params"));
			return flipInfo;
		}
		
		String formId = params.get("formId") == null ? "" : params.get("formId").toString();
		String fieldName = params.get("fieldName") == null ? "" : params.get("fieldName").toString();
		Long contentDataId = params.get("contentDataId")==null || "".equals(params.get("contentDataId").toString()) ? 0l : Long.parseLong(params.get("contentDataId").toString());
		Long recordId = params.get("recordId")==null ||"".equals(params.get("recordId").toString()) ? 0l : Long.parseLong(params.get("recordId").toString());
		if(Strings.isBlank(formId)){
			return new FlipInfo();
		}
		String searchName = params.get("searchName") == null ? "" : params.get("searchName").toString();
		String searchValue = params.get("searchValue") == null ? "" : params.get("searchValue").toString();
		String treeResultName = params.get("treeResultName") == null ? "":params.get("treeResultName").toString();
		
		String listRefField = params.get("listRefField") == null ? "":params.get("listRefField").toString();
		String listRefFieldValue = params.get("listRefFieldValue") == null ? "":params.get("listRefFieldValue").toString();
		Map<String,String> searchParam = new HashMap<String, String>();
		//树结构 点击节点，产生的列表父id名称和值
		if( !"".equals(listRefField) && !"".equals(listRefFieldValue) ){
			searchParam.put(listRefField, listRefFieldValue);
		}
		
		String listType = params.get("listType") == null ? "":params.get("listType").toString();
		
		List advanceSearch = params.get("advanceSearch")!=null?(List)params.get("advanceSearch"):null;
		
		//将deemasterIds存入表单缓存中数据先清空。  手机端没有换页 但在加载剩下的数据前，后台会清理前一页的缓存
		//但手机端就就可以在加载下一页的数据后，继续选择前页的数据，但前页的缓存数据已经被清理了
		FormDataMasterBean cacheMasterData = formManager.getSessioMasterDataBean(contentDataId);
		String deeMasterIds = cacheMasterData.getExtraAttr("deeMasterIds") == null ? null : cacheMasterData.getExtraAttr("deeMasterIds").toString();
//		if(deeMasterIds != null && !"".equals(deeMasterIds)) removeCurrentDeeCache(deeMasterIds);

		InfoPath_DeeTask deetask = null;
		List<InfoPath_DeeParam> taskParamList = null;
		FormBean formBean = formCacheManager.getForm(Long.valueOf(formId));
		FormFieldBean formFieldBean = formBean.getFieldBeanByName(fieldName);
		deetask = formFieldBean.getDeeTask();
		taskParamList = deetask.getTaskParamList();
		// 获取任务数据并解析数据
		DEEClient client = new DEEClient();
		try{
			//获取parameters
			Parameters parameters = getDeeTaskParameters(taskParamList,formBean,contentDataId,recordId,searchName,searchValue,flipInfo.getPage(),flipInfo.getSize(),searchParam);
			parameters.add("treeResultName", treeResultName);//树结构结果集
//			parameters.add("deeListType", listType);//dee 是否树形或列表
			parameters.add("listTreeResultName", deetask.getTablename());//dee任务
			if(advanceSearch!=null){//高级查询
				parameters.add("advanceSearch", advanceSearch);
			}
			Map<String, String> formFlow_Data = FlowFormUtil.getFlowFormData(collaborationApi, 
					cacheMasterData.getId(), params.get("fieldName") == null 
					? "" : params.get("fieldName").toString());
			parameters.add("formFlow_Data", formFlow_Data);
			//根据任务id从dee端获取数据。
			com.seeyon.v3x.dee.Document deeDoc = client.execute(deetask.getId(),parameters);
			Document doc = null;
			if (deeDoc != null) {
				String deeResultXml = deeDoc.toString();
				doc = Dom4jxmlUtils.paseXMLToDoc(deeResultXml);
			}
			int totalRecord = 0;
			if (doc != null && doc.getRootElement() != null) {
				@SuppressWarnings("unchecked")
				List<Element> eleList = doc.getRootElement().elements();
				if(eleList == null || eleList.size() < 1 || eleList.get(0) == null)
					return flipInfo;
				for(Element e:eleList){
					//获取控件列表的记录总数
					if(e.getName().equals(deetask.getTablename())){
						totalRecord = Integer.parseInt(e.attribute("totalCount").getValue());
						break;
					}
				}
				//解析主表，重复表数据
				List<DeeMasterDataBean> masterDataList = getRowDataListByElement(eleList,deetask);
				if(Constants.ListType_TREE.equals(listType)){//如果树
					List<Map<String,String>> treeDataList = getTreeDataListByElement(eleList,deetask,treeResultName);
					Map m = flipInfo.getParams();
					if( m!=null ) {
						m.put("treeDataList", treeDataList);
					} else {
						m = new HashMap();
						m.put("treeDataList", treeDataList);
						flipInfo.setParams(m);
					}
						
				}
				List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
				StringBuilder masterIds = new StringBuilder();
				for(DeeMasterDataBean ddb:masterDataList){
					if(ddb == null)
						continue;
					Map<String,Object> dataValue = ddb.getDataValue();
					if(!"".equals(searchName) && !"".equals(searchValue)){
						Object currentValue = dataValue.get(searchName);
						//如果为null,说明该值为空，继续
						if(currentValue == null){
							continue;
						}
					}
					//构造dee那边有id列的话，这边为了前台展示，将id列重命名。
					Map<String,Object> newDataValue = new LinkedHashMap<String,Object>();
					newDataValue.putAll(dataValue);
					for(String dvKey:newDataValue.keySet()){
						if("id".equalsIgnoreCase(dvKey)){
							Object idValue = newDataValue.get(dvKey);
							newDataValue.remove(dvKey);
							newDataValue.put("column_@dee@newId@_id", idValue);
							break;
						}
					}
					newDataValue.put("id", ddb.getId());
					datas.add(newDataValue);
					
					//将查询主从表数据对象放入缓存
					masterIds.append("@deeId@"+ddb.getId());
					putSessionMasterDataBean(ddb);
				}
				//将deemasterIds存入表单缓存中。
				if(deeMasterIds!=null){
					masterIds.append(deeMasterIds);
				} 
				cacheMasterData.putExtraAttr("deeMasterIds", masterIds.toString());
				flipInfo.setData(datas);
				flipInfo.setTotal(totalRecord);
			}
		} 
		catch (InvocationTargetException ex){
			String errMsg = "";
			if(ex.getMessage() == null && ex.getTargetException() != null){
				errMsg = ex.getTargetException().getMessage();
			}
			else if(ex.getMessage() != null){
				errMsg = ex.getMessage();
			}
			log.error(errMsg,ex);
			throw new BusinessException(ResourceUtil.getString("form.create.input.setting.deetask.resultdata.error.label"));
		}
		catch (Exception ex) {
			log.error(ex);
			throw new BusinessException(ResourceUtil.getString("form.create.input.setting.deetask.resultdata.error.label"));
		}
		return flipInfo;
	}

 
	
	private Parameters getDeeTaskParameters(List<InfoPath_DeeParam> taskParamList, FormBean formBean, Long contentDataId,
                                            Long recordId, String condition, String textfield, int pageNumber, int pageSize,Map<String,String> searchParam){
		FormulaVar[] varArr = FormulaVar.values();
		Parameters parameters = new Parameters();
		Object fieldVal = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<FormFieldBean> frmList = formBean.getAllFieldBeans();
		FormDataMasterBean cacheMasterData = formManager.getSessioMasterDataBean(contentDataId);
		FormDataSubBean frmSubBean = null;
		for(FormFieldBean frmField:frmList){
			if(frmField != null){
				if(frmField.isMasterField()){
					//参数绑定主表字段
					fieldVal = cacheMasterData.getFieldValue(frmField.getName());
//					paramMap.put(frmField.getName(), fieldVal);
					paramMap.put(frmField.getDisplay(), TransDateUtil.format2DEE(frmField.getFieldType(),fieldVal));
				}
				else{
					frmSubBean = cacheMasterData.getFormDataSubBeanById(frmField.getOwnerTableName(), recordId);
					if(frmSubBean != null){
						fieldVal = frmSubBean.getFieldValue(frmField.getName());
						paramMap.put(frmField.getDisplay(), TransDateUtil.format2DEE(frmField.getFieldType(),fieldVal));
					}
//					cacheMasterData.getSubData(subTableName)
				}
			}
		}
		if(taskParamList != null && taskParamList.size()>0){
			for(int i = 0; i < taskParamList.size(); i++){
				InfoPath_DeeParam param = taskParamList.get(i);
				String value = param.getValue();
				if(value == null){
					parameters.add(param.getName(), null);
					continue;
				} 
				if(value.startsWith("{")){
					//如果是系统变量则取真实值param.getRealValue()
					if(param.getRealValue() == null || "".equals(param.getRealValue()))
						continue;
					value = param.getRealValue().substring(1, param.getRealValue().length()-1);
					for(int j=0; j<varArr.length; j++){
						if(varArr[j].getKey().equals(value)){
							parameters.add(param.getName(), varArr[j].getValue());
							break;
						}
					}
				}else if(paramMap.containsKey(value)){
					//参数绑定表单主表字段值
					parameters.add(param.getName(), paramMap.get(value)!=null?paramMap.get(value):"");//值判空，防止带参数的任务，参数是null，eval计算报错
				}else{
					//参数赋值常量
					parameters.add(param.getName(), value!=null?value:"");//值判空，防止带参数的任务，参数是null，eval计算报错
				}
				//parameters.add(param.getName(), paramMap.get(param.getValue()));
			}
		}
		if(condition != null && condition.length()>0 && textfield != null){
			if(Strings.isNotBlank(textfield)){
				//对单引号进行防护
				textfield = textfield.replace("'", "''");
			}
			if(isChinese(textfield)){//如果有中文字符，加N前缀，让数据库已unicode进行解析
				parameters.add("DeeListconditionStr",  condition + " like N'%" +SQLWildcardUtil.escape(textfield) +"%'");
			}else{
				parameters.add("DeeListconditionStr",  condition + " like '%" +SQLWildcardUtil.escape(textfield) +"%'");
			}
			
		}
//		else{
		parameters.add("whereString"," where 1=1");
//		}
		if(searchParam.size() > 0){
//			String where = parameters.getValue("whereString").toString();
//			StringBuilder str = new StringBuilder(where);
			//树结构父id sql拼装
			for(Entry<String, String> entry : searchParam.entrySet()){
				String key = entry.getKey();
				String value = entry.getValue();
				parameters.add(key, value);
				if(value.indexOf(",")!=-1){
					String[] ids = value.split(",");
					if(ids!=null && ids.length > 0){
						StringBuilder idStr = new StringBuilder(" in( ");
						for(int i=0;i<ids.length ;i++ ){
							idStr.append(" '").append(ids[i]).append("' ");
							if( i < (ids.length -1) ){
								idStr.append(",");
							}
						}
						idStr.append(" ) ");
						parameters.add("DeeListPidStr",key + idStr.toString() );
					}
				}else{
					parameters.add("DeeListPidStr",key + " in( '" +value+ "' ) " );
				}
				
			}
//			parameters.add("whereString",str.toString());
		}
//		//放入高级查询
//		parameters.add("advanceSearch",searchParam);
		parameters.add(DEEConfigService.PARAM_PAGENUMBER, Integer.valueOf(pageNumber));
		parameters.add(DEEConfigService.PARAM_PAGESIZE, Integer.valueOf(pageSize));
		return parameters;
	}
	
	
	// 根据Unicode编码完美的判断中文汉字和符号
	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	// 完整的判断中文汉字和符号
	private boolean isChinese(String strName) {
		if(StringUtils.isBlank(strName)){
			return false;
		}
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}
		
	
	public List<Map<String,String>> getTreeDataListByElement(List<Element> eleList, InfoPath_DeeTask deetask,String treeResultName){
		if(eleList == null) return null;
		List<Map<String,String>> treeList = new ArrayList<Map<String,String>>();
		 
		Map<String,String> deeTreeData = null;
		int totalCount = 0;
		for (Element e : eleList) {
			if (e == null) continue;
			if (e.getName() != null && e.getName().equals(treeResultName)) {
				totalCount = Integer.parseInt(e.attribute("totalCount").getValue());
				if (totalCount < 1) continue;
				List<Element> rows = e.elements("row");
				for (Element row : rows) {
					List<Element> colums = row.elements();
					deeTreeData = new LinkedHashMap<String, String>();
					for (Element column : colums) {
						if(column == null || column.getName() == null)
							continue;
//						if(column.getData() == null){
						deeTreeData.put(column.getName(),column.getText());
//						}
					}
					 
					//设置
					treeList.add(deeTreeData);
				}
			} 
		}
		return treeList;
	}
	
	public List<DeeMasterDataBean> getRowDataListByElement(List<Element> eleList, InfoPath_DeeTask deetask){
		if(eleList == null) return null;
		List<DeeMasterDataBean> masterList = new ArrayList<DeeMasterDataBean>();
		int totalCount = 0;
		//获取主从表关联信息
		Map<String,String> relFieldMap = deetask.getMasterSubRelMap();
		Map<String, Object> dataValue = null;
		//主表数据对象
		DeeMasterDataBean deeMasterData = null;
		//解析主表数据
		for (Element e : eleList) {
			if (e == null) continue;
			totalCount = Integer.parseInt(e.attribute("totalCount").getValue());
			if (totalCount < 1) continue;
			if (e.getName() != null && e.getName().equals(deetask.getTablename())) {
				@SuppressWarnings("unchecked")
				List<Element> rows = e.elements("row");
				// 主表数据
				for (Element row : rows) {
					@SuppressWarnings("unchecked")
					List<Element> colums = row.elements();
					dataValue = new LinkedHashMap<String, Object>();
					for (Element column : colums) {
						if(column == null || column.getName() == null)
							continue;
						if(column.getData() == null){
							dataValue.put(column.getName(),column.getData());
						}
						else{
							//获取字段信息
							InfoPath_DeeField df = getFieldByName(column.getName(),deetask);
							//如果是varchar则比较长度
							if(df != null && "VARCHAR".equalsIgnoreCase(df.getFieldtype()) 
									&& df.getFieldlength() != null && !"".equals(df.getFieldlength())){
								String dVal = column.getData().toString().trim();
								//如果字段实际长度大于字段长度则截取前长度
								int len = Integer.parseInt(df.getFieldlength());
								if(dVal.length() > len){
									dVal = dVal.substring(0,len-1);
								}
								dataValue.put(column.getName(),dVal);
							}
							else{
								dataValue.put(column.getName(),column.getData());
							}
						}
					}
					deeMasterData = new DeeMasterDataBean();
					deeMasterData.setDataValue(dataValue);
					deeMasterData.setIdIfNew();
					//设置
					masterList.add(deeMasterData);
				}
			}
		} 
		
		
		//解析从表
		for(DeeMasterDataBean masterRow: masterList){
			if(masterRow == null || masterRow.getDataValue() == null)
				continue;
			//循环外键列(从表名.字段-主表字段)
			for(Map.Entry<String,String> entry : relFieldMap.entrySet()){
				//外键值
				Object masterFieldValue = masterRow.getDataValue().get(entry.getValue());
				if(masterFieldValue == null) continue;
				getSubDeeData(masterFieldValue,entry.getKey(),deetask.getTablename(),eleList,masterRow);
			}
			masterRow.setSubTablesDisplay(deetask.getSubTablesDisplay());
		}
		
		return masterList;
	}
	
	//解析从表
	private void getSubDeeData(Object masterFieldValue, String refTbFieldName, String masterTableName, List<Element> eleList, DeeMasterDataBean masterRow){
		int totalCount = 0;
		Map<String, Object> dataValue = null;
		DeeSubDataBean deeSubData = null;
		for (Element e : eleList) {
			if (e == null) continue;
			totalCount = Integer.parseInt(e.attribute("totalCount").getValue());
			if (totalCount < 1) continue;
			if(e.getName() == null || e.getName().equals(masterTableName)) continue;
			String subTableName = e.getName();
			@SuppressWarnings("unchecked")
			List<Element> rows = e.elements("row");
			// 主表数据
			for (Element row : rows) {
				@SuppressWarnings("unchecked")
				List<Element> colums = row.elements();
				dataValue = new LinkedHashMap<String, Object>();
				//判断是否添加到主表对应的从表中
				boolean needFlag = false;
				for (Element column : colums) {
					if(column == null || column.getName() == null)
						continue;
					if(refTbFieldName.equals(subTableName+"."+column.getName()) && masterFieldValue.equals(column.getData())){
						needFlag = true;
					}
					dataValue.put(column.getName(),column.getData());
				}
				if(!needFlag) continue;
				deeSubData = new DeeSubDataBean();
				deeSubData.setDataValue(dataValue);
				deeSubData.setDeeMasterId(masterRow.getId());
				masterRow.addSubData(subTableName, deeSubData);
			}
		} 
	}

	//根据字段名获取字段相关信息
	private InfoPath_DeeField getFieldByName(String fieldName, InfoPath_DeeTask deetask){
		InfoPath_DeeField df = null;
		if(deetask == null || deetask.getTaskFieldList() == null ||fieldName == null ||"".equals(fieldName)){
			return df;
		}
		for(InfoPath_DeeField idf:deetask.getTaskFieldList()){
			if(idf == null) continue;
			if(fieldName.equalsIgnoreCase(idf.getName()))
				return idf;
		}
		return df;
	}

	/**
	 * 验证数据类型
	 * @param fieldType
	 * @param fieldLen
	 * @param fieldValue
	 * @return
	 * @throws BusinessException
	 */
	private boolean checkTransData(String fType,Object fValue){
		if(fValue == null || "".equals(fValue))
			return true;
		boolean retFlag = false;
		FieldType fieldType = FieldType.getEnumByKey(fType.toUpperCase());
		switch(fieldType){
			case DECIMAL:
				try{
					new java.math.BigDecimal(fValue.toString());
					retFlag = true;
				}
				catch(Exception e){}
				break;
			case TIMESTAMP:
				try{
//					Timestamp.valueOf(fValue.toString());
//					retFlag = true;
					//对应日期
					java.text.SimpleDateFormat sf = new java.text.SimpleDateFormat ("yyyy-MM-dd");
					sf.parse(fValue.toString());
					retFlag = true;
				}
				catch(Exception e){}
				break;
			case DATETIME:
				try{
					java.text.SimpleDateFormat sf = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm");
					sf.parse(fValue.toString());
					retFlag = true;
				}
				catch(Exception e){}
				break;
			default:
				retFlag = true;
				break;
		}
		return retFlag;
	}
 
	
	@Override
	public Map<String, Object> transDeeData4FormData(FormBean formBean,
			FormFieldBean currentField, FormAuthViewBean favb, Long masterId,
			FormDataMasterBean cacheMasterData, Map<String, Object> resultMap,
			Long recordId) throws BusinessException {
		return transDeeData4FormData(formBean, currentField, favb, masterId, cacheMasterData, resultMap, recordId, null);
	}
	
	/**
	 * 将DEE当前数据构造到当前表单缓存数据FormDataMasterBean
	 * @param formId
	 * @param fieldName
	 * @param masterId
	 * @param contentDataId
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> transDeeData4FormData(FormBean formBean, FormFieldBean currentField, FormAuthViewBean favb, Long masterId, FormDataMasterBean cacheMasterData, Map<String,Object> resultMap, Long recordId, String detailRows) throws BusinessException{
		Set<DataContainer> datas = new LinkedHashSet<DataContainer>();
		Set<FormDataSubBean> subDatas = new LinkedHashSet<FormDataSubBean>();

		//当前字段只可能是主表。目前只支持主表字段作为dee数据交换任务控件
		InfoPath_DeeTask currentTask = currentField.getDeeTask();
		//获取dee数据
		DeeMasterDataBean ddmb = getSessionMasterDataBean(masterId);
		if(ddmb == null){
			throw new BusinessException("获取dee数据失败！");
		}
		Map<String,Object> masterMap = ddmb.getDataValue();
		//dee重复表所有数据
		Map<String,List<DeeSubDataBean>> subMaps = ddmb.getSubTables();
		//当前字段值
//		Object currentFiledValue = masterMap.get(currentTask.getRefInputName());
		Object currentFiledValue = null;
		for(Map.Entry<String,Object> entry:masterMap.entrySet()){
			if(entry == null) continue;
			if(currentTask.getRefInputName() != null && currentTask.getRefInputName().equalsIgnoreCase(entry.getKey())){
				currentFiledValue = entry.getValue();
				break;
			}
		}
		
		//验证回填数据正确性
		if(!checkTransData(currentField.getFieldType(),currentFiledValue)){
			throw new BusinessException("字段类型【"+currentField.getDisplay()+"】与回填数据不一致！");
		}
		List<FormRelation> relationList = formBean.getRelationList();
		FormDataSubBean frmSubBean = null;
		//先放入当前主表字段的值
		if(currentField.isMasterField()){
			cacheMasterData.addFieldValue(currentField.getName(), currentFiledValue);
		}else{
			frmSubBean = cacheMasterData.getFormDataSubBeanById(currentField.getOwnerTableName(), recordId);
			frmSubBean.addFieldValue(currentField.getName(), currentFiledValue);
		}
		formDataManager.calcAllWithFieldIn(formBean, currentField, cacheMasterData, recordId, resultMap, favb, true, true);
		FormAuthViewFieldBean auth = favb.getFormAuthorizationField(currentField.getName());
        Object obj = cacheMasterData.getExtraAttr(FormConstant.viewRight);
        if(obj!=null){
            auth = ((FormAuthViewBean)obj).getFormAuthorizationField(currentField.getName());
        }
		if(auth!=null){
            String fieldHtml = "";
            if(currentField.isMasterField()){
            	fieldHtml = FormFieldComBean.FormFieldComEnum.getHTML(formBean,currentField, auth, cacheMasterData);
            	resultMap.put(currentField.getName(), fieldHtml);
            }else{
            	fieldHtml = FormFieldComBean.FormFieldComEnum.getHTML(formBean,currentField, auth, frmSubBean);
            	resultMap.put(currentField.getName()+ FormConstant.DOWNLINE+recordId, fieldHtml);
            }
        }
		String viewAttr = "";
		//存储表单名称-表单字段-dee字段
		Map<String,Map<String,String>> formRelDeeMap = new LinkedHashMap<String,Map<String,String>>();
		//表单名称-对应dee数据
		Map<String,List<DeeSubDataBean>> form4DeeDataMap = new LinkedHashMap<String,List<DeeSubDataBean>>();
		//记录重复表名称集合
		List<String> slaveTableNameList = new ArrayList<String>();
		Object tempValue = null;
		for(FormRelation tempRelation : relationList){
			//数据关联dee字段，同时关联的字段是当前表单字段名称
			if(tempRelation.getToRelationAttr().equals(currentField.getName()) && tempRelation.getToRelationAttrType().intValue() == ToRelationAttrType.data_relation_dee.getKey()){
				viewAttr = tempRelation.getViewAttr();
				//数据关联的表单字段名称
				String fromFieldName = tempRelation.getFromRelationAttr();
				FormFieldBean ffb = formBean.getFieldBeanByName(fromFieldName);
				if(ffb.isMasterField()){
					//主表关联DEE主表
//					if(masterMap.containsKey(viewAttr)){
//						tempValue = masterMap.get(viewAttr);
//					}else{
//						//主表关联DEE重表
//						for(Map.Entry<String, List<DeeSubDataBean>> subEntry : subMaps.entrySet()){
//							List<DeeSubDataBean> deeSubList = subEntry.getValue();
//							//只取出重复表的第一行数据放入关联主表字段当中
//							DeeSubDataBean dsdb = deeSubList != null ? deeSubList.get(0) : null;
//							if(dsdb != null && dsdb.getDataValue().containsKey(viewAttr)){
//								tempValue = dsdb.getDataValue().get(viewAttr);
//								break;
//							}
//						}
//					}
					//是否主表字段
					boolean masterFlag = false;
					for(Map.Entry<String,Object> entry:masterMap.entrySet()){
						if(entry == null) continue;
						if(viewAttr != null && viewAttr.equalsIgnoreCase(entry.getKey())){
							tempValue = entry.getValue();
							masterFlag = true;
							break;
						}
					}
					if(!masterFlag){
						//主表关联DEE重表
						boolean isOut = false;
						for(Map.Entry<String, List<DeeSubDataBean>> subEntry : subMaps.entrySet()){
							List<DeeSubDataBean> deeSubList = subEntry.getValue();
							//只取出重复表的第一行数据放入关联主表字段当中
							DeeSubDataBean dsdb = deeSubList != null ? deeSubList.get(0) : null;
							if(dsdb != null){
								for(Map.Entry<String,Object> entry:dsdb.getDataValue().entrySet()){
									if(viewAttr != null && viewAttr.equalsIgnoreCase(entry.getKey())){
										tempValue = entry.getValue();
										isOut = true;
										break;
									}
								}
								if(isOut) break;
							}
						}
					}
					//验证回填数据正确性
					if(!checkTransData(ffb.getFieldType(),tempValue)){
						throw new BusinessException("字段类型【"+ffb.getDisplay()+"】与回填数据不一致！");
					}
					//主表字段数据添加到表单数据中
					cacheMasterData.addFieldValue(fromFieldName, tempValue);
					formDataManager.calcAllWithFieldIn(formBean, ffb, cacheMasterData, null, resultMap, favb, true, true);
					FormAuthViewFieldBean masterAuth = favb.getFormAuthorizationField(fromFieldName);
					Object tempobj = cacheMasterData.getExtraAttr(FormConstant.viewRight);
                    if(tempobj!=null){
                        masterAuth = ((FormAuthViewBean)tempobj).getFormAuthorizationField(ffb.getName());
                    }
					if(masterAuth!=null){
			            String fieldHtml = FormFieldComBean.FormFieldComEnum.getHTML(formBean,ffb, masterAuth, cacheMasterData);
			            resultMap.put(ffb.getName(), fieldHtml);
			        }
				}else{
					//表单重复表字段关联重复表字段Dee任务
					if(!currentField.isMasterField()){
						//首先判断该重复表是否在同一个重复表，在就直接recordId找到单元格，添加值，如果不在，新增行。
						//是否主表字段
//						tempValue = masterMap.get(viewAttr);
						for(Map.Entry<String,Object> entry:masterMap.entrySet()){
							if(entry == null) continue;
							if(viewAttr != null && viewAttr.equalsIgnoreCase(entry.getKey())){
								tempValue = entry.getValue();
								break;
							}
						}
						if(ffb.getOwnerTableName().equals(currentField.getOwnerTableName())){
							frmSubBean = cacheMasterData.getFormDataSubBeanById(ffb.getOwnerTableName(), recordId);
							//验证回填数据正确性
							if(!checkTransData(ffb.getFieldType(),tempValue)){
								throw new BusinessException("字段类型【"+ffb.getDisplay()+"】与回填数据不一致！");
							}
							frmSubBean.addFieldValue(ffb.getName(), tempValue);
							formDataManager.calcAllWithFieldIn(formBean, ffb, cacheMasterData, frmSubBean.getId(), resultMap, favb, true, true);
							//获取当前字段权限
							FormAuthViewFieldBean subAuth = favb.getFormAuthorizationField(ffb.getName());
							if(subAuth != null){
								String fieldHtml = FormFieldComBean.FormFieldComEnum.getHTML(formBean,ffb, subAuth, frmSubBean);
								resultMap.put(ffb.getName()+ FormConstant.DOWNLINE+recordId, fieldHtml);
							}
						}
					}else{//重复表字段关联主表DEE任务
						//重复表字段--根据当前dee的数据有多少行就在表单重复表行中新增多少行
						//这里面先将所有的dee重复表和主表给关联起来，放入Map。统一进行合并数据
						if(formRelDeeMap.containsKey(ffb.getOwnerTableName())){
							Map<String,String> formField2DeeField = formRelDeeMap.get(ffb.getOwnerTableName());
							formField2DeeField.put(fromFieldName, viewAttr);
						}else{
							Map<String,String> formField2DeeField = new HashMap<String,String>();
							formField2DeeField.put(fromFieldName, viewAttr);
							formRelDeeMap.put(ffb.getOwnerTableName(), formField2DeeField);
							slaveTableNameList.add(ffb.getOwnerTableName());
						}
						
						//回填数据的索引
						Map<String,String> selectIndex = new HashMap<String, String>();
						String[] ids = null;
						if(detailRows!=null && !"".equals(detailRows)){
							if(detailRows.indexOf(",")!=-1){
								ids = detailRows.split(",");
								
							}else{
								ids = new String[]{detailRows};
								 
							}
						}
						
						//将dee数据存入Map<表单名称，dee数据>
						if(!form4DeeDataMap.containsKey(ffb.getOwnerTableName())){
							for(Map.Entry<String, List<DeeSubDataBean>> deeSubDataMap : subMaps.entrySet()){
								List<DeeSubDataBean> deeSubDataList = deeSubDataMap.getValue();
								List<DeeSubDataBean> deeList = new ArrayList<DeeSubDataBean>();
								DeeSubDataBean currentDeeSubBean = deeSubDataList.get(0);
								String tableName = deeSubDataMap.getKey();
//								if(currentDeeSubBean.getDataValue().containsKey(viewAttr)){
//									form4DeeDataMap.put(ffb.getOwnerTableName(), deeSubDataList);
//								}
								//dee子表数据回填
								for(Map.Entry<String,Object> entry:currentDeeSubBean.getDataValue().entrySet()){
									if(entry == null) continue;
									if(viewAttr != null && viewAttr.equalsIgnoreCase(entry.getKey())){
										//对选中的数据进行过滤，去掉未选择的子表数据
										if(ids!=null && ids.length>0){
											for(String selectId:ids){
												if(selectId.indexOf("-")!=-1){
													String[] sids = selectId.split("-");
													if(tableName.equals(sids[0])){
														deeList.add(deeSubDataList.get(Integer.parseInt(sids[1])));
													}
												}
											}
										}
										 
//										if(deeList.size() > 0)
										form4DeeDataMap.put(ffb.getOwnerTableName(), deeList);
//										if(detailRows!=null && !"".equals(detailRows)){
//											if(detailRows.indexOf(",")!=-1){
//												String[] ids = detailRows.split(",");
//												for(int i=0;i<ids.length;i++){
//													if(ids[i]!=null && !"".equals(ids[i])){
//														String[] values = ids[i].split("-");
//														if(Integer.parseInt(values[1]) < deeSubDataList.size())
//															deeList.add(deeSubDataList.get(Integer.parseInt(values[1])));
//													}
//												}
//											}else{
//												String[] values = detailRows.split("-");
//												deeList.add(deeSubDataList.get(Integer.parseInt(values[1])));
//											}
//											form4DeeDataMap.put(ffb.getOwnerTableName(), deeList);
//										}
//										form4DeeDataMap.put(ffb.getOwnerTableName(), deeSubDataList);
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		if(!currentField.isMasterField()) return resultMap;
		FormDataSubBean subLine = null;
		List<FormDataSubBean> currentSubList = null;
		//处理重复表中只有关联dee主表数据没有字段关联dee重复表数据
		for(int i=0;i < slaveTableNameList.size();i++){
			String salveTableName = slaveTableNameList.get(i);
			//如果说map中没有该表名称，那就有可能是只有关联了dee主表数据
			if(!form4DeeDataMap.containsKey(salveTableName)){
				Map<String,String> currentField2Dee = formRelDeeMap.get(salveTableName);
				FormTableBean fromFormTableBean = formBean.getTableByTableName(salveTableName);
				//判断当前是否已经有一行空行
				currentSubList = cacheMasterData.getSubData(salveTableName);
				if(currentSubList != null && currentSubList.size() == 1 && currentSubList.get(0).isEmpty()){
					subLine = currentSubList.get(0);
				}else{
					subLine = new FormDataSubBean(favb,fromFormTableBean,cacheMasterData);
					cacheMasterData.addSubData(salveTableName, subLine);
				}
				for(Map.Entry<String, String> tempMap : currentField2Dee.entrySet()){
					String currentSubFieldName = tempMap.getKey();
					FormFieldBean currentSubFieldBean = formBean.getFieldBeanByName(currentSubFieldName);
					//验证回填数据正确性
					Object tempVal = null;
					for(Map.Entry<String,Object> entry:masterMap.entrySet()){
						if(entry == null) continue;
						if(tempMap.getValue() != null && tempMap.getValue().equalsIgnoreCase(entry.getKey())){
							tempVal = entry.getValue();
							break;
						}
					}
					if(!checkTransData(currentSubFieldBean.getFieldType(),tempVal)){
						throw new BusinessException("字段类型【"+currentSubFieldBean.getDisplay()+"】与回填数据不一致！");
					}
					subLine.addFieldValue(currentSubFieldName, tempVal);
					formDataManager.calcAllWithFieldIn(formBean, currentSubFieldBean, cacheMasterData, subLine.getId(), resultMap, favb, true, true);
					//增加行不用如下处理
//					FormAuthViewFieldBean subAuth = favb.getFormAuthorizationField(currentSubFieldName);
//					if(subAuth!=null){
//			            String fieldHtml = FormFieldComBean.FormFieldComEnum.getHTML(formBean,currentSubFieldBean, subAuth, subLine);
//			            resultMap.put(currentSubFieldName, fieldHtml);
//			        }
				}
				subDatas.add(subLine);
			}
		}
		//处理重复表中有关联dee重复表的情况（包含关联一个重复表和主表的情况,这里排除夸表）
		for(Map.Entry<String,List<DeeSubDataBean>> formToDeeData : form4DeeDataMap.entrySet()){
			String currentTableName = formToDeeData.getKey();
			Map<String,String> currentField2Dee = formRelDeeMap.get(currentTableName);
			FormTableBean fromFormTableBean = formBean.getTableByTableName(currentTableName);
			List<DeeSubDataBean> currentDeeDataList = formToDeeData.getValue();
			for(DeeSubDataBean deeSubData : currentDeeDataList){
				//判断当前是否已经有一行空行
				currentSubList = cacheMasterData.getSubData(currentTableName);
				if(currentSubList != null && currentSubList.size() == 1 && currentSubList.get(0).isEmpty()){
					subLine = currentSubList.get(0);
				}else{
					subLine = new FormDataSubBean(favb,fromFormTableBean,cacheMasterData);
					cacheMasterData.addSubData(currentTableName, subLine);
				}
				Object tempSubFieldValue = null;
				for(Map.Entry<String, String> tempMap : currentField2Dee.entrySet()){
					String currentSubFieldName = tempMap.getKey();
					String currentSubDeeName = tempMap.getValue();
					FormFieldBean currentSubFieldBean = formBean.getFieldBeanByName(currentSubFieldName);
					//是否主表字段
//					if(masterMap.containsKey(tempMap.getValue())){
//						//重复表字段关联dee主表字段
//						tempSubFieldValue = masterMap.get(currentSubDeeName);
//					}else{
//						//重复表字段关联dee重复表字段
//						Map<String,Object> slaveSubData = deeSubData.getDataValue();
//						tempSubFieldValue = slaveSubData.get(currentSubDeeName);
//					}
					boolean masterFlag = false;
					for(Map.Entry<String,Object> entry:masterMap.entrySet()){
						if(entry == null) continue;
						if(currentSubDeeName != null && currentSubDeeName.equalsIgnoreCase(entry.getKey())){
							tempSubFieldValue = entry.getValue();
							masterFlag = true;
							break;
						}
					}
					if(!masterFlag){
						//重复表字段关联dee重复表字段
//						Map<String,Object> slaveSubData = deeSubData.getDataValue();
//						tempSubFieldValue = slaveSubData.get(currentSubDeeName);
						for(Map.Entry<String,Object> entry:deeSubData.getDataValue().entrySet()){
							if(entry == null) continue;
							if(currentSubDeeName != null && currentSubDeeName.equalsIgnoreCase(entry.getKey())){
								tempSubFieldValue = entry.getValue();
								break;
							}
						}
					}
					//验证回填数据正确性
					if(!checkTransData(currentSubFieldBean.getFieldType(),tempSubFieldValue)){
						throw new BusinessException("字段类型【"+currentSubFieldBean.getDisplay()+"】与回填数据不一致！");
					}
					subLine.addFieldValue(currentSubFieldName, tempSubFieldValue);
					formDataManager.calcAllWithFieldIn(formBean, currentSubFieldBean, cacheMasterData, subLine.getId(), resultMap, favb, true, true);
					//增加行不用如下处理
//					FormAuthViewFieldBean subAuth = favb.getFormAuthorizationField(currentSubFieldName);
//					if(subAuth!=null){
//			            String fieldHtml = FormFieldComBean.FormFieldComEnum.getHTML(formBean,currentSubFieldBean, subAuth, subLine);
//			            resultMap.put(currentSubFieldName, fieldHtml);
//			        }
				}		
				subDatas.add(subLine);
			}
		}
		//如果dee的从表回填数据为null，那就不覆盖resultMap的datas，以防止将关联表单类型的操作的数据删除
		if (subDatas != null && !subDatas.isEmpty()) {
			datas.addAll(formDataManager.getSubDataLineContainer(formBean, favb, cacheMasterData, subDatas,resultMap));
			List<DataContainer> tempList = new ArrayList<DataContainer>();
			Iterator<DataContainer> it = datas.iterator();
	        while(it.hasNext()){
	            tempList.add(tempList.size(), it.next());
	        }
	        resultMap.put("datas", tempList);
		} 
		return resultMap;
	}
	
	/**
	 * 根据表单数据缓存获取deeMasterIds
	 */
	public void removeDeeDataByFormDataId(Long formMasterId){
		FormDataMasterBean cacheMasterData = formManager.getSessioMasterDataBean(formMasterId);
		if(cacheMasterData != null){
			String deeMasterIds = cacheMasterData.getExtraAttr("deeMasterIds") == null ? null : cacheMasterData.getExtraAttr("deeMasterIds").toString();
			if(deeMasterIds != null && !"".equals(deeMasterIds)) removeCurrentDeeCache(deeMasterIds);
		}
	}
	
	/**
	 * 根据传入deemasterIds移除缓存
	 * @param masterIds
	 */
	public void removeCurrentDeeCache(String masterIds){
		if(masterIds != null && !"".equals(masterIds)){
			String[] masterIdArray = masterIds.split("@deeId@");
	        for(int i = 0;i < masterIdArray.length;i++){
	        	Long removeId = masterIdArray[i] != null && !"".equals(masterIdArray[i]) ?
	        			Long.parseLong(masterIdArray[i]) : 0l;
	        	removeSessionMasterDataBean(removeId);
	        }
		}
	}

    /**
     * 往Session中存放dee缓存数据DeeMasterDataBean
     * 
     */
    public void putSessionMasterDataBean(DeeMasterDataBean cacheMasterData){
        AppContext.putSessionContext(createSessionMasterDataKey(cacheMasterData.getId()), cacheMasterData);
    }

    /**
     * 从Session中获取DEE数据缓存
     * @param formMasterId
     * @return
     */
    public DeeMasterDataBean getSessionMasterDataBean(Long deeMasterId){
        try {
            return (DeeMasterDataBean) AppContext.getSessionContext(createSessionMasterDataKey(deeMasterId));
        } catch (Exception e) {
            return null;
        }
    }
    
	/**
     * 往Session中存放dee缓存数据FlipInfo
     * 
     */
    public void putSessionFlipInfo(Map fData){
        AppContext.putSessionContext(createSessionMasterDataKey(20150522L), fData);
    }

    /**
     * 从Session中获取DEE数据缓存FlipInfo
     * @param 20150522
     * @return
     */
    public Map getSessionFlipInfo(){
        try {
        	Object fInfo = AppContext.getSessionContext(createSessionMasterDataKey(20150522L));
            return (Map) fInfo;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 从Session中移除DEE数据缓存
     * @param 20150522
     */
    public void revSessionFlipInfo(){
        AppContext.removeSessionArrribute(createSessionMasterDataKey(20150522L));
    }

    /**
     * 从Session中移除DEE数据缓存
     * @param formMasterId
     */
    public void removeSessionMasterDataBean(Long deeMasterId){
        AppContext.removeThreadContext(createSessionMasterDataKey(deeMasterId));
    }

    /**
     * 产生Session中存储FormDataMasterBean的key
     * @param formMasterId
     * @return
     */
    private String createSessionMasterDataKey(Long deeMasterId){
        return AppContext.currentUserId() + FormConstant.DOWNLINE + deeMasterId;
    }

    @Override
    public void transFormReport2Dee(Report report) throws Exception {
        DataSet ds=report.getDataSet(); // 数据域
        com.seeyon.v3x.dee.Document document = TransformFactory.getInstance().newDocument("root"); // 创建根节点
        //得到表单对应数据库表名称
        String table = report.getReportCfg().getQuerySourceCfg().getTableListCfg().getTableList().get(0).getCode();
        com.seeyon.v3x.dee.Document.Element tableElement = document.createElement(table); // 创建表名称节点
        List<List<DataObject>> data = ds.getDataObjectList(); // 得到数据
        List<ColumnCfg> columns = new ArrayList<ColumnCfg>(); // 用户统计设置的列数组
        columns.addAll(report.getReportCfg().getReportHeadCfg().getHeadList()); // 分组项
        columns.addAll(report.getReportCfg().getShowDataList()); // 统计项
        Boolean isAddRow = Boolean.TRUE;
        for(int i=0;i<data.size();i++){ //遍历数据，创建row节点
            com.seeyon.v3x.dee.Document.Element rowElement = document.createElement("row");
            for(int j=0;j<data.get(0).size();j++){ // 在row 节点之下新建列节点
                DataObject obj =data.get(i).get(j);
                if(("add").equals(obj.getType().toLowerCase())){ // 行汇总不传过去。dee识别不了
                    isAddRow = Boolean.FALSE;
                    break;
                }
                ColumnCfg column = columns.get(j); //得到当前遍历列
                String title = column.getCode(); // 默然传给dee的列名为表字段名，但是如果不存在，（列计算）则直接传统计对应的列头
                if(("column").equals(column.getType()) || ("change").equals(column.getCalcType())){ // 列计算 && 枚举变化次数
                    title = obj.getTitle(); // 将列头传给DEE
                }
                com.seeyon.v3x.dee.Document.Element cellElement = document.createElement(title);
                cellElement.setValue(obj.getValue()); //给列节点设值
                rowElement.addChild(cellElement); //把列节点存入行节点下
            }
            if(isAddRow){
                tableElement.addChild(rowElement); //把行节点存入表节点下
            }
            isAddRow = Boolean.TRUE;
        }
        tableElement.setAttribute("totalCount", tableElement.getChildren().size()); //给表节点设值行数。总行数
        document.getRootElement().addChild(tableElement); //把表节点添加到根节点下
        DEEClient dee = new DEEClient(); //创建DEE客户端
        dee.execute(report.getReportCfg().getDeeId(), document, new Parameters()); //执行DEE任务
    }

    public void setDeeDataManager(DeeDataManager deeDataManager) {
        this.deeDataManager = deeDataManager;
    }

    public void setFormDataManager(FormDataManager formDataManager) {
        this.formDataManager = formDataManager;
    }

    public void setFormCacheManager(FormCacheManager formCacheManager) {
        this.formCacheManager = formCacheManager;
    }

    public void setFormManager(FormManager formManager) {
        this.formManager = formManager;
    }
    
    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
}