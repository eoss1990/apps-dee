package com.seeyon.apps.dee.manager;

import com.seeyon.apps.dee.util.Constants;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;
import com.seeyon.ctp.form.dee.bean.InfoPath_DeeField;
import com.seeyon.ctp.form.dee.bean.InfoPath_DeeParam;
import com.seeyon.ctp.form.dee.bean.InfoPath_DeeTask;
import com.seeyon.ctp.form.dee.design.DeeDesignManager;
import com.seeyon.ctp.form.modules.engin.relation.FormRelationEnums.ToRelationAttrType;
import com.seeyon.ctp.form.po.FormRelation;
import com.seeyon.ctp.form.service.FormManager;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * dee设置业务接口实现类
 * @author dengxj
 *
 */
public class DeeDesignManagerImpl implements DeeDesignManager {
	private static final Log LOGGER        = CtpLogFactory.getLog(DeeDesignManagerImpl.class);
	
	private FormManager formManager;
	
	@Override
    public void setDeeTask4FormField(FormBean formBean) throws BusinessException{
		if(formBean!=null){
			List<FormFieldBean> fieldList = formBean.getAllFieldBeans();
			if(fieldList!=null && fieldList.size()>0){
				List<FormRelation> relationList = new ArrayList<FormRelation>();
				for(FormFieldBean formFieldBean : fieldList){
					if(formFieldBean.getFormRelation()!=null && formFieldBean.getFormRelation().getToRelationAttrType()==ToRelationAttrType.data_relation_dee.getKey()){
						relationList.add(formFieldBean.getFormRelation());
					}
				}
				if(relationList!=null && relationList.size()>0){
					for(FormFieldBean formFieldBean : fieldList){
						InfoPath_DeeTask deeTask = formFieldBean.getDeeTask();
						if(deeTask!=null){
							List<InfoPath_DeeField> deeFieldList = deeTask.getTaskFieldList();
							deeTask.initFieldList();
							if(deeFieldList!=null && deeFieldList.size()>0){
								for(FormRelation relation : relationList){
									if(relation.getToRelationAttr().equals(formFieldBean.getName())){
										for(InfoPath_DeeField field : deeFieldList){
											if(relation.getViewAttr().equals(field.getName())){
												//field.setToRelFormField(relation.getFromRelationAttr());
												/**
												 * 处理当多个数据关联关联统一字段是，只回填表单后面的单元格，前面的被覆盖了的bug
												 */
												if(field.getToRelFormField()!=null && !"".equals(field.getToRelFormField())){
													field.setToRelFormField(field.getToRelFormField()+","+relation.getFromRelationAttr());
												}else{
													field.setToRelFormField(relation.getFromRelationAttr());
												}
												break;
											}
										}
									}
								}
//								deeTask.formatFields();
							}
						}
					}
				}
			}
		}
	}
	
