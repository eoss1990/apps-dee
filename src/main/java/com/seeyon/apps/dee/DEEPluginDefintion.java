package com.seeyon.apps.dee;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.seeyon.ctp.cluster.ClusterConfigBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.init.MclclzUtil;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.plugin.PluginInitializer;
import com.seeyon.ctp.util.TextEncoder;
import com.seeyon.v3x.dee.DataSourceManager;

import www.seeyon.com.mocnoyees.DogException;
import www.seeyon.com.mocnoyees.LRWMMocnoyees;
import www.seeyon.com.mocnoyees.MSGMocnoyees;

public class DEEPluginDefintion implements PluginInitializer {
	public static final String DEE_HOME = "DEE_HOME";
	private static final String deeKey = "fff3c53847b317c15cd3e162fe9c0b31";

	@Override
	public boolean isAllowStartup(PluginDefinition pd, Logger logger) {
		if ("true".equals(pd.getPluginProperty("dee.enable"))) {
//			String path = this.getClass().getResource("/").getPath()+"../lib/dee-client.jar";
//			String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();				
//	    	try {
//				path = java.net.URLDecoder.decode(path, "UTF-8");
//				if(path.indexOf("WEB-INF/lib") < 0){
//					logger.error("检查DEE客户端版本时，获取路径出错！");
//					return false;
//				}
//				path = path.substring(0,path.indexOf("WEB-INF/lib")) + "WEB-INF/lib/dee-client.jar";
//	    		File file = new File(path);
//				String md5 = DigestUtils.md5Hex(new FileInputStream(file));
//				if(!md5.trim().equals(deeKey))
//				{
//					return false;
//				}
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				logger.error("检查DEE客户端版本出现异常：" + e1.getMessage());
//				return false;
//			}

            Class obj = null;
            try {
                obj = Class.forName("com.seeyon.v3x.dee.context.EngineController");
            } catch (Throwable ignored) {
            }
            //TODO 暂时注释 
            if (obj != null) {
                return false;
            }

            final String deeEnableConst = "DEE.dee.enable";

            try {
				logger.info("初始化DEE……");

                AppContext.putThreadContext(deeEnableConst, Boolean.TRUE);

                Class<?> c1s = MclclzUtil.ioiekc("com.seeyon.ctp.product.ProductInfo");
                Boolean isDev = (Boolean) c1s.getMethod("isDev").invoke(null);
              //是否开发环境
				if(!isDev){
					boolean deeFlag = ((Boolean)MclclzUtil.invoke(c1s, "hasPlugin",
							new Class[] { String.class }, null, new Object[] { "dee" })).booleanValue();
					logger.info("dee是否启用："+deeFlag);
//					if(deeFlag){
//						DataSourceManager.getInstance();
//					}
					return deeFlag;
				} 
				
//                String a8DogNo = (String) c1s.getMethod("getDogNo").invoke(null);
//                Boolean isU8OEM = (Boolean) c1s.getMethod("isU8OEM").invoke(null);
//                Boolean flag = false;
//                if(a8DogNo != null ){
//                    a8DogNo = TextEncoder.decode(a8DogNo);
//                    if(!a8DogNo.equals("-2"))
//                    flag =true;
//                }
//                if (!isDev && flag) {
//                    String deeHome = System.getProperty(DEE_HOME);
//                    if (deeHome == null) {
//                        deeHome = SystemEnvironment.getBaseFolder() + File.separator + "dee";
//                        System.setProperty(DEE_HOME, deeHome);
//                        File deeHomePath = new File(deeHome);
//                        if (!deeHomePath.exists()) {
//                            deeHomePath.mkdirs();
//                        }
//                    }
//                    // System.out.println("================================================================================================"+dee_home);
//                    File licensePath = new File(deeHome + File.separator + "licence");
//                    if (licensePath.isDirectory()) {
//                        File[] licensesFiles = licensePath
//                                .listFiles(new LicenseFileFilter(Pattern
//                                        .compile("(?:.+\\.seeyonkey)")));
//                        if (licensesFiles.length > 0) {
//                            for (File file : licensesFiles) {
//                                LRWMMocnoyees lrwmmocnoyees;
//                                lrwmmocnoyees = new LRWMMocnoyees(file);
//                                MSGMocnoyees dog = new MSGMocnoyees(lrwmmocnoyees);
//
//
//                                if (!isU8OEM) {
//                                	ClusterConfigBean clusterBean = ClusterConfigBean.getInstance();
//									if (clusterBean.isClusterEnabled()
//											&& !clusterBean.isClusterMain()){
//										DataSourceManager.getInstance();
//										return true;
//									}
//                                    String deeDogNo = dog.methodz("EE.EE2");
//                                    if (deeDogNo == null || !deeDogNo.equals(a8DogNo)) {
//                                        logger.error("A8加密狗号（" + a8DogNo + "）和dee License文件加密狗号（" + deeDogNo + "）不一致。");
//                                        return false;
//                                    }
//
//                                }
//                            }
//                        } else {
//                            logger.error("加载DEE插件错误：License文件不存在。");
//                            return false;
//                        }
//                    } else {
//                        logger.error("加载DEE插件错误：License文件存放路径不存在。");
//                        return false;
//                    }
//                }

//				DataSourceManager.getInstance();
				return true;
			} 
//            catch (DogException e) {
//                logger.error("加密狗错误：" + e.getLocalizedMessage(), e);
//                return false;
//            } 
            catch (Exception e) {
				logger.error("加载DEE插件错误：License文件解析错误。"+e.getLocalizedMessage(), e);
				return false;
			} finally {
                AppContext.removeThreadContext(deeEnableConst);
            }
        } else {
            return false;
        }
	}

	public class LicenseFileFilter implements FileFilter {
		protected Pattern _pattern;

		public LicenseFileFilter(Pattern pattern) {
			_pattern = pattern;
		}

		@Override
		public boolean accept(File pathname) {
			boolean res;
			if (pathname.isFile()) {
				if (_pattern != null) {
					String fileName = pathname.getName();
					Matcher m = _pattern.matcher(fileName);
					res = m.matches();
				} else {
					res = true;
				}
			} else {
				res = false;
			}
			return res;
		}
	}
	
}
