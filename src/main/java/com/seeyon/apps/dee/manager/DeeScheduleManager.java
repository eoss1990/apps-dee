package com.seeyon.apps.dee.manager;

import java.util.Map;

import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.common.db.schedule.model.ScheduleBean;


/**
 * 功能说明：对Dee数据源的操作
 * @author XQ
 *
 */
public interface DeeScheduleManager {
	public ScheduleBean findById(String id) throws TransformException;
	
	public void update(ScheduleBean drb) throws TransformException;

	public void deleteSchedule(String[] ids) throws TransformException;

    public FlipInfo findScheduleList(FlipInfo fi, Map<String, Object> param);
}
