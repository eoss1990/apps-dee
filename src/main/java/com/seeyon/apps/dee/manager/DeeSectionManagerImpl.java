package  com.seeyon.apps.dee.manager;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jfree.util.Log;
import org.xml.sax.InputSource;

import com.seeyon.apps.dee.dao.DeeSectionDao;
import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.apps.dee.po.DeeSectionProps;
import com.seeyon.apps.dee.po.DeeSectionSecurity;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.cache.loader.AbstractMapDataLoader;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.portal.space.manager.PortletEntityPropertyManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Document.Attribute;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.flow.model.FlowBean;


public class DeeSectionManagerImpl implements DeeSectionManager {
	
	private static final CacheAccessable cacheFactory = CacheFactory.getInstance(DeeSectionManagerImpl.class);

//	private CacheMap<Long, DeeSectionDefine> DeeSectionDefineMap;
//	private CacheMap<Long, ArrayList<DeeSectionProps>> DeeSectionPropsMap;

	private PortletEntityPropertyManager portletEntityPropertyManager;

	private DeeSectionDao deeSectionDao;

	public void setDeeSectionDao(DeeSectionDao deeSectionDao) {
		this.deeSectionDao = deeSectionDao;
	}


	public void init(){
		/* 集群缓存
		DeeSectionDefineMap = cacheFactory.createLinkedMap("DeeSectionDefineMap");
		DeeSectionPropsMap = cacheFactory.createLinkedMap("DeeSectionPropsMap");		
		
		DeeSectionDefineMap.setDataLoader(new AbstractMapDataLoader<Long, DeeSectionDefine> (DeeSectionDefineMap) {
			@Override
			protected Map<Long, DeeSectionDefine> loadLocal() {
				Map<Long, DeeSectionDefine> result = new HashMap<Long, DeeSectionDefine>();
				List<DeeSectionDefine> ds = deeSectionDao.getAllDeeSection();
				if(CollectionUtils.isNotEmpty(ds)){
					for (DeeSectionDefine definition : ds) {
						result.put(definition.getId(), definition);
					}
				}
				return result;
			}

			@Override
			protected DeeSectionDefine loadLocal(Long k) {
				return deeSectionDao.getDeeSectinById(k);
			}
		});
		DeeSectionDefineMap.reload();
		
		DeeSectionPropsMap.setDataLoader(new AbstractMapDataLoader<Long, ArrayList<DeeSectionProps>>(DeeSectionPropsMap) {

			@Override
			protected Map<Long, ArrayList<DeeSectionProps>> loadLocal() {
				Map<Long, ArrayList<DeeSectionProps>> result = new LinkedHashMap<Long, ArrayList<DeeSectionProps>>();
				List<DeeSectionProps> ps = deeSectionDao.getAllSectionProps();
				for (DeeSectionProps props : ps) {
					ArrayList<DeeSectionProps> prop = result.get(props.getDeeSectionId());
					if(prop == null){
						prop = new ArrayList<DeeSectionProps>();
						result.put(props.getDeeSectionId(), prop);
					}
					prop.add(props);
				}
				return result;
			}

			@Override
			protected ArrayList<DeeSectionProps> loadLocal(Long k) {
				return new ArrayList<DeeSectionProps>(deeSectionDao.getPropsByDeeSectionId(k));
			}
		});
		DeeSectionPropsMap.reload();
		*/
	}

	@Override
	public void createDeeSection(DeeSectionDefine deeSection) {
		deeSection.setIdIfNew();
		deeSectionDao.saveDeeSection(deeSection);
	}

	@Override
	public void updateDeeSection(DeeSectionDefine deeSection) {
		deeSectionDao.updateDeeSection(deeSection);
	}

	@Override
	public void deleteDeeSection(String[] ids) {
		for(String id : ids){
			deeSectionDao.deleteDeeSection(Long.valueOf(id));
		}
	}

