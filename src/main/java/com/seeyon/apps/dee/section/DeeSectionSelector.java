/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.dee.section;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.seeyon.apps.dee.manager.DeeSectionManager;
import com.seeyon.apps.dee.po.DeeSectionDefine;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.link.manager.LinkCategoryManager;
import com.seeyon.ctp.portal.link.manager.LinkSectionManager;
import com.seeyon.ctp.portal.section.bo.SectionTreeNode;
import com.seeyon.ctp.portal.section.manager.BaseAbstractSectionSelector;
import com.seeyon.ctp.util.UUIDLong;

/**
 * @author admin
 *
 */
public class DeeSectionSelector extends BaseAbstractSectionSelector{
	private LinkCategoryManager   linkCategoryManager;
    private LinkSectionManager linkSectionManager;
    private DeeSectionManager deeSectionManager;
    private OrgManager orgManager ;
	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
    public void setDeeSectionManager(DeeSectionManager deeSectionManager) {
		this.deeSectionManager = deeSectionManager;
	}
	public void setLinkSectionManager(LinkSectionManager linkSectionManager) {
        this.linkSectionManager = linkSectionManager;
    }
    public void setLinkCategoryManager(LinkCategoryManager linkCategoryManager) {
        this.linkCategoryManager = linkCategoryManager;
    }
    /* (non-Javadoc)
     * @see com.seeyon.ctp.portal.section.manager.BaseAbstractSectionSelector#selectSectionTreeData(java.lang.String, java.lang.String)
     */
    public String getId() {
        return "deeSectionSelector";
    }
    public String getBaseName(Map<String, String> preference) {
    	return "deeSectionSelector";
    }
    @Override
    public List<SectionTreeNode> selectSectionTreeData(String spaceType, String spaceId) throws BusinessException {
        List<String[]> sections = super.selectAllowedSections(spaceType);
        List<SectionTreeNode> l = new ArrayList<SectionTreeNode>();
        SectionTreeNode rootNode = new SectionTreeNode();
//        rootNode.setId(String.valueOf(UUIDLong.longUUID()));
//        rootNode.setSectionName(ResourceUtil.getString("publicManager.select"));
    //    l.add(rootNode);
        String rootID = String.valueOf(UUIDLong.longUUID());
        if(CollectionUtils.isNotEmpty(sections)){
            for(String[] str : sections){
                if("deeSection".equals(str[0])){
                	List<DeeSectionDefine> deeSections = deeSectionManager.findAllDeeSection();
                	rootNode.setId(rootID);
                    rootNode.setSectionName(ResourceUtil.getString("dee.section.name"));
                    rootNode.setParentId("forumSectionTreeRoot");
                    l.add(rootNode);
                    if(deeSections != null){
            			for(DeeSectionDefine dee : deeSections){
            				SectionTreeNode rootNode1 = new SectionTreeNode();
                            rootNode1.setId(dee.getId().toString());
                            rootNode1.setSectionName(dee.getDeeSectionName());
                            rootNode1.setSectionBeanId(str[0]);
                            rootNode1.setSectionCategory(str[2]);
                            rootNode1.setParentId(rootID);
                            Map<String,String> showProps = new LinkedHashMap<String,String>();
                            showProps.put("columnsName",dee.getDeeSectionName());
                            rootNode1.setProperties(showProps);
                            rootNode1.setSingleBoardId(dee.getId().toString());
                            DeeSection deeSection = new DeeSection();
                            if(deeSection.isAllowUserUsed(rootNode1.getSingleBoardId(),deeSectionManager,orgManager)){
                            	 l.add(rootNode1);
                            }
            			}
            		}
                    
                }
            }
        }
        return l;
    }
    public boolean isAllowUserUsed(String singleBoardId) {
    	return true;
    }

}
