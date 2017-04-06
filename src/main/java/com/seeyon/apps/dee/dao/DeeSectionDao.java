package  com.seeyon.apps.dee.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.apps.dee.po.DeeSectionProps;
import com.seeyon.apps.dee.po.DeeSectionSecurity;



public interface DeeSectionDao{
	
	public void saveDeeSection(DeeSectionDefine deeSection);
	
	public void updateDeeSection(DeeSectionDefine deeSection);
	
	public void deleteDeeSection(long id);
	
	public List<DeeSectionDefine> getAllDeeSection();
	
	public List<DeeSectionDefine> getDeeSectionByName(String sectionName);
	
	public List<DeeSectionDefine> getAllDeeSection(String sectionName);
	
	public DeeSectionDefine getDeeSectinById(long id);
	
	public void save(DeeSectionDefine deeSection,List<DeeSectionSecurity> securities);
	
	public void update(DeeSectionDefine deeSection,List<DeeSectionSecurity> securities);
	
	public List<DeeSectionSecurity> getSectionSecurity(long entityId);
	
	public List<DeeSectionProps> getAllSectionProps();
	
	public List<DeeSectionProps> getPropsByDeeSectionId(long deeSectionId);
	
	public void saveDeeSectionProps (long id,Map<String,Map<String,String>> props);
	
	public List<DeeSectionSecurity> getDeeSectionBySecurity(List<Long> entityIds);
}