	@Override
	public List<DeeSectionDefine> findAllDeeSection() {
		return deeSectionDao.getAllDeeSection();
	}

	@Override
	public DeeSectionDefine findDeeSectionById(long id) {
		return deeSectionDao.getDeeSectinById(id);
	}

	@Override
	public List<DeeSectionProps> getSectionProps(long id) {
		List<DeeSectionProps> props = this.deeSectionDao.getPropsByDeeSectionId(id);
		return props;
	}

	@Override
	public Map<String, Object> getFlowList(String flowType, String moduleName,
			String flowName,int pageNum, int pageSize) {
		DEEConfigService deeService = DEEConfigService.getInstance();
		Map<String,Object> flowMap = deeService.getFlowList(flowType, moduleName, flowName, pageNum, pageSize);
		return flowMap;
	}
	public static void main(String[] args){
		DEEConfigService deeService = DEEConfigService.getInstance();
		Map<String,Object> flowList = deeService.getFlowList("1", DEEConfigService.MODULENAME_PORTAL, null, 1, 20);
		if(flowList!=null&&flowList.size()>0){
				System.out.println(flowList.get(DEEConfigService.MAP_KEY_TOTALCOUNT).toString());
				@SuppressWarnings("unchecked")
				List<FlowBean> list = (List<FlowBean>) flowList.get(DEEConfigService.MAP_KEY_RESULT);
				if(list!=null){
					for(FlowBean bean : list){
						System.out.println("Name:"+bean.getDIS_NAME()+"\t FLOW_ID:"+bean.getFLOW_ID()+" \t flow_desc:"+bean.getFLOW_DESC()+"\t FLOW_META:"+bean.getFLOW_META());
					}
				}
		}
	}

	@Override
	public void save(DeeSectionDefine deeSection, String[][] security) {
		List<DeeSectionSecurity> securities = new ArrayList<DeeSectionSecurity>();
		if(security != null){
			for (int i = 0; i < security.length; i++) {
				DeeSectionSecurity s = new DeeSectionSecurity();
				s.setIdIfNew();
				s.setDeeSectionId(deeSection.getId());
				s.setEntityType(security[i][0]);
				s.setEntityId(Long.parseLong(security[i][1]));
				s.setSort(i);
				securities.add(s);
			}
		}
		this.deeSectionDao.save(deeSection,securities);
	}

	@Override
	public void update(DeeSectionDefine deeSection, String[][] security) {
		List<DeeSectionSecurity> securities = new ArrayList<DeeSectionSecurity>();
		if(security != null){
			for (int i = 0; i < security.length; i++) {
				DeeSectionSecurity s = new DeeSectionSecurity();
				s.setIdIfNew();
				s.setDeeSectionId(deeSection.getId());
				s.setEntityType(security[i][0]);
				s.setEntityId(Long.parseLong(security[i][1]));
				s.setSort(i);
				securities.add(s);
			}
		}
		this.deeSectionDao.update(deeSection,securities);
	}

	@Override
	public List<DeeSectionSecurity> getSectionSecurity(long entityId) {
		return this.deeSectionDao.getSectionSecurity(entityId);
	}
	
