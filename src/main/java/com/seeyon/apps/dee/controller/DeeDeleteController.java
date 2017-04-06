package com.seeyon.apps.dee.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.controller.BaseController;

public class DeeDeleteController extends BaseController {
    public ModelAndView getFlowFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("plugin/dee/deleteFlow/deleteFlow");
    }
}
