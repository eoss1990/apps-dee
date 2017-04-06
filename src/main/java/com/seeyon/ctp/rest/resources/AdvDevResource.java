package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.form.modules.event.CollaborationFormBindEventListener;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.form.util.FormUtil;
import com.seeyon.ctp.util.ParamUtil;
import org.apache.commons.logging.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * dee开发高级业务rest接口
 * Created by dkywolf on 2016-8-17.
 */
@Path("dee")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public class AdvDevResource extends BaseResource{
    private static final Log LOGGER = CtpLogFactory.getLog(AdvDevResource.class);

    private CollaborationFormBindEventListener collaborationFormBindEventListener = (CollaborationFormBindEventListener) AppContext.getBean("collaborationFormBindEventListener");

    /**
     * 取得任务类型
     * IN
     * affairid,attitude,strOperationId
     *
     * OUT
     * {Map}d
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("achieveTaskType")
    public Response achieveTaskType(Map<String,Object> params) throws BusinessException {
        DataContainer dc = new DataContainer();
        Long affairid = ParamUtil.getLong(params,"affairid");
        String attitude = ParamUtil.getString(params,"attitude");
        String operationId = ParamUtil.getString(params,"operationId");
        Map retMap = new HashMap<String,Object>();
        String taskType = "";
        try {
            taskType = collaborationFormBindEventListener.achieveTaskType(affairid,attitude,operationId);
            retMap.put(FormConstant.SUCCESS,"true");
            retMap.put(FormConstant.ERRORMSG,"");
            retMap.put("taskType",taskType);
        }
        catch (Exception e){
            retMap.put(FormConstant.SUCCESS,"false");
            retMap.put(FormConstant.ERRORMSG,e.getMessage());
            LOGGER.error("取任务类型出现异常："+e.getMessage());
        }
        return ok(retMap);
    }

    /**
     * DEE处理
     * IN
     * affairId       事件ID
     * attitude       态度，发送/撤销/回退...，在表单处理时为提交、已阅和拒绝的国际化
     * operationId        发送时，为操作ID，其他时候为提交意见
     * currentEventId 当前事件ID
     * skipConcurrent 跳过并发
     * @return {<p/>
     * hasNext:                     true or false<p/>
     * skipConcurrent:              true or false<p/>
     * currentEventId:              当前事件ID<p/>
     * blockInfoMsgType:            阻塞弹出框类型，error or info<p/>
     * blockInfoReason:             阻塞弹出框内容<p/>
     * blockFormWriteBackJson:      表单回填Json字符串<p/>
     * }
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("preDeeHandler")
    public Response preDeeHandler(Map<String,Object> params) throws BusinessException {
        Long affairid = ParamUtil.getLong(params,"affairid");
        String attitude = ParamUtil.getString(params,"attitude");
        String operationId = ParamUtil.getString(params,"operationId");
        String currentEventId = ParamUtil.getString(params,"currentEventId");
        String skipConcurrent = ParamUtil.getString(params,"skipConcurrent");
        Map retMap = new HashMap<String,Object>();
        try {
            retMap = collaborationFormBindEventListener.preDeeHandler(affairid,attitude,operationId,currentEventId,skipConcurrent);
//            if (pcMap != null && pcMap.size()>0)
//                retMap.putAll(pcMap);
            retMap.put(FormConstant.SUCCESS,"true");
            retMap.put(FormConstant.ERRORMSG,"");
//            Map<String, Object> results = FormUtil.getRestChangeFieldInfo();
//            retMap.put(FormConstant.RESULTS, results);
        }
        catch (Exception e){
            retMap.put(FormConstant.SUCCESS,"false");
            retMap.put(FormConstant.ERRORMSG,e.getMessage());
            LOGGER.error("执行DEE任务出现异常："+e.getMessage());
        }
        return ok(retMap);
    }
    /**
     * 执行扩展类
     * IN
     * affairid,attitude,strOperationId
     *
     * OUT
     * {Map}d
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("preExtHandler")
    public Response preExtHandler(Map<String,Object> params) throws BusinessException {
        Long affairid = ParamUtil.getLong(params,"affairid");
        String attitude = ParamUtil.getString(params,"attitude");
        String operationId = ParamUtil.getString(params,"operationId");
        Map retMap = new HashMap<String,Object>();
        try {
            String[] extRet = collaborationFormBindEventListener.preHandler(affairid,attitude,operationId);
            if (extRet == null){
                retMap.put(FormConstant.SUCCESS,"true");
                retMap.put(FormConstant.ERRORMSG,"");
            }
            else {
                retMap.put(FormConstant.SUCCESS,"false");
                String errMsg = "执行扩展类出错";
                //返回错误信息
                if (extRet.length == 2){
                    errMsg = extRet[1];
                }
                retMap.put(FormConstant.ERRORMSG,errMsg);
            }
        }
        catch (Exception e){
            retMap.put(FormConstant.SUCCESS,"false");
            retMap.put(FormConstant.ERRORMSG,e.getMessage());
            LOGGER.error("执行扩展类出现异常："+e.getMessage());
        }
        return ok(retMap);
    }
}