	@Override
    public void saveOrUpdateDeeTask(Map<String, Object> mapAll) throws BusinessException {
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) mapAll.get("dee");
        String fieldName = map.get("fieldName").toString();
        FormBean formBean = formManager.getEditingForm();
        FormFieldBean fieldBean = formBean.getFieldBeanByName(fieldName);
        String taskId = map.get("taskId").toString();
		String taskName = map.get("taskName").toString();
		String taskResult = map.get("taskResult").toString();
		//绑定字段
		String refTaskField = map.get("refTaskField").toString();
		String refFieldDisplay = map.get("refFieldDisplay").toString();
		String taskParam = map.get("paramStr").toString();
		//任务字段
		String taskField = map.get("fieldStr").toString();
		//绑定字段表
		String refFieldTable = map.get("refFieldTable").toString();
		//列表绑定字段
		String listRefField = map.get("listRefField")!=null?map.get("listRefField").toString():"";
		String isLoad = map.get("isLoad")!=null?map.get("isLoad").toString():"";
		//列表类型
		String listType = map.get("listType").toString();
		String inputType = map.get("inputType").toString();
		String treeId = "";
		String treeName = "";
		String treePid = "";
		String treeResult = "";
		if( "exchangetask".equals(inputType) && Constants.ListType_TREE.equals(listType) ){
			//分类树信息
	        Map<String, String> treeMap = (Map<String, String>) mapAll.get("rtable");
	        treeId = treeMap.get("treeId")!=null?treeMap.get("treeId").toString():"";
	        treeName = treeMap.get("treeName")!=null?treeMap.get("treeName").toString():"";
	        treePid = treeMap.get("treePid").toString()!=null?treeMap.get("treePid").toString():"";
	        treeResult = treeMap.get("treeResult")!=null?treeMap.get("treeResult").toString():"";
		}
		//主从表外键信息
		String refMasterField = map.get("refMasterField").toString();
		//从表表名信息
		String taskSubTableNames = map.get("subTableNameStr").toString();
		fieldBean.setInputType(inputType);
		if(taskId!=null && taskId.length()>0){
			InfoPath_DeeTask deeTask = new InfoPath_DeeTask();
			deeTask.setId(taskId);
			deeTask.setName(taskName);
			deeTask.setRefInputName(refTaskField);
			deeTask.setRefFieldDisplay(refFieldDisplay);
			deeTask.setRefResult(taskResult);
			deeTask.setTablename(refFieldTable);
			deeTask.setToRelMasterField(refMasterField);
			deeTask.setFormAppId(formBean.getId().toString());
			deeTask.setListType(listType);
			deeTask.setListRefField(listRefField);
			deeTask.setTreeResult(treeResult);
			deeTask.setTreeId(treeId);
			deeTask.setTreeName(treeName);
			deeTask.setTreePid(treePid);
			deeTask.setIsLoad(isLoad);
			if(FormFieldComEnum.EXTEND_EXCHANGETASK.getKey().equals(inputType)){
				deeTask.setSelectType("select");
			}else{
				deeTask.setSelectType("search");
			}
			List<InfoPath_DeeParam> taskParamList = new ArrayList<InfoPath_DeeParam>();
			if(taskParam!=null && taskParam.length()>0){
				String[] params = taskParam.split("\\|");
				if(params!=null&&params.length>0){
					for(int m=0;m<params.length;m++){
						String param = params[m];
						String[] paramAtt = param.split(",");
						InfoPath_DeeParam deeParam = new InfoPath_DeeParam();
						deeParam.setName(paramAtt[0]);
						String value = "";
						if(paramAtt.length>1){
							value = paramAtt[1];
						}
						deeParam.setValue(value);
						String display = "";
						if(paramAtt.length>2){
							display = paramAtt[2];
						}
						deeParam.setDisplay(display);
						String realValue = "";
						if(paramAtt.length>3){
							realValue = paramAtt[3];
						}
						deeParam.setRealValue(realValue);
						taskParamList.add(deeParam);
					}
				}
			}
			deeTask.setTaskParamList(taskParamList);
			List<InfoPath_DeeField> taskFieldList = new ArrayList<InfoPath_DeeField>();
			if(taskField!=null){
				String[] fields = taskField.split("\\|");
				if(fields!=null&&fields.length>0){
					for(int n=0;n<fields.length;n++){
						String field = fields[n];
						String[] fieldAtt = field.split(",");
						InfoPath_DeeField deeField = new InfoPath_DeeField();
						deeField.setName(fieldAtt[0]);
						deeField.setDisplay(fieldAtt[1]);
						deeField.setFieldtype(fieldAtt[2]);
						deeField.setFieldlength(fieldAtt[3]);
						deeField.setChecked(fieldAtt[4]);
						deeField.setIsmaster(fieldAtt[5]);
						taskFieldList.add(deeField);
					}
				}
			}
			deeTask.setTaskFieldList(taskFieldList);
			if(taskSubTableNames != null){
				String[] subTables = taskSubTableNames.split("\\|");
				for(int i=0;i<subTables.length;i++){
					String subTable = subTables[i];
					if("".equals(subTable))
						continue;
					String[] subTbAtt = subTable.split("=");
					if(subTbAtt.length > 1)
						deeTask.setSubTablesDisplay(subTbAtt[0], subTbAtt[1]);
				}
			}
			fieldBean.setDeeTask(deeTask);
			
		}
    }

    public void setFormManager(FormManager formManager) {
        this.formManager = formManager;
    }
}
