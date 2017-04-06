package com.seeyon.apps.dee.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.dee.manager.DeeSectionManager;
import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.apps.dee.po.DeeSectionProps;
import com.seeyon.apps.dee.po.DeeSectionSecurity;
import com.seeyon.apps.dee.util.FlowFormUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
import com.seeyon.ctp.report.chart.manager.AnyChartManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Document;
import com.seeyon.v3x.dee.Document.Element;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.code.model.FlowTypeBean;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;


public class DeeSectionController extends BaseController {

	private static Log log = LogFactory.getLog(DeeSectionController.class);

	private DeeSectionManager deeSectionManager;
	
	private PortletEntityPropertyManager portletEntityPropertyManager;
	
	private AnyChartManager anyChartManager;
	

	public void setPortletEntityPropertyManager(
			PortletEntityPropertyManager portletEntityPropertyManager) {
		this.portletEntityPropertyManager = portletEntityPropertyManager;
	}

	public void setDeeSectionManager(DeeSectionManager deeSectionManager) {
		this.deeSectionManager = deeSectionManager;
	}

	public ModelAndView main(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mv = new ModelAndView("plugin/deeSection/main");
		mv.addObject("reMsg", request.getParameter("reMsg"));
		return mv;
	}

	public ModelAndView list(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mv = new ModelAndView("plugin/deeSection/list");
/*		String condition = request.getParameter("condition");
		String textfield = request.getParameter("textfield");
		if(Strings.isNotBlank(textfield)){
			mv.addObject("sectionName", textfield);
		}
		List<DeeSectionDefine> deeSections = deeSectionManager.findAllDeeSection(textfield);
			
		Map<Long, List<DeeSectionSecurity>> deeSectionSecurityMap = new HashMap<Long, List<DeeSectionSecurity>>();
		if(deeSections != null){
			for(DeeSectionDefine dee : deeSections){
				List<DeeSectionSecurity> sectionSecurities = this.deeSectionManager.getSectionSecurity(dee.getId());
				deeSectionSecurityMap.put(dee.getId(), sectionSecurities);
			}
		}
		mv.addObject("securityMap", deeSectionSecurityMap);
		mv.addObject("list", deeSections);*/

/*		Map param = new HashMap();
        FlipInfo fi = deeSectionManager.deeSectionList(new FlipInfo(), param);
        request.setAttribute("fflistDeePortalTable", fi);*/
		return mv;
	}

	public ModelAndView create(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mv = new ModelAndView("plugin/deeSection/create");
		String idStr = request.getParameter("id");
		try{
			if (Strings.isNotBlank(idStr)) {
				long deeSectionId = Long.parseLong(idStr);
				DeeSectionDefine deeSection = this.deeSectionManager.findDeeSectionById(deeSectionId);

				List<DeeSectionSecurity> sectionSecurities = this.deeSectionManager.getSectionSecurity(deeSectionId);
				List<DeeSectionProps> props = this.deeSectionManager.getSectionProps(deeSection.getId());

				mv.addObject("sectionSecurities", sectionSecurities);

				mv.addObject("deeSection", deeSection);

				mv.addObject("deeSectionProps", props);
			}
		}catch (Exception e)
		{
			log.error(e.getStackTrace(),e);
		}

		return mv;
	}
	
