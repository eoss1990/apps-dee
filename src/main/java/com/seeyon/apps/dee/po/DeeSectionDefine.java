package  com.seeyon.apps.dee.po;

import java.io.Serializable;

import com.seeyon.v3x.common.domain.BaseModel;

/**
 * The data exchange engine section
 */
public class DeeSectionDefine extends BaseModel implements Serializable{

	private static final long serialVersionUID = -8078224843882595016L;
	
	private long flowId;
	
	private String moduleName;
	
	private Integer pageHeight;
	
	private String deeSectionName;
	
	private String flowDisName;
	
	private String portalStyle;
	
	private String chartStyle;
	
	private String org;
	
	public String getFlowDisName() {
		return flowDisName;
	}

	public void setFlowDisName(String flowDisName) {
		this.flowDisName = flowDisName;
	}

	public DeeSectionDefine(){
		
	}
	
	public DeeSectionDefine(long flowId,String flowDisName,String moduleName,Integer pageHeight,String deeSectionName){
		super();
		this.flowId = flowId;
		this.moduleName = moduleName;
		this.pageHeight = pageHeight;
		this.deeSectionName = deeSectionName;
		this.flowDisName = flowDisName;
	}
	
	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public Integer getPageHeight() {
		return pageHeight;
	}

	public void setPageHeight(Integer pageHeight) {
		this.pageHeight = pageHeight;
	}

	public String getDeeSectionName() {
		return deeSectionName;
	}

	public void setDeeSectionName(String deeSectionName) {
		this.deeSectionName = deeSectionName;
	}

	public String getPortalStyle() {
		return portalStyle;
	}

	public void setPortalStyle(String portalStyle) {
		this.portalStyle = portalStyle;
	}

	public String getChartStyle() {
		return chartStyle;
	}

	public void setChartStyle(String chartStyle) {
		this.chartStyle = chartStyle;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}
	
}