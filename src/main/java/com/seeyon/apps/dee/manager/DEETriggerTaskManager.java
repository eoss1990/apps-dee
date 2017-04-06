package com.seeyon.apps.dee.manager;

import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormDataSubBean;
import com.seeyon.ctp.form.bean.FormTableBean;
import com.seeyon.ctp.form.util.StringUtils;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.datasource.XMLDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import java.util.*;

public class DEETriggerTaskManager extends Thread {
    
    /**
     * 
     */
    public DEETriggerTaskManager() {
        // TODO Auto-generated constructor stub
    }
	private static Log log = LogFactory.getLog(DEETriggerTaskManager.class);
	// private static RuntimeCharset fCurrentCharSet =
	// SeeyonForm_Runtime.getInstance().getCharset();
	// SeeyonFormPojo fPojo ;
	// InfoPath_DataSource fDatasource;
	// String fMasterTableName;
	/** 这个TYPE表示提供的公共方法返回的MAP 有个键为$type$表示的是表单数据在数据库中保存的表名称 需要处理下 */
	private final String type = "$type$";
	Map<String, List<Map<String, String>>> formData;
	FormBean formBean;
	String deeTaskId;
	FormDataMasterBean masterData;
	Map<String, String> formFlow_Data;

	public DEETriggerTaskManager(FormBean formBean, FormDataMasterBean masterData, 
			String deeTaskId, Map<String, String> formFlow_Data) {
		this.formBean = formBean;
		this.masterData = masterData;
		this.deeTaskId = deeTaskId;
		this.formFlow_Data = formFlow_Data;
	}

	StringBuffer xmlsb;

	@Override
	public void run() {
		createXMLStr();
		DEEClient client = new DEEClient();
		XMLDataSource xml = new XMLDataSource(xmlsb.toString());
		try {
			Parameters params = new Parameters();
			params.add("masterId", masterData.getId());
			client.execute(deeTaskId, xml.parse(), params);
		} catch (TransformException e) {
			log.error("执行DEE触发转换任务出错", e);
			// e.printStackTrace();
		} catch (DocumentException e) {
			log.error("Dee触发任务，XML格式转换出错", e);
			// e.printStackTrace();
		} catch (Exception e) {
			log.error("Dee触发任务，获取表单数据出错", e);
		}
	}

	/**
	 * 顺序执行任务
	 */
	public void runOrder() {
		createXMLStr();
		DEEClient client = new DEEClient();
		XMLDataSource xml = new XMLDataSource(xmlsb.toString());
		try {
			Parameters params = new Parameters();
			params.add("masterId", masterData.getId());
			params.add("formFlow_Data", formFlow_Data);
			client.execute(deeTaskId, xml.parse(), params);
		} catch (TransformException e) {
			log.error("执行DEE触发转换任务出错", e);
			// e.printStackTrace();
		} catch (DocumentException e) {
			log.error("Dee触发任务，XML格式转换出错", e);
			// e.printStackTrace();
		} catch (Exception e) {
			log.error("Dee触发任务，获取表单数据出错", e);
		}
	}
	/**
	 * 构建DEE需要的固定格式的XML
	 */
	private void createXMLStr() {
		xmlsb = new StringBuffer();
		xmlsb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n");
		xmlsb.append("<root> \r\n");
		Map<String, List<String>> mapSubTable = new HashMap<String, List<String>>();
		if (masterData != null) {
			// 处理主表
			StringBuffer rowString = new StringBuffer();
			FormTableBean masterTable = formBean.getMasterTableBean();
			Map<String, Object> mainData = masterData.getRowData();
			rowString.append(StringUtils.space(4) + "<row> \r\n");
			for (String key : mainData.keySet()) {
				String value = "";
				if (mainData.get(key) != null) {
					value = mainData.get(key).toString();
				}
				rowString.append(StringUtils.space(6) + "<" + key + ">" + StringUtils.Java2XMLStr(value) + "</" + key + "> \r\n");
			}
			rowString.append(StringUtils.space(4) + "</row> \r\n");
			xmlsb.append(StringUtils.space(2) + "<"	+ masterTable.getTableName() + "> \r\n");
			xmlsb.append(rowString);
			xmlsb.append(StringUtils.space(2) + "</"+ masterTable.getTableName() + "> \r\n");
			rowString.setLength(0);
			
			// 处理重复表
			
			Map<String, List<FormDataSubBean>> subDataBeans = masterData.getSubTables();
			Iterator<String> it = subDataBeans.keySet().iterator();
			while (it.hasNext()) {
				List<FormDataSubBean> subBeans = subDataBeans.get(it.next());
				
				for (FormDataSubBean subBean : subBeans) {
					FormTableBean subTableBean = subBean.getFormTable();
					Map<String, Object> rows = subBean.getRowData();
					for (String key : rows.keySet()) {
						String value = "";
						if(rows.get(key) != null){
							value = rows.get(key).toString();
						}
						rowString.append(StringUtils.space(6) + "<" + key + ">" + StringUtils.Java2XMLStr(value) + "</" + key + "> \r\n");
					}
					List<String> subTable = mapSubTable.get(subTableBean.getTableName());
					if (subTable == null) {
						List<String> list = new ArrayList<String>();
						list.add(rowString.toString());
						mapSubTable.put(subTableBean.getTableName(), list);
					}else{
						subTable.add(rowString.toString());
						mapSubTable.remove(subTableBean.getTableName());
						mapSubTable.put(subTableBean.getTableName(), subTable);
					}
					rowString.setLength(0);
				}
			}
		}
		//合并子表数据
		for (String key : mapSubTable.keySet()) {
			List<String> rowList = mapSubTable.get(key);
			xmlsb.append(StringUtils.space(2) + "<"	+ key + "> \r\n");
			for(String rowString: rowList){
				xmlsb.append(StringUtils.space(4) + "<row> \r\n");
				xmlsb.append(rowString);
				xmlsb.append(StringUtils.space(4) + "</row> \r\n");
			}
			xmlsb.append(StringUtils.space(2) + "</"+ key + "> \r\n");
		}
		xmlsb.append("</root> \r\n");
		log.debug(xmlsb.toString());
	}
}