	public ModelAndView save(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String reMsg = "";
		try
		{
			String[][] securities = Strings.getSelectPeopleElements(request.getParameter("selectPeopleStr"));
			
			String id = request.getParameter("id");
			String flowId = request.getParameter("flowId");
			String deeSectionName = request.getParameter("deeSectionName");
			String flowDisName = request.getParameter("flowDisName");
			String pageHeight = request.getParameter("pageHeight");
			String portalStyle = request.getParameter("portalStyle");
			String chartStyle = request.getParameter("chartStyle");
			
			if(Strings.isBlank(pageHeight)){
				pageHeight = "200";
			}
			String moduleName = DEEConfigService.MODULENAME_PORTAL;
			
			Map<String,Map<String,String>> allProps = deeSectionManager.getShowFieldMap(flowId);
			
			String[] keys = request.getParameterValues("showFieldKey");
			if(keys!=null&&keys.length>0&&allProps!=null&&!allProps.isEmpty()){
				for(String key : keys){
					if(allProps.get(key)!=null){
						String sort = request.getParameter("sort_"+key);
						allProps.get(key).put("isShow", "0");
						allProps.get(key).put("sort", sort);
					}
				}
			}
			
			DeeSectionDefine deeSection = new DeeSectionDefine();
			deeSection.setFlowId(Long.valueOf(flowId));
			deeSection.setFlowDisName(flowDisName);
			deeSection.setModuleName(moduleName);
			deeSection.setPageHeight(Integer.parseInt(pageHeight));
			deeSection.setDeeSectionName(deeSectionName);
			deeSection.setPortalStyle(portalStyle);
			deeSection.setChartStyle(chartStyle);
			
			if (Strings.isBlank(id)) {
				deeSection.setIdIfNew();
				deeSectionManager.save(deeSection, securities);
			} else {
				deeSection.setId(Long.valueOf(id));
				deeSectionManager.update(deeSection, securities);
			}
			
			deeSectionManager.saveSectionProps(deeSection.getId(), allProps);
	/*		response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.println("alert('"
					+ ResourceUtil.getString("system.manager.ok") + "')");
			out.println("</script>");
			out.flush();*/
			reMsg = "true";
		}catch(Exception e)
		{
			reMsg = "false";
			log.error(e.getMessage(),e);
		}
		return super.redirectModelAndView("/deeSectionController.do?method=main&&reMsg="+reMsg,"parent");
	}
	
//	public ModelAndView selectDataSource(HttpServletRequest request,
//			HttpServletResponse response) throws Exception {
//		ModelAndView mv = new ModelAndView("plugin/deeSection/treeDataSource");
//		DEEConfigService deeService = DEEConfigService.getInstance();
//		List<FlowTypeBean> flowTypeList = deeService.getFlowTypeList();
//		List<FlowTypeBean> flowList = new ArrayList<FlowTypeBean>();
//		if(CollectionUtils.isNotEmpty(flowTypeList)){
//			for (FlowTypeBean bean : flowTypeList) {
//				if(Strings.isNotBlank(bean.getPARENT_ID())){
//					flowList.add(bean);
//				}
//			}
//		}
//		mv.addObject("flowTypeList", flowList);
//		return mv;
//	}
	
	public ModelAndView getDataSourceFrame(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("plugin/deeSection/selectTask/taskDEEIndex");
		mav.addObject("taskType", DEEConfigService.MODULENAME_PORTAL);
		return mav;
	}
	
	public ModelAndView taskTree(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("plugin/deeSection/selectTask/taskTree");
		List<FlowTypeBean> flowTypeList = DEEConfigService.getInstance().getFlowTypeList();
		if(flowTypeList==null){
			flowTypeList = new ArrayList<FlowTypeBean>();
		}
		mav.addObject("typeList", flowTypeList);
		return mav;
	}
	
	public ModelAndView taskList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("plugin/deeSection/selectTask/taskList");
		String flowType = request.getParameter("type_id");
		String flowName = request.getParameter("flowName");
		if(!StringUtils.isBlank(flowType)){
			if("-1".equals(flowType)){
				flowType = null;
			}
		}
		if(flowName!=null){
			flowName = Functions.urlDecoder(flowName);
		}
		List<FlowBean> flowList = new ArrayList<FlowBean>();//
		int pageNumber = Strings.isBlank(request.getParameter("page")) ? 1 : Integer.parseInt(request.getParameter("page"));
		Map<String,Object> listObj = DEEConfigService.getInstance().getFlowList(flowType, null, flowName, pageNumber, Pagination.getMaxResults());
		if(listObj!=null){
			flowList = (List<FlowBean>)listObj.get(DEEConfigService.MAP_KEY_RESULT);
			int rowCount = Integer.parseInt(listObj.get(DEEConfigService.MAP_KEY_TOTALCOUNT).toString());
			Pagination.setRowCount(rowCount);
		}
		mav.addObject("flowList", flowList);
		return mav;
	}
