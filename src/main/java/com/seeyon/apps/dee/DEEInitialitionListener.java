package com.seeyon.apps.dee;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.SystemEnvironment;

import www.seeyon.com.mocnoyees.LRWMMocnoyees;
import www.seeyon.com.mocnoyees.MSGMocnoyees;

public class DEEInitialitionListener implements ServletContextListener {
	private final static Log log = LogFactory.getLog(DEEInitialitionListener.class);
	public static final String DEE_HOME = "DEE_HOME";

	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void contextInitialized(ServletContextEvent arg0) {
		String dee_home = System.getenv(DEE_HOME);
		if (dee_home == null) {
			dee_home = SystemEnvironment.getBaseFolder() + File.separator
					+ "dee";
			System.setProperty(DEE_HOME, dee_home);
		}
		File deeLicensePath = new File(dee_home + File.separator + "licence");
		if (deeLicensePath.exists() && deeLicensePath.isDirectory()) {
			File[] keys = deeLicensePath.listFiles(new DeekeyFileFilter(Pattern
					.compile("(?:.+\\.seeyonkey)")));
			boolean checked = keys.length > 0;
			for (File file : keys) {
				checked = checked && checkLicense(file);
			}
			if(checked){
//				DataSourceManager.getInstance();
			}else{
				log.info("授权文件错误，加载DEE失败.");
			}
		} else {
			log.info("未发现DEE License.");
			return;
		}
	}

	private boolean checkLicense(File f) {
		try {
			LRWMMocnoyees lrwmmocnoyees = new LRWMMocnoyees(f);
			MSGMocnoyees dog = new MSGMocnoyees(lrwmmocnoyees);
			String s = dog.methodz("EE");
			if (s == null) {
				return false;
			}
			String regex = "<\\s*value\\s*>(.*?)<\\s*/value\\s*>";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(s);
			return true;
			// return dog.methoda("value");
		} catch (Exception e) {
			log.error("获取flow licence时出错：", e);
		}
		return false;
	}

	public class DeekeyFileFilter implements FileFilter {
		protected Pattern _pattern;

		public DeekeyFileFilter(Pattern pattern) {
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
