package com.seeyon.apps.dee;


import org.apache.log4j.Logger;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.v3x.dee.DataSourceManager;

public class DEEInitializer extends AbstractSystemInitializer {
	private static final Logger log = Logger.getLogger(DEEInitializer.class);
	public static final String DEE_HOME = "DEE_HOME";

	public void initialize() {
		
//		String dee_home = System.getenv(DEE_HOME);
//		if (dee_home == null) {
//			dee_home = SystemEnvironment.getBaseFolder() + File.separator
//					+ "dee";
//			System.setProperty(DEE_HOME, dee_home);
//			File dee_home_path = new File(dee_home);
//			if(!dee_home_path.exists()){
//				dee_home_path.mkdirs();
//			}
//		}
		if(AppContext.hasPlugin("dee")){
			log.info("初始化DEE……");
			DataSourceManager.getInstance();
		}
	}

	public void destroy() {
		System.out.println("销毁Samples模块");
	}
}
