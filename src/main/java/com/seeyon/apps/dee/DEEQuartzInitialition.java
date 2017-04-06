package com.seeyon.apps.dee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.v3x.dee.schedule.QuartzManager;

public class DEEQuartzInitialition extends AbstractSystemInitializer {
	private final static Log log = LogFactory
			.getLog(DEEQuartzInitialition.class);

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize() {
		log.info("清理DEE相关Quartz持久化数据。");
		QuartzHolder.deleteQuartzJobByGroup(QuartzManager.JOB_GROUP_NAME);
	}

}