	@Override
	public String getShowField(String flowId){
		if(flowId==null){
			return null;
		}
		StringBuffer out = new StringBuffer();
		out.append("var data = [];");
		DEEConfigService deeService = DEEConfigService.getInstance();
		String meta = deeService.getFlowMeta(flowId);
		
		if(Strings.isNotBlank(meta)){
			StringReader reader = new StringReader(meta);
			InputSource in = new InputSource(reader);
			SAXBuilder builder=new SAXBuilder();

			try {
				Document doc = builder.build(in);
				Element root = doc.getRootElement();
				List<Element> apps = root.getChildren("App");
				for (Element app : apps) {
					List<Element> tableLists = app.getChildren("TableList");
					for (Element tableList : tableLists) {
						List<Element> tables = tableList.getChildren("Table");
						for (Element table : tables) {
							List<Element> fields = table.getChildren("Field");
							for (Element field : fields) {
								if(field != null) {
									String id = field.getAttributeValue("name");
									String display = field
											.getAttributeValue("display");
									String fieldtype = field.getAttributeValue("fieldtype");
									out.append("var "+id+" = [];");
									out.append(id+"[0] = '"+id+"';");
									out.append(id+"[1] = '"+display+"';");
									out.append(id+"[2] = '"+fieldtype+"';");
									out.append(" data[data.length] = "+id+";");
								}
							}
						}
					}
				}
			} catch (JDOMException e) {
				Log.error("DEE数据级描述文件解析失败：", e);
			} catch (IOException e) {
				Log.error("DEE数据级描述文件加载失败：", e);
			}finally{
				reader.close();
			}
		}
		
		return out.toString();
	}
	@Override
	public Map<String,Map<String,String>> getShowFieldMap(String flowId){
		if(flowId==null){
			return null;
		}
		DEEConfigService deeService = DEEConfigService.getInstance();
		String meta = deeService.getFlowMeta(flowId);
		
		Map<String,Map<String,String>> allProps = new LinkedHashMap<String,Map<String,String>>();
		
		if(Strings.isNotBlank(meta)){
			StringReader reader = new StringReader(meta);
			InputSource in = new InputSource(reader);
			SAXBuilder builder=new SAXBuilder();
			try {
				Document doc = builder.build(in);
				Element root = doc.getRootElement();
				List<Element> apps = root.getChildren("App");
				for (Element app : apps) {
					List<Element> tableLists = app.getChildren("TableList");
					for (Element tableList : tableLists) {
						List<Element> tables = tableList.getChildren("Table");
						for (Element table : tables) {
							List<Element> fields = table.getChildren("Field");
							for (Element field : fields) {
								if(field != null) {
									String id = field.getAttributeValue("name");
									String display = field.getAttributeValue("display");
									String fieldtype = field.getAttributeValue("fieldtype");
									Map<String,String> map = new HashMap<String,String>();
									map.put("id", id);
									map.put("displayName",display);
									map.put("fieldType", fieldtype);
									map.put("isShow", "1");//"0":show;"1":hide
									allProps.put(id, map);
								}
							}
						}
					}
				}
				
			} catch (JDOMException e) {
				Log.error("DEE数据级描述文件解析失败：", e);
			} catch (IOException e) {
				Log.error("DEE数据级描述文件加载失败：", e);
			}
		}
		
		return allProps;
	}
	@Override
	public void saveSectionProps(long id, Map<String, Map<String,String>> props) {
		this.deeSectionDao.saveDeeSectionProps(id, props);
//		List<DeeSectionProps> list = this.deeSectionDao.getPropsByDeeSectionId(id);
//		this.DeeSectionPropsMap.put(id, (ArrayList<DeeSectionProps>) list);
	}
	@Override
	public boolean hasCurrentSectionName(String sectionName,String id) {
		if(Strings.isNotBlank(id)){
			return false;
		}else{
			List<DeeSectionDefine> list = this.deeSectionDao.getDeeSectionByName(sectionName);
			if(CollectionUtils.isNotEmpty(list)){
				return true;
			}else{
				return false;
			}
		}
	}
	@Override
	public List<DeeSectionDefine> findAllDeeSection(String sectionName) {
		return this.deeSectionDao.getAllDeeSection(sectionName);
	}
	@Override
	public List<DeeSectionDefine> getDeeSectionIdBySecurity(List<Long> entityIds) {
		if(CollectionUtils.isEmpty(entityIds)){
			return null;
		}
		List<DeeSectionSecurity> securities = deeSectionDao.getDeeSectionBySecurity(entityIds);
		List<DeeSectionDefine> deeSections = new ArrayList<DeeSectionDefine>();
		if(CollectionUtils.isNotEmpty(securities)){
			for(DeeSectionSecurity security : securities){
				Long deeSectionId = security.getDeeSectionId();
				DeeSectionDefine deeSectionDefine = this.findDeeSectionById(deeSectionId);
				if(deeSectionDefine!=null){
					deeSections.add(deeSectionDefine);
				}
			}
		}
		return deeSections;
	}
	@Override
	public FlipInfo deeSectionList(FlipInfo fi, Map<String, Object> param){
		
		String deeSectionName = "";
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            if (entry.getKey().equals("deeSectionName")) {
                Object tmp = entry.getValue();
                deeSectionName = tmp == null ? "" : tmp.toString();
            }
        }
        
        
        try {
            List<DeeSectionDefine> datas = new ArrayList<DeeSectionDefine>();
            List<DeeSectionDefine> result = findAllDeeSection();
            List<DeeSectionDefine> resultList = new ArrayList<DeeSectionDefine>();

            if(Strings.isNotBlank(deeSectionName))
            {
            	for (DeeSectionDefine db : result) {
                    if ((db.getDeeSectionName() == null ? "" : db.getDeeSectionName()).contains(deeSectionName)) {
                        resultList.add(db);
                    }
                }
            }else
            {
            	resultList=result;
            }
            
            for (int i = 0; i < resultList.size(); i++) {
                //这里进行内存分页
                if (i >= (fi.getPage() - 1) * fi.getSize() && i < fi.getPage() * fi.getSize()) {
                	List<DeeSectionSecurity> sectionSecurities = this.getSectionSecurity(resultList.get(i).getId());
                	String org = Functions.showOrgEntities(sectionSecurities, "entityId", "entityType", null);
                	resultList.get(i).setOrg(org);
                    datas.add(resultList.get(i));
                }
            }
            fi.setData(datas);
            fi.setTotal(resultList.size());
        } catch (Exception e) {
        	Log.error(e.getMessage(), e);
        }
        return fi;
	}
	
	@Override
	public FlipInfo deeSectionMoreList(FlipInfo fi, Map<String, Object> param){
		
		String sectionDefineId = "";
		String entityId="";
		String ordinal="";
		String deeSearchKey="";
		String deeSearchValue="";
		int pageSize =fi.getSize();
		int page=fi.getPage();
		
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            if (entry.getKey().equals("sectionDefineId")) {
                Object tmp = entry.getValue();
                sectionDefineId = tmp == null ? "" : tmp.toString();
            }
            else if(entry.getKey().equals("entityId")) {
                Object tmp = entry.getValue();
                entityId = tmp == null ? "" : tmp.toString();
            }
            else if(entry.getKey().equals("ordinal")) {
                Object tmp = entry.getValue();
                ordinal = tmp == null ? "" : tmp.toString();
            }
            else if(entry.getKey().equals("deeSearchKey")) {
                Object tmp = entry.getValue();
                deeSearchKey = tmp == null ? "" : tmp.toString();
            }
            else if(entry.getKey().equals("deeSearchValue")) {
                Object tmp = entry.getValue();
                deeSearchValue = tmp == null ? "" : tmp.toString();
            }
        }
        
        if(Strings.isNotBlank(sectionDefineId)){
			List<DeeSectionProps> props = this.getSectionProps(Long.valueOf(sectionDefineId));
			Map<String,String> defaultShowProps = new LinkedHashMap<String,String>();
			Map<String,String> searchProps = new LinkedHashMap<String,String>();
			if(CollectionUtils.isNotEmpty(props)){
				for(DeeSectionProps p : props){
					if(p.getIsShow()==0){
						defaultShowProps.put(p.getPropName(), p.getPropValue());
						if(notDateField(p.getPropMeta())){
							searchProps.put(p.getPropName(), p.getPropValue());
						}
					}
				}
			}
			
			
			
			Map<String,String> entityProps = this.portletEntityPropertyManager.getPropertys(Long.valueOf(entityId), ordinal);
			Map<String,String> showProps = new LinkedHashMap<String,String>();
			
			
			if(entityProps!=null&&!entityProps.isEmpty()){
				String rowList = entityProps.get("rowList");
/*				String sectionName = entityProps.get("columnsName");
				mv.addObject("sectionName", sectionName);*/
				if(Strings.isNotBlank(rowList)&&rowList.equals("showField")){
					String showFields = entityProps.get("showField_value");
					if(Strings.isNotBlank(showFields)){
						searchProps.clear();
						String[] fields = showFields.split(",");
						for(DeeSectionProps prop : props){
							for(int i=0; i<fields.length; i++){
								if(fields[i].equals(prop.getPropName())){
									showProps.put(prop.getPropName(), prop.getPropValue());
									if(notDateField(prop.getPropMeta())){
										searchProps.put(prop.getPropName(), prop.getPropValue());
									}
								}
							}
						}
					}
				}else{
					showProps = defaultShowProps;
				}
			}
			
			DeeSectionDefine deeSectionDefine = this.findDeeSectionById(Long.valueOf(sectionDefineId));
			
			List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
			DEEClient client = new DEEClient();
			try {
				Parameters deeParam = new Parameters();
				int totalCount = 0;
				deeParam.add(DEEConfigService.PARAM_PAGESIZE, Integer.valueOf(pageSize));
				deeParam.add(DEEConfigService.PARAM_PAGENUMBER, Integer.valueOf(page));
				String condition = deeSearchKey;
				String textfield = deeSearchValue;
				String where = " where 1=1 ";
				if(Strings.isNotBlank(condition)&&Strings.isNotBlank(textfield)){
					where += " AND "+condition+" like '%"+StringEscapeUtils.escapeSql(textfield)+"%'";
				}
				
				deeParam.add("whereString", where);
				com.seeyon.v3x.dee.Document document = client.execute(String.valueOf(deeSectionDefine.getFlowId()),deeParam);
				com.seeyon.v3x.dee.Document.Element root =  document.getRootElement();
				List<com.seeyon.v3x.dee.Document.Element> list = root.getChildren();
				if(CollectionUtils.isNotEmpty(list)){
					for(com.seeyon.v3x.dee.Document.Element t : list){
						Attribute total = (Attribute) t.getAttribute("totalCount");
						if(total!=null&&total.getValue()!=null){
							totalCount = Integer.parseInt(String.valueOf(total.getValue()));
							//Pagination.setRowCount(totalCount);
						}
						
						List<com.seeyon.v3x.dee.Document.Element> rows = t.getChildren();
						if(CollectionUtils.isNotEmpty(rows)&&totalCount>0){
							for(com.seeyon.v3x.dee.Document.Element row : rows){
								if(props!=null&&!props.isEmpty()){
									Map<String,Object> rowMap = new LinkedHashMap<String,Object>();
									
									Set<String> keys = null;
									
									keys = showProps.keySet();
									
									for(String key : keys){
										com.seeyon.v3x.dee.Document.Element e = row.getChild(key);
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
												rowMap.put(new String(key), e.getValue());
											}else{
												rowMap.put(new String(key), "");
											}
										}else{
											rowMap.put(new String(key), "");
										}
									}
									data.add(rowMap);
								}
							}
						}
					}
				}
				document = null;
				fi.setData(data);
				fi.setTotal(totalCount);
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
        
        
        return fi;
	}
	
	public boolean notDateField(String fieldType){
		String type = fieldType.toLowerCase();
		if(type.indexOf("time")!=-1||type.indexOf("date")!=-1||type.indexOf("timestamp")!=-1){
			return false;
		}else{
			return true;
		}
	}
	public PortletEntityPropertyManager getPortletEntityPropertyManager() {
		return portletEntityPropertyManager;
	}
	public void setPortletEntityPropertyManager(
			PortletEntityPropertyManager portletEntityPropertyManager) {
		this.portletEntityPropertyManager = portletEntityPropertyManager;
	}
	
}