//	public ModelAndView getFlowList(HttpServletRequest request,
//			HttpServletResponse response) throws Exception {
//		ModelAndView mv = new ModelAndView("plugin/deeSection/flowList");
//		
//		String pageSize = request.getParameter("pageSize");
//		String page = request.getParameter("page");
//		if(pageSize==null){
//			pageSize = "20";
//		}
//		if(page==null){
//			page = "1";
//		}
//		mv.addObject("pageSize", pageSize);
//		mv.addObject("page", page);
//		
//		String flowTypeId = request.getParameter("flowTypeId");
//		
//		if(Strings.isNotBlank(flowTypeId)){
//			Map<String, Object> flowMap = deeSectionManager.getFlowList(flowTypeId, DEEConfigService.MODULENAME_PORTAL, null, Integer.parseInt(page), Integer.parseInt(pageSize));
//			if(flowMap!=null){
//				Object total = flowMap.get(DEEConfigService.MAP_KEY_TOTALCOUNT);
//				Object list = flowMap.get(DEEConfigService.MAP_KEY_RESULT);
//				
//				if(total!=null){
//					Pagination.setRowCount(Integer.parseInt(String.valueOf(total)));
//				}
//				if(list!=null){
//					mv.addObject("flowList", (List<FlowBean>)list);
//				}
//			}
//		
//		}
//		return mv;
//	}
	
	public ModelAndView delete(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String reMsg = "";
		try
		{
			String id = request.getParameter("ids");
			String[] ids = id.trim().split(",");
			if(ids!=null&&ids.length>0){
					deeSectionManager.deleteDeeSection(ids);
			}
/*			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.println("alert('"
					+ ResourceUtil.getString("system.manager.ok") + "')");
			out.println("</script>");
			out.flush();*/
			reMsg = "true";
		}catch(Exception e)
		{
			reMsg = "false";
			log.error(e.getStackTrace(),e);
		}

		return super.redirectModelAndView("/deeSectionController.do?method=main&&reMsg="+reMsg,"parent");
	}
	
	 
	
	public ModelAndView showField4Portal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mv = new ModelAndView("plugin/deeSection/showField4Portal");
		String entityId = request.getParameter("entityId");
		String ordinal = request.getParameter("ordinal");
		if(Strings.isNotBlank(entityId)){
			Map<String,String> entityProps = this.portletEntityPropertyManager.getPropertys(Long.valueOf(entityId), ordinal);
			if(entityProps!=null&&!entityProps.isEmpty()){
				String singleBoardId = entityProps.get("singleBoardId");
				if(Strings.isNotBlank(singleBoardId)){
					List<DeeSectionProps> props = deeSectionManager.getSectionProps(Long.valueOf(singleBoardId));
					Map<String,String> defaultShowProps = new LinkedHashMap<String,String>();
					if(CollectionUtils.isNotEmpty(props)){
						for(DeeSectionProps p : props){
							if(p.getIsShow()==0){
								defaultShowProps.put(p.getPropName(), p.getPropValue());
							}
						}
					}
					mv.addObject("props", defaultShowProps);
				}
			}else{
				log.info("获取DEE栏目属性失败，栏目entityId为："+entityId);
			}
		}else{
			log.info("获取DEE栏目ID失败，栏目entityId为："+entityId);
		}
		return mv;
	}
	
	
	public ModelAndView showSectionData(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mv = new ModelAndView("plugin/deeSection/showSectionData");
		String sectionDefineId = request.getParameter("sectionDefineId");
		String entityId = request.getParameter("entityId");
		String ordinal = request.getParameter("ordinal");
		StringBuffer searchSb = new StringBuffer("[");
		StringBuffer fieldSb = new StringBuffer("["); 
		
		mv.addObject("sectionDefineId", sectionDefineId);
		mv.addObject("entityId", entityId);
		mv.addObject("ordinal", ordinal);
		
		if(Strings.isNotBlank(sectionDefineId)){
			List<DeeSectionProps> props = deeSectionManager.getSectionProps(Long.valueOf(sectionDefineId));
			Map<String,String> defaultShowProps = new LinkedHashMap<String,String>();
			if(CollectionUtils.isNotEmpty(props)){
				for(DeeSectionProps p : props){
					if(p.getIsShow()==0){
						defaultShowProps.put(p.getPropName(), p.getPropValue());
					}
				}
			}
			
			
			
			Map<String,String> entityProps = this.portletEntityPropertyManager.getPropertys(Long.valueOf(entityId), ordinal);
			Map<String,String> showProps = new LinkedHashMap<String,String>();
			
			
			if(entityProps!=null&&!entityProps.isEmpty()){
				String rowList = entityProps.get("rowList");
				String sectionName = entityProps.get("columnsName");
				mv.addObject("sectionName", sectionName);
				if(Strings.isNotBlank(rowList)&&rowList.equals("showField")){
					String showFields = entityProps.get("showField_value");
					if(Strings.isNotBlank(showFields)){
						String[] fields = showFields.split(",");
						for(DeeSectionProps prop : props){
							for(int i=0; i<fields.length; i++){
								if(fields[i].equals(prop.getPropName())){
									showProps.put(prop.getPropName(), prop.getPropValue());
								}
							}
						}
					}
				}else{
					showProps = defaultShowProps;
				}
			}
			
			Set<String> keys = null;
			
			keys = showProps.keySet();
			for(String key : keys)
			{
				searchSb.append("{id:'").append(key).append("',");
				searchSb.append("name:'").append(key).append("',");
				searchSb.append("type:'input',");
				searchSb.append("text:'").append(showProps.get(key)).append("',");
				searchSb.append("value:'").append(key).append("'},");
				
				fieldSb.append("{display:'").append(showProps.get(key)).append("',");
				fieldSb.append("name:'").append(key).append("',");
				fieldSb.append("width:'200'},");
				
			}
			String searchCd = searchSb.toString();
			searchCd = searchCd.substring(0, searchCd.length()-1)+"]";
			String fieldCd = fieldSb.toString();
			fieldCd = fieldCd.substring(0, fieldCd.length()-1)+"]";
			
			mv.addObject("searchCd", searchCd);
			mv.addObject("fieldCd", fieldCd);
		}
		return mv;
	}
	
