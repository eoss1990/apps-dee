package  com.seeyon.apps.dee.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.apps.dee.po.DeeSectionProps;
import com.seeyon.apps.dee.po.DeeSectionSecurity;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;

public class DeeSectionDaoImpl implements DeeSectionDao{

	@Override
	public void saveDeeSection(DeeSectionDefine deeSection) {
		DBAgent.save(deeSection);
	}

	@Override
	public void updateDeeSection(DeeSectionDefine deeSection) {
		DBAgent.update(deeSection);
	}

	@Override
	public void deleteDeeSection(long id) {
		String hql1 = "delete from " + DeeSectionDefine.class.getName() +" where id = ?";
		String hql2 = "delete from "+ DeeSectionProps.class.getName() +" where deeSectionId = ?";
		String hql3 = "delete from "+ DeeSectionSecurity.class.getName() +" where deeSectionId = ?";
		DBAgent.bulkUpdate(hql1,id);
		DBAgent.bulkUpdate(hql2,id);
		DBAgent.bulkUpdate(hql3,id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DeeSectionDefine> getAllDeeSection() {
		return DBAgent.find("from "+ DeeSectionDefine.class.getName());
	}

	@Override
	public DeeSectionDefine getDeeSectinById(long id) {
		String hql = " from "+ DeeSectionDefine.class.getName() + " where id = :id";
		HashMap p = new HashMap();
		p.put("id", id);
		List<DeeSectionDefine> securities = DBAgent.find(hql, p);
		if(null!=securities && !securities.isEmpty()){
			return securities.get(0);
		}
		return null;
	}

	@Override
	public void save(DeeSectionDefine deeSection, List<DeeSectionSecurity> securities) {
		this.saveDeeSection(deeSection);

		if(securities != null && !securities.isEmpty()){
			for (DeeSectionSecurity security : securities) {
				DBAgent.save(security);
			}
		}
	}

	@Override
	public void update(DeeSectionDefine deeSection, List<DeeSectionSecurity> securities) {
		DBAgent.update(deeSection);

		String hql = "delete from "+ DeeSectionSecurity.class.getName() +" where deeSectionId = ?";
		DBAgent.bulkUpdate(hql,deeSection.getId());

		if(securities != null && !securities.isEmpty()){
			for (DeeSectionSecurity security : securities) {
				DBAgent.save(security);
			}
		}
	}

	@Override
	public List<DeeSectionSecurity> getSectionSecurity(long entityId) {
		String hql = " from "+ DeeSectionSecurity.class.getName() + " where deeSectionId = :id";
		HashMap p = new HashMap();
		p.put("id", entityId);
		List<DeeSectionSecurity> securities = DBAgent.find(hql, p);
		return securities;
	}

	@Override
	public List<DeeSectionProps> getAllSectionProps() {
		return DBAgent.loadAll(DeeSectionProps.class);
	}

	@Override
	public List<DeeSectionProps> getPropsByDeeSectionId(long deeSectionId) {
		String hql = "from "+DeeSectionProps.class.getName()+" where deeSectionId=:id order by sort asc";
		HashMap p = new HashMap();
		p.put("id", deeSectionId);
		return DBAgent.find(hql,p);
	}

	@Override
	public void saveDeeSectionProps(long id, Map<String, Map<String,String>> props) {
		String hql = "delete from "+ DeeSectionProps.class.getName() +" where deeSectionId = ?";
		DBAgent.bulkUpdate(hql,id);

		if(props!=null&&!props.isEmpty()){
			Set<String> keys = props.keySet();
			for(String key : keys){
				DeeSectionProps sectionProp = new DeeSectionProps();
				Map<String,String> map = props.get(key);
				sectionProp.setIdIfNew();
				sectionProp.setDeeSectionId(id);
				sectionProp.setPropName(key);
				sectionProp.setPropValue(map.get("displayName"));
				sectionProp.setPropMeta(map.get("fieldType"));
				sectionProp.setIsShow(Integer.valueOf(map.get("isShow")));
				String sort = map.get("sort");
				if(Strings.isNotBlank(sort)){
					sectionProp.setSort(Integer.valueOf(sort));
				}
				DBAgent.save(sectionProp);
			}
		}

	}

	@Override
	public List<DeeSectionDefine> getAllDeeSection(String sectionName) {
		if(Strings.isNotBlank(sectionName)){
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("name", "%"+SQLWildcardUtil.escape(sectionName.trim())+"%");
			return DBAgent.find("from "+ DeeSectionDefine.class.getName()+" where deeSectionName like :name",parameterMap);
		}else{
			return DBAgent.find("from "+ DeeSectionDefine.class.getName());
		}
	}

	@Override
	public List<DeeSectionDefine> getDeeSectionByName(String sectionName) {
		if(Strings.isNotBlank(sectionName)){
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("name", SQLWildcardUtil.escape(sectionName.trim()));
			return DBAgent.find("from "+ DeeSectionDefine.class.getName()+" where deeSectionName = :name",parameterMap);
		}else{
			return null;
		}
	}

	@Override
	public List<DeeSectionSecurity> getDeeSectionBySecurity(List<Long> entityIds) {
		String hql = " from " + DeeSectionSecurity.class.getName() + " where entityId in (:entityId)";
		Map<String, Object> nameParameters = new HashMap<String, Object>();
		nameParameters.put("entityId", entityIds);
		return DBAgent.find(hql.toString(), nameParameters);
	}
}
