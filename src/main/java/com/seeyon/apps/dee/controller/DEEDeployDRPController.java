package com.seeyon.apps.dee.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.SystemEnvironment ;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;

@CheckRoleAccess(roleTypes={Role_NAME.GroupAdmin,Role_NAME.AccountAdministrator})
public class DEEDeployDRPController extends BaseController {

    private static final String baseName = "com.seeyon.apps.dee.resources.i18n.DeeResources";
    private static final String DEE_HOME = "DEE_HOME";
    /**
     * 日志
     */
    private static final Log log = LogFactory.getLog(DEEDeployDRPController.class);

    public ModelAndView deployDRP(HttpServletRequest request, HttpServletResponse response) {
        String retMsg;
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile multipartFile = multipartRequest.getFile("drpFile");
            if (StringUtils.isNotEmpty(multipartFile.getOriginalFilename()) &&
                    multipartFile.getOriginalFilename().toLowerCase().endsWith(".drp")) {
                String fileName = multipartFile.getOriginalFilename();
                if (!fileName.toLowerCase().endsWith(".drp")) {
                    retMsg = ResourceBundleUtil.getString(this.baseName,
                            "dee.deploy.errfile.label");
                } else {
                    String dee_home = System.getProperty(DEE_HOME);
                    if (dee_home == null) {
                        dee_home = SystemEnvironment.getBaseFolder() + File.separator + "dee";
                        System.setProperty(DEE_HOME, dee_home);
                    }

                    String fileRealPath = dee_home + File.separator + "hotdeploy" + File.separator + fileName;
                    File file = new File(fileRealPath);
                    multipartFile.transferTo(file);
                    retMsg = ResourceBundleUtil.getString(this.baseName,
                            "dee.deploy.success.label");
                }
            } else {
                retMsg = ResourceBundleUtil.getString(this.baseName,
                        "dee.deploy.errfile.label");
            }
        } catch (IOException e) {
            retMsg = ResourceBundleUtil.getString(this.baseName,
                    "dee.deploy.failed.label");
            e.printStackTrace();
            log.error(e);
        }
        ModelAndView view = new ModelAndView("plugin/dee/uploadDRP/uploadDRP");
        view.addObject("retMsg", retMsg);
        return view;
    }

    public ModelAndView show(HttpServletRequest request,
                             HttpServletResponse response) {
        ModelAndView view = new ModelAndView("plugin/dee/uploadDRP/uploadDRP");
        return view;

    }
}
