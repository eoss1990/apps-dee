package com.seeyon.apps.dee.util;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.AppContext;

public class FlowFormUtil {
	public static Map<String, String> getFlowFormData(CollaborationApi collaborationApi, Long formRecordId, String action) throws Exception{
		Map<String, String> formFlow_Data = new HashMap<String, String>();
		ColSummary col = collaborationApi.getColSummaryByFormRecordId(formRecordId);
		if (col != null) {
			formFlow_Data.put("name", col.getSubject());
			formFlow_Data.put("id", col.getId().toString());
		}
		String person = AppContext.getCurrentUser().getName();
		formFlow_Data.put("person", person);
		formFlow_Data.put("action", action);
		return formFlow_Data;
	}
	public static String getFlowFormDataString(CollaborationApi collaborationApi, Long formRecordId, String action) throws Exception{
		StringBuffer formFlow_Data = new StringBuffer("{");
		ColSummary col = collaborationApi.getColSummaryByFormRecordId(formRecordId);
		if (col != null) {
			formFlow_Data.append("\"name\"" + "=\"" + col.getSubject() + "\",");
			formFlow_Data.append(" \"id\"" + "=\"" + col.getId().toString() + "\",");
		}
		String person = AppContext.getCurrentUser().getName();
		formFlow_Data.append(" \"person\"" + "=\"" + person + "\",");
		formFlow_Data.append(" \"action\"" + "=\"" + action + "\"");
		formFlow_Data.append("}");
		return formFlow_Data.toString();
	}
	public static Map<String, String> getFlowFormData(String name, String id, String action) throws Exception{
		Map<String, String> formFlow_Data = new HashMap<String, String>();
		formFlow_Data.put("name", name);
		formFlow_Data.put("id", id);
		String person = AppContext.getCurrentUser().getName();
		formFlow_Data.put("person", person);
		formFlow_Data.put("action", action);
		return formFlow_Data;
	}
}