/*	private boolean notDateField(String fieldType){
		String type = fieldType.toLowerCase();
		if(type.indexOf("time")!=-1||type.indexOf("date")!=-1||type.indexOf("timestamp")!=-1){
			return false;
		}else{
			return true;
		}
	}*/
	
	public ModelAndView chartPortal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mv = new ModelAndView("plugin/deeSection/chartPortal");
		String sectionDefineId = request.getParameter("sectionDefineId");
		
		List<DeeSectionProps> props = deeSectionManager.getSectionProps(Long.valueOf(sectionDefineId));
		
		Map<String,String> defaultShowProps = new LinkedHashMap<String,String>();
		if(CollectionUtils.isNotEmpty(props)){
			for(DeeSectionProps p : props){
				if(p.getIsShow()==0){
					defaultShowProps.put(p.getPropName(), p.getPropValue());
				}
			}
		}
		
		Map<String,String> showProps = new LinkedHashMap<String,String>();
		
		String rowList = request.getParameter("rowList");
		String size = request.getParameter("count");
		if(StringUtils.isBlank(size)||size.trim().equalsIgnoreCase("null")){
			size = "7";
		}
		if(Strings.isNotBlank(rowList)&&rowList.equals("showField")){
			String showFields = request.getParameter("showField_value");
			if(Strings.isNotBlank(showFields)){
				String[] fields = showFields.split(",");
				Set<String> keys = defaultShowProps.keySet();
				for(String key : keys){
					for(int i=0; i<fields.length; i++){
						if(fields[i].equals(key)){
							showProps.put(key, defaultShowProps.get(key));
						}
					}
				}
			}
		}
		
		Set<String> keys = null;
		if(showProps!=null&&!showProps.isEmpty()){
			keys = showProps.keySet();
		}else{
			keys = defaultShowProps.keySet();
		}
		
		DeeSectionDefine deeSectionDefine = deeSectionManager.findDeeSectionById(Long.valueOf(sectionDefineId));
		
		List<Map<String,String>> data = new ArrayList<Map<String,String>>();
		DEEClient client = new DEEClient();
		try {
			Parameters param = new Parameters();
			param.add("Paging_pageSize", Integer.valueOf(size));
			param.add("Paging_pageNumber", Integer.valueOf(1));
			param.add("whereString", " where 1=1");
			Map<String, String> formFlow_Data = FlowFormUtil.getFlowFormData(
					deeSectionDefine.getDeeSectionName(), "", "portal栏目");
			param.add("formFlow_Data", formFlow_Data);
			Document document = client.execute(String.valueOf(deeSectionDefine.getFlowId()),param);
			Element root =  document.getRootElement();
			List<Element> list = root.getChildren();
			if(CollectionUtils.isNotEmpty(list)){
				for(Element t : list){
					
					List<Element> rows = t.getChildren();
					if(CollectionUtils.isNotEmpty(rows)){
						for(Element row : rows){
							if(props!=null&&!props.isEmpty()){
								Map<String,String> rowMap = new LinkedHashMap<String,String>();
								
								for(String key : keys){
									Element e = row.getChild(key);
									if(e==null)
									{
										e=row.getChild(key.toUpperCase());
									}
									if(e==null)
									{
										e=row.getChild(key.toLowerCase());
									}
									if(e!=null){
										if(e.getValue()!=null){
											rowMap.put(key, e.getValue().toString());
										}else{
											rowMap.put(key, "");
										}
									}else{
										rowMap.put(key, "");
									}
								}
								data.add(rowMap);
							}
						}
					}
				}
			}
		} catch (InvocationTargetException ee )
		{
			log.error("DEE栏目执行引擎查询出错："+ee.getTargetException().getMessage(),ee);
			return  mv;
		}
		catch (Exception e) {
			log.error("DEE栏目执行引擎查询出错："+e.getMessage(),e);
			return mv;
		}
		
		//后台组装图表数据
		ArrayList xAxis = new ArrayList();
		ArrayList yAxis = new ArrayList();
		String chartType = "";
		if(deeSectionDefine.getPortalStyle().equals("1"))     //如果栏目样式设置为列表+图
		{
			if(deeSectionDefine.getChartStyle().equals("1"))
			{
				chartType = "bar"; //柱状图
			}else if(deeSectionDefine.getChartStyle().equals("0"))
			{
				chartType = "line"; //折线图
			}

			for(String key:keys)
			{
				xAxis.add("'"+defaultShowProps.get(key)+"'");
				yAxis.add(data.get(0).get(key));
			}
		}

		mv.addObject("deeColumnSize", keys.size());
		mv.addObject("xAxis", xAxis);
		mv.addObject("yAxis", yAxis);
		mv.addObject("chartType", chartType);
		mv.addObject("deeData", data);
		mv.addObject("fieldKeys", keys);
		mv.addObject("defaultShowProps",defaultShowProps);
		return mv;
	}

	public AnyChartManager getAnyChartManager() {
		return anyChartManager;
	}

	public void setAnyChartManager(AnyChartManager anyChartManager) {
		this.anyChartManager = anyChartManager;
	}
	
	public static void main(String[] args)
	{
		Map map = new HashMap();
	}
	
}
