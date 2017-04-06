package com.seeyon.apps.dee.event;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.batch.BatchState;
import com.seeyon.apps.collaboration.batch.exception.BatchException;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.dee.util.FlowFormUtil;
import com.seeyon.apps.dee.util.TransDateUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.SystemInitializer;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.form.bean.*;
import com.seeyon.ctp.form.dee.bean.InfoPath_DeeParam;
import com.seeyon.ctp.form.dee.bean.InfoPath_DeeTask;
import com.seeyon.ctp.form.modules.engin.base.formData.FormDataManager;
import com.seeyon.ctp.form.modules.event.CollaborationEventTask;
import com.seeyon.ctp.form.modules.event.CollaborationFormBindEventListener;
import com.seeyon.ctp.form.service.FormCacheManager;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.v3x.dee.DEEClient;
import com.seeyon.v3x.dee.Parameters;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.datasource.XMLDataSource;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.BeansException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理前事件监听(提交前,回退前,终止前,撤销前)
 * id保持到数据库,类型:提交前,阻塞式
 *
 * @author zhangyong
 * @since NC-OA5.72 2012-04-27
 * 2013-11-12 迁移5.0
 */
public class CollaborationFormBindEventListenerImpl implements CollaborationFormBindEventListener ,SystemInitializer{
	
    private static Log logger = CtpLogFactory.getLog(CollaborationFormBindEventListenerImpl.class);
    private AffairManager affairManager;
    private Map<String, CollaborationEventTask> collaborationEvent = new ConcurrentHashMap<String, CollaborationEventTask>();
    private CollaborationApi collaborationApi;
    private FormManager formManager;
    private FormCacheManager formCacheManager;
    private FormDataManager formDataManager;

    public static final String KEY_HASNEXT = "hasNext";
    public static final String KEY_SKIPCONCURRENT = "skipConcurrent";
    public static final String KEY_CURRENTEVENTID = "currentEventId";

    public static final String BLOCK_INFO_MSG_TYPE = "blockInfoMsgType";
    public static final String BLOCK_INFO_MSG_TYPE_ERROR = "error";
    public static final String BLOCK_INFO_MSG_TYPE_INFO = "info";
    public static final String BLOCK_INFO_REASON = "blockInfoReason";
    public static final String BLOCK_FORM_WRITE_BACK_JSON = "blockFormWriteBackJson";
    public static final String BLOCK_EXCEPTION_INFO = "exception";


	public void setFormCacheManager(FormCacheManager formCacheManager) {
        this.formCacheManager = formCacheManager;
    }

    public void setFormManager(FormManager formManager) {
        this.formManager = formManager;
    }

    public void setFormDataManager(FormDataManager formDataManager) {
		this.formDataManager = formDataManager;
	}

	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    
    @Override
    public void initialize() {
    	 try {
             List<SortEvent> sort = new ArrayList<SortEvent>();
             @SuppressWarnings("unchecked")
             Map<String, CollaborationEventTask> events = AppContext.getBeansOfType(CollaborationEventTask.class);
             for (Map.Entry<String, CollaborationEventTask> event : events.entrySet()) {
                 sort.add(new SortEvent(event.getValue()));
             }
             Collections.sort(sort);
             String name = "";
             for (SortEvent sortEvent : sort) {
                 collaborationEvent.put(String.valueOf(sortEvent.getEvent().getId()), sortEvent.getEvent());
                 name = name + sortEvent.getEvent().getLabel() + ",";
             }
             logger.info("加载高级设置中的事件实现:[" + StringUtils.removeEnd(name, ",") + "]");
         } catch (BeansException e) {
             logger.error(e.getMessage(), e);
         }
    	
    }
    
    @Override
    public void destroy() {
    	// TODO Auto-generated method stub
    	
    }
    
    public void init() {
       
    }

    public List<CollaborationEventTask> getAllCollaborationEvent() {
        return new ArrayList<CollaborationEventTask>(collaborationEvent.values());
    }

    /**
     * 批处理提交校验，只校验，不执行
     * @param formAppId
     * @param formOperationId
     * @param colSummaryId
     * @throws BatchException
     */
    public void checkBatch(long formAppId, long formOperationId, long colSummaryId) throws BatchException {
    	 try {
             List<Operation_BindEvent> formEentBind = FormService.getEventBindList(colSummaryId, formOperationId);
             if (formEentBind == null) {
                 return;
             }
             for (Operation_BindEvent event : formEentBind) {
                 if (event == null) {
                     continue;
                 }
                 if (EventModel.block.name().equals(event.getModel())) {
                     throw new BatchException(BatchState.NotSupport.getCode());
                 }
             }
         } catch (BusinessException e1) {
             logger.error("开发高级处理事件批处理异常", e1);
         }
    }
    
    /**
     * 支持批处理表单
     *
     * @param formAppId
     * @param formOperationId
     * @param colSummaryId
     * @throws BatchException
     */
    public void checkBindEventBatch(long formAppId, long formOperationId, long colSummaryId) throws BatchException {
        try {
            List<Operation_BindEvent> formEentBind = FormService.getEventBindList(colSummaryId, formOperationId);

            if (formEentBind == null) {
                return;
            }
            for (Operation_BindEvent event : formEentBind) {
                if (event == null) {
                    continue;
                }
                if (EventModel.block.name().equals(event.getModel())) {
                    throw new BatchException(BatchState.NotSupport.getCode());
                }
                if (event.getOperationType().equalsIgnoreCase(ColEvent.submit.name())) {
                    if (event.getTaskType().equals(TaskType.dee.name())) {
                        try {
                            ColSummary summary = collaborationApi.getColSummary(colSummaryId);
                            if (summary != null) {
                                excuteDeeTask(event, summary);
                            }
                        } catch (Exception e) {
                            logger.error("节点事件触发DEE任务失败", e);
                        }
                    } else {
                        throw new BatchException(BatchState.NotSupport.getCode());
//                        if (collaborationEvent.isEmpty()) {
//                            this.init();
//                        }
//                        if (CollectionUtils.isEmpty(collaborationEvent.values())) {
//                            return;
//                        }
//                        CollaborationEventTask task = collaborationEvent.get(event.getTaskName());
//                        if (task == null) {
//                            continue;
//                        }
                    }
                }

            }
        } catch (BusinessException e1) {
            logger.error("开发高级处理事件批处理异常", e1);
        }

    }

    /**
     * 取得任务类型
     *
     * @param affairId       事件ID
     * @param attitude       操作类型
     * @param strOperationId 操作权限ID
     * @return 扩展类：ext，DEE：dee，没有：null
     */
    public String achieveTaskType(long affairId, String attitude, String strOperationId) {
        try {
            List<Operation_BindEvent> bindEvents = null;
            if (ColEvent.start.name().equalsIgnoreCase(attitude)) {
                // 取得表单数据
                FormDataMasterBean formDataMasterBean = formManager.getSessioMasterDataBean(affairId);
                if(formDataMasterBean == null) return null;
                if(formDataMasterBean.getFormTable() == null) return null;
                // 取得表单信息
                FormBean formBean = formCacheManager.getForm(formDataMasterBean.getFormTable().getFormId());
                // 操作权限ID
                Long operationId = Long.valueOf(strOperationId);
                FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(operationId);
                // 如果传入为子权限ID，则通过父权限ID查找开发高级中的Bean
                Long parentId = formAuthViewBean.getParentId();
                if (parentId != null && parentId != 0L) {
                    formAuthViewBean = formBean.getAuthViewBeanById(parentId);
                }
                bindEvents = formAuthViewBean.getOperationBindEvent();
            } else {
                CtpAffair affair = affairManager.get(affairId);
                if (affair != null) {
                    long summaryId = affair.getObjectId();
                    bindEvents = FormService.getEventBindList(summaryId, affair.getFormOperationId());

                    //2017.02.14补丁打印日志
                    if (bindEvents==null){
                        StringBuilder sb = new StringBuilder("deeBindEvents is null-------------\n");
                        sb.append("affairId:"+affairId+"," );
                        sb.append("summaryId:"+summaryId+",");
                        sb.append("operationId:"+affair.getFormOperationId());
                        logger.error(sb.toString());
                        System.out.println(sb.toString());
                    }else {
                        StringBuilder sb = new StringBuilder("deeBindEvent is not null----------\n");
                        sb.append("affairId:"+affairId+"," );
                        sb.append("summaryId:"+summaryId+",");
                        sb.append("operationId:"+affair.getFormOperationId());
                        logger.error(sb.toString());
                        System.out.println(sb.toString());
                    }
                }
            }

            if (bindEvents != null) {
                for (Operation_BindEvent event : bindEvents) {
                    if (event != null) {
                        // 返回事件的任务类型
                        return event.getTaskType();
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    /**
     * DEE处理
     *
     * @param affairId       事件ID
     * @param attitude       态度，发送/撤销/回退...，在表单处理时为提交、已阅和拒绝的国际化
     * @param content        发送时，为操作ID，其他时候为提交意见
     * @param currentEventId 当前事件ID
     * @param skipConcurrent 跳过并发
     * @return {<p/>
     * hasNext:                     true or false<p/>
     * skipConcurrent:              true or false<p/>
     * currentEventId:              当前事件ID<p/>
     * blockInfoMsgType:            阻塞弹出框类型，error or info<p/>
     * blockInfoReason:             阻塞弹出框内容<p/>
     * blockFormWriteBackJson:      表单回填Json字符串<p/>
     * }
     */
    public Map<String, String> preDeeHandler(long affairId, String attitude,
                                             String content, String currentEventId,
                                             String skipConcurrent) {
        // 定义返回Map及初始值
        Map<String, String> retMap = new HashMap<String, String>();
        retMap.put(KEY_HASNEXT, Boolean.FALSE.toString());
        retMap.put(KEY_SKIPCONCURRENT, skipConcurrent);

        try {
            if (attitude.equalsIgnoreCase(ColEvent.start.name())) {
                // 获取表单主表数据
                FormDataMasterBean formDataMasterBean = formManager.getSessioMasterDataBean(affairId);
                // 获取表单Bean
                FormBean formBean = formCacheManager.getForm(formDataMasterBean.formTable.getFormId());
                Long operationId = Long.parseLong(content);
                FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(operationId);
                // 如果传入为子权限ID，则通过父权限ID查找开发高级中的Bean
                Long parentId = formAuthViewBean.getParentId();
                if (parentId != null && parentId != 0L) {
                    formAuthViewBean = formBean.getAuthViewBeanById(parentId);
                }
                // 获取绑定事件
                List<Operation_BindEvent> bindEvents = formAuthViewBean.getOperationBindEvent();
                List<InfoPath_DeeTask> deeTaskList = formAuthViewBean.getDEEBindEvent();

                List<Operation_BindEvent> resultBindEvents = getAfterBindEvents(bindEvents, ColEvent.start.name(), currentEventId, skipConcurrent);
                if (resultBindEvents.size() > 0) {
                    Operation_BindEvent event = resultBindEvents.get(0);
                    if (EventModel.block.name().equals(event.getModel())) {
                        InfoPath_DeeTask deeTask = getDeeTask(event, deeTaskList);
                        InfoPath_DeeParam deeParam = new InfoPath_DeeParam();
                        deeParam.setName("formFlow_Data");
                        deeParam.setValue(FlowFormUtil.getFlowFormDataString(collaborationApi, 
                        		formDataMasterBean.getId(), event.getOperationType()));
                        deeTask.getTaskParamList().add(deeParam);
                        // 执行阻塞任务
                        Map<String, String> executedMap = executeStartBlockDeeTask(deeTask, affairId, formDataMasterBean, formBean);
                        // 将执行后结果放入返回
                        retMap.putAll(executedMap);

                        if (Boolean.TRUE.toString().equals(skipConcurrent)) {
                            if (getAllBlockEvents(resultBindEvents).size() > 1) {
                                retMap.put(KEY_HASNEXT, Boolean.TRUE.toString());
                            }
                        } else if (Boolean.FALSE.toString().equals(skipConcurrent)) {
                            if (resultBindEvents.size() > 1) {
                                retMap.put(KEY_HASNEXT, Boolean.TRUE.toString());
                            }
                        }
                    } else if (EventModel.concurrent.name().equals(event.getModel())) {
                        String formData = createXMLStr(formBean, formDataMasterBean);

                        // 开启线程，执行并发任务
                        executeConcurrentDeeTask(getAllConcurrentEvents(resultBindEvents), affairId, affairId, formData);

                        if (getAllBlockEvents(resultBindEvents).size() > 0) {
                            retMap.put(KEY_HASNEXT, Boolean.TRUE.toString());
                        }
                        retMap.put(KEY_SKIPCONCURRENT, Boolean.TRUE.toString());
                    }
                    retMap.put(KEY_CURRENTEVENTID, event.getId());
                }
            } else {
                CtpAffair affair = affairManager.get(affairId);
                if (affair == null) {
                    return null;
                }
                long summaryId = affair.getObjectId();
                List<Operation_BindEvent> bindEvents = FormService.getEventBindList(summaryId, affair.getFormOperationId());
                long masterId = -1L;

                ColSummary summary = collaborationApi.getColSummary(summaryId);
                if (summary != null) {
                    masterId = summary.getFormRecordid();
                }

                putComentToFormCache(affair, content, attitude, summary);

                if (("collaboration.dealAttitude.haveRead").equals(attitude) ||
                        ("collaboration.dealAttitude.agree").equals(attitude) ||
                        ("collaboration.dealAttitude.disagree").equals(attitude)) {
                    attitude = ColEvent.submit.name();
                }if(("collaboration.dealAttitude.dealSaveWait").equals(attitude)){
                    attitude = "dealSaveWait";
                }

                List<Operation_BindEvent> resultBindEvents = getAfterBindEvents(bindEvents, attitude, currentEventId, skipConcurrent);
                if (resultBindEvents.size() > 0) {
                    Operation_BindEvent event = resultBindEvents.get(0);
                    if (EventModel.block.name().equals(event.getModel())) {
                        // 执行阻塞任务
                        Map<String, String> executedMap = executeOtherBlockDeeTask(event, masterId, summaryId);
                        // 将执行后结果放入返回
                        retMap.putAll(executedMap);

                        if (Boolean.TRUE.toString().equals(skipConcurrent)) {
                            if (getAllBlockEvents(resultBindEvents).size() > 1) {
                                retMap.put(KEY_HASNEXT, Boolean.TRUE.toString());
                            }
                        } else if (Boolean.FALSE.toString().equals(skipConcurrent)) {
                            if (resultBindEvents.size() > 1) {
                                retMap.put(KEY_HASNEXT, Boolean.TRUE.toString());
                            }
                        }
                    } else if (EventModel.concurrent.name().equals(event.getModel())) {
                        // 开启线程，执行并发任务
                        executeConcurrentDeeTask(getAllConcurrentEvents(resultBindEvents), affairId, masterId, null);

                        if (getAllConcurrentEvents(resultBindEvents).size() > 0) {
                            retMap.put(KEY_HASNEXT, Boolean.TRUE.toString());
                        }

                        retMap.put(KEY_SKIPCONCURRENT, Boolean.TRUE.toString());
                    }
                    retMap.put(KEY_CURRENTEVENTID, event.getId());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return retMap;
    }

    private void putComentToFormCache(CtpAffair affair, String content, String attitude, ColSummary summary) throws Exception {
        Comment comment = new Comment();
        comment.setAffairId(affair.getId());
        comment.setCreateId(affair.getMemberId());
        comment.setCtype(0);
        comment.setContent(content);
        comment.setCreateDate(new Date());
        comment.setExtAtt1(attitude);
        if (affair.getMemberId().equals(AppContext.getCurrentUser().getId())) {
            comment.setExtAtt2(AppContext.getCurrentUser().getId() + "");
        } else {
            comment.setExtAtt2(null);
        }

        formDataManager.getNewFlowDealOpitionValue(affair.getFormAppId(), summary, comment);
    }

    /**
     * 从DEE任务列表中取DEE任务
     *
     * @param bindEvent 事件
     * @param deeTasks  任务列表
     * @return 当前任务
     */
    private InfoPath_DeeTask getDeeTask(Operation_BindEvent bindEvent, List<InfoPath_DeeTask> deeTasks) {
        for (InfoPath_DeeTask deeTask : deeTasks) {
            if (bindEvent.getTaskId().equals(deeTask.getId())) {
                return deeTask;
            }
        }
        return null;
    }

    /**
     * 在事件列表中，截取指定事件之后的所有事件
     *
     * @param bindEvents     事件列表
     * @param operationType  操作类型
     * @param currentEventId 当前事件ID
     * @param skipConcurrent 是否拦截并发性任务，true：拦截，false：不拦截
     * @return 事件列表
     */
    private List<Operation_BindEvent> getAfterBindEvents(List<Operation_BindEvent> bindEvents,
                                                         String operationType,
                                                         String currentEventId, String skipConcurrent) {
        List<Operation_BindEvent> resultBindEvents = new ArrayList<Operation_BindEvent>();
        boolean flag = false;

        for (Operation_BindEvent event : bindEvents) {
            if (event == null) {
                continue;
            }

            // 取所有满足条件的
            if (currentEventId == null || "".equals(currentEventId)) {
                if (event.getOperationType().equalsIgnoreCase(operationType)) {
                    resultBindEvents.add(event);
                }
                continue;
            }

            // 找到当前事件
            if (event.getOperationType().equalsIgnoreCase(operationType) &&
                    currentEventId.equals(event.getId())) {
                flag = true;
                continue;
            }
            if (flag && event.getOperationType().equalsIgnoreCase(operationType)) {
                if ("true".equals(skipConcurrent) &&
                        EventModel.concurrent.name().equals(event.getModel())) {
                    continue;
                }
                resultBindEvents.add(event);
            }
        }
        return resultBindEvents;
    }

    /**
     * 筛选阻塞事件
     *
     * @param bindEvents 筛选前
     * @return 筛选后
     */
    private List<Operation_BindEvent> getAllBlockEvents(List<Operation_BindEvent> bindEvents) {
        return getAllEventsByEventModel(bindEvents, EventModel.block.name());
    }

    /**
     * 筛选并发事件
     *
     * @param bindEvents 筛选前
     * @return 筛选后
     */
    private List<Operation_BindEvent> getAllConcurrentEvents(List<Operation_BindEvent> bindEvents) {
        return getAllEventsByEventModel(bindEvents, EventModel.concurrent.name());
    }

    /**
     * 筛选指定类型的事件
     *
     * @param bindEvents 筛选前
     * @param eventModel 事件类型，block or concurrent
     * @return 筛选后
     */
    private List<Operation_BindEvent> getAllEventsByEventModel(List<Operation_BindEvent> bindEvents, String eventModel) {
        List<Operation_BindEvent> resultBindEvents = new ArrayList<Operation_BindEvent>();

        for (Operation_BindEvent event : bindEvents) {
            // 当前事件为指定类型的事件
            if (eventModel.equals(event.getModel())) {
                resultBindEvents.add(event);
            }
        }

        return resultBindEvents;
    }

    /**
     * 执行《发起》的《阻塞》DEE任务
     *
     * @param deeTask            DEE任务
     * @param masterId           masterId，表单数据ID
     * @param formDataMasterBean 表单数据
     * @param formBean           表单Bean
     * @return 任务结果
     * @throws Exception
     */
    private Map<String, String> executeStartBlockDeeTask(InfoPath_DeeTask deeTask,
                                                         long masterId, FormDataMasterBean formDataMasterBean,
                                                         FormBean formBean) throws Exception {
        // 将表单对象解析成xml
        String formData = createXMLStr(formBean, formDataMasterBean);
        return executeBlockDeeTask(deeTask, formData, masterId, ColEvent.start.name());
    }

    /**
     * 执行《非发起》的《阻塞》DEE任务
     *
     * @param event        事件
     * @param masterId     masterId，表单数据ID
     * @param colSummaryId colSummaryId
     * @return 任务结果
     */
    private Map<String, String> executeOtherBlockDeeTask(Operation_BindEvent event,
                                                         long masterId, long colSummaryId) {
        InfoPath_DeeTask deeTask = event.getDeeTask();

        try {
            ColSummary colSummary = collaborationApi.getColSummary(colSummaryId);
            FormBean formBean = formCacheManager.getForm(Long.valueOf(deeTask.getFormAppId()));
            FormDataMasterBean formDataMasterBean = formManager.getSessioMasterDataBean(colSummary.getFormRecordid());

            // 将表单对象解析成xml
            String formData = createXMLStr(formBean, formDataMasterBean);
            InfoPath_DeeParam deeParam = new InfoPath_DeeParam();
            deeParam.setName("formFlow_Data");
            deeParam.setValue(FlowFormUtil.getFlowFormDataString(collaborationApi, 
            		formDataMasterBean.getId(), event.getOperationType()));
            deeTask.getTaskParamList().add(deeParam);
            return executeBlockDeeTask(deeTask, formData, masterId, null);
        } catch (BusinessException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            logger.error("DEE异常：" + e.getLocalizedMessage(), e);
        }

        return null;
    }

    /**
     * 执行阻塞DEE任务
     *
     * @param deeTask  DEE任务
     * @param formData 表单数据
     * @param masterId masterId，表单数据ID
     * @param attitude 发送、撤销...
     * @return 任务结果
     */
    private Map<String, String> executeBlockDeeTask(InfoPath_DeeTask deeTask, String formData, long masterId, String attitude) {
        Map<String, String> retMap = new HashMap<String, String>();

        Document beforeDocument = null;
        try {
            beforeDocument = DocumentHelper.parseText(formData);
        } catch (DocumentException e) {
            logger.error(e.getLocalizedMessage(), e);
            return retMap;
        }

        Parameters deeParams = new Parameters();
        processDEEParamsValue(beforeDocument, deeTask, deeParams, deeTask.getTaskParamList());

        // 传递表单数据
        deeParams.add("FormData", formData);
        // 传递masterId
        deeParams.add("masterId", masterId);
        // 传递发送方式
        if (ColEvent.start.name().equalsIgnoreCase(attitude)) {
            deeParams.add("a8DeeAttitude", ColEvent.start.name());
        }
        // 传递操作方式“阻塞”
        // deeParams.add("a8DeeOperationMode", "block");

//        logger.info("DEE 发送前调试信息: " + formData);
        com.seeyon.v3x.dee.Document deeReturnDocument = null;
        try {
        	String strMap = (String) deeParams.getValue("formFlow_Data");
        	JSONObject jStr = JSONObject.fromObject(strMap);
        	Map<String, String> formFlow_Data = jStr;
        	deeParams.add("formFlow_Data", formFlow_Data);
            // 执行DEE任务
            deeReturnDocument = new DEEClient().execute(deeTask.getId(), deeParams);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            retMap.put(BLOCK_EXCEPTION_INFO, e.toString());
            return retMap;
        }
//        logger.info("DEE 调试信息: " + deeReturnDocument.toString());

        Object obj = AppContext.getThreadContext("resultMap");
        if (obj != null) {
            DataContainer dc = new DataContainer();
            dc.add(FormConstant.SUCCESS, "true");
            dc.put(FormConstant.RESULTS, obj);
            retMap.put(BLOCK_FORM_WRITE_BACK_JSON, dc.getJson());
//          return retMap;
        }


        String deeStatus = null;
        String dialogIsPop = null;
        String reason = null;
        try {
            Document document = DocumentHelper.parseText(deeReturnDocument.toString());
            Element rootElement = document.getRootElement();
            Element element = rootElement.element("deeblockrtn");
            Element row = element.element("row");
            deeStatus = row.element("deestatus").getText();
            dialogIsPop = row.element("dialogispop").getText();
            reason = row.element("reason").getText();
        } catch (Exception e) {
            logger.info(e.getLocalizedMessage(), e);
            return retMap;
        }

        if ("F".equalsIgnoreCase(deeStatus)) {
            retMap.put(BLOCK_INFO_REASON, reason);
            retMap.put(BLOCK_INFO_MSG_TYPE, BLOCK_INFO_MSG_TYPE_ERROR);
        } else if ("Y".equalsIgnoreCase(dialogIsPop)) {
            retMap.put(BLOCK_INFO_REASON, reason);
            retMap.put(BLOCK_INFO_MSG_TYPE, BLOCK_INFO_MSG_TYPE_INFO);
        }
        return retMap;
    }

    /**
     * 执行并发DEE任务
     *
     * @param bindEvents 事件列表
     * @param affairId   affairId
     * @param masterId   masterId，表单数据ID
     * @param formData   表单数据
     */
    private void executeConcurrentDeeTask(final List<Operation_BindEvent> bindEvents,
                                          final long affairId,
                                          final long masterId,
                                          final String formData) {
        /*
         * AppContext的线程上下文是使用ThreadLocal来存储的，
         * 而ThreadLocal是线程局部变量，线程独占的，故这儿需要在线程间传递参数。
         */
        final User currentUser = AppContext.getCurrentUser();
        final String formDataKey = currentUser.getId() + FormConstant.DOWNLINE + affairId;
        final Object masterData = AppContext.getSessionContext(formDataKey);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
					Thread.sleep(5*1000);
				} catch (InterruptedException e1) {
					 
				}
                AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, currentUser);
                AppContext.putThreadContext(formDataKey, masterData);

                for (Operation_BindEvent event : bindEvents) {
                    Parameters params = new Parameters();
                    if (formData != null) {
                        // 传递表单数据
                        params.add("FormData", formData);
                    }
                    params.add("masterId", masterId);
                    
					try {
						Map<String, String> formFlow_Data = FlowFormUtil.getFlowFormData(collaborationApi,
								masterId, event.getOperationType());
	        			params.add("formFlow_Data", formFlow_Data);
					} catch (Exception e1) {
						logger.error("获取formFlow_Data异常：" + e1.getMessage());
					}
                    try {
                        com.seeyon.v3x.dee.Document deeDoc = new DEEClient().execute(event.getTaskId(), params);

                        if (deeDoc == null) {
                            logger.warn("节点事件触发DEE任务失败：" + event.getTaskId());
                        }
                        if (logger.isDebugEnabled()) {
                            if (deeDoc != null) {
                                logger.debug(deeDoc.toString());
                            }
                        }
                    } catch (TransformException e) {
                        logger.error(e.getMessage(), e);
                    } catch (ClassNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    } catch (InvocationTargetException e) {
                        logger.error(e.getMessage(), e);
                    } catch (NoSuchMethodException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IllegalAccessException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });

        thread.start();
    }

    /**
     * 提交前,终止前
     *
     * @return String[]  数组下标[0]值为1代表扩展类，数组下标[0]值2代表DEE，有返回代表审核失败，返回null代表正常
     */
    public String[] preHandler(long affairId, String attitude, String content) {
        try {
            List<Operation_BindEvent> formEventBind;
            long colSummaryId = -1L;
            FormDataMasterBean formDataMasterBean = null;
            FormBean formBean = null;
            if (attitude.equalsIgnoreCase(ColEvent.start.name())) {
                //表单主表数据id
                formDataMasterBean = formManager.getSessioMasterDataBean(affairId);
                formBean = formCacheManager.getForm(formDataMasterBean.formTable.getFormId());
                //操作权限id
                Long operationId = Long.valueOf(content);
                FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(operationId);
                formEventBind = formAuthViewBean.getOperationBindEvent();
            } else {
                CtpAffair affair = affairManager.get(affairId);
                if (affair == null) {
                    return null;
                }
                colSummaryId = affair.getObjectId();
                formEventBind = FormService.getEventBindList(colSummaryId, affair.getFormOperationId());
            }

            if (formEventBind == null) {
                return null;
            }

            for (Operation_BindEvent event : formEventBind) {
                if (event == null) {
                    continue;
                }
                CollaborationEventTask task = null;
                if (event.getTaskType().equals(TaskType.ext.name())) {
                    if (collaborationEvent.isEmpty()) {
                        this.init();
                    }
                    if (CollectionUtils.isEmpty(collaborationEvent.values())) {
                        return null;
                    }
                    task = collaborationEvent.get(event.getTaskName());
                    if (task == null) {
                        continue;
                    }
                }
                String[] tempResult;
                if (event.getOperationType().equalsIgnoreCase(ColEvent.start.name())) {
                    tempResult = operatingReturnStart(attitude, formDataMasterBean, formBean, event, task);
                } else {
                    tempResult = operatingReturn(attitude, content, colSummaryId, event, task);
                }
                if (tempResult != null) {
                    return tempResult;
                }
            }
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }

        return null;
    }

    /**
     * 具体操作
     *
     * @param attitude
     * @param content
     * @param colSummaryId
     * @param event
     * @param task
     * @return
     * @throws BusinessException
     */
    private String[] operatingReturn(final String attitude, final String content,
                                     final long colSummaryId, Operation_BindEvent event,
                                     final CollaborationEventTask task) throws BusinessException {
        if (event.getOperationType().equalsIgnoreCase(ColEvent.submit.name())) {
            if (("collaboration.dealAttitude.haveRead").equals(attitude) ||
                    ("collaboration.dealAttitude.agree").equals(attitude) ||
                    ("collaboration.dealAttitude.disagree").equals(attitude)) {
                try {
                    if (!EventModel.block.name().equals(event.getModel())) {
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    task.onSubmit(colSummaryId, attitude, content);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        });
                        thread1.start();
                    } else {
                        return task.onBeforeSubmit(colSummaryId, attitude, content);
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        } else if (event.getOperationType().equalsIgnoreCase(ColEvent.stepstop.name())) {
            if (attitude.equals(ColEvent.stepstop.name())) {
                try {
                    if (!EventModel.block.name().equals(event.getModel())) {
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    task.onStop(colSummaryId, attitude, content);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        });
                        thread1.start();
                    } else {
                        return task.onBeforeStop(colSummaryId, attitude, content);
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        } else if (event.getOperationType().equalsIgnoreCase(ColEvent.stepBack.name())) {
            if (attitude.equals(ColEvent.stepBack.name())) {
                try {
                    if (!EventModel.block.name().equals(event.getModel())) {
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    task.onStepBack(colSummaryId, attitude, content);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        });
                        thread1.start();
                    } else {
                        return task.onBeforeStepBack(colSummaryId, attitude, content);
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        } else if (event.getOperationType().equalsIgnoreCase(ColEvent.repeal.name())) {
            if (attitude.equals(ColEvent.repeal.name())) {
                try {
                    if (!EventModel.block.name().equals(event.getModel())) {
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    task.onCancel(colSummaryId, attitude, content);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        });
                        thread1.start();
                    } else {
                        return task.onBeforeCancel(colSummaryId, attitude, content);
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        } else if (event.getOperationType().equalsIgnoreCase(ColEvent.takeback.name())) {
            if (attitude.equals(ColEvent.takeback.name())) {
                try {
                    if (!EventModel.block.name().equals(event.getModel())) {
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    task.onCancel(colSummaryId, attitude, content);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        });
                        thread1.start();
                    } else {
                        return task.onBeforeTakeback(colSummaryId, attitude, content);
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * 功能：根据表单对象解析成xml
     *
     * @param formBean
     * @param masterData
     * @return
     */
    public String createXMLStr(FormBean formBean, FormDataMasterBean masterData) {
        StringBuffer xmlsb = new StringBuffer();
        String formName = formBean.getFormName();
        xmlsb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n");
        xmlsb.append("<root formName='" + formName + "'> \r\n");
        Map<String, List<String>> mapSubTable = new HashMap<String, List<String>>();
        if (masterData != null) {
            // 处理主表
            StringBuffer rowString = new StringBuffer();
            FormTableBean masterTable = formBean.getMasterTableBean();
            Map<String, Object> mainData = masterData.getRowData();
            rowString.append(com.seeyon.ctp.form.util.StringUtils.space(4) + "<row> \r\n");
            for (String key : mainData.keySet()) {
                String value = "";
                Object objVal = mainData.get(key);
                FormFieldBean fb = formBean.getFieldBeanByName(key);
                if (objVal != null){
                    value = fb==null?objVal.toString():TransDateUtil.format2DEE(fb.getFieldType(),objVal);
                }
                String display = "";
                if (fb != null){
                    display = " display='" + (fb.getDisplay() == null ? "" : fb.getDisplay()) + "'";
                }
                rowString.append(com.seeyon.ctp.form.util.StringUtils.space(6) + "<" + key + display + ">" + com.seeyon.ctp.form.util.StringUtils.Java2XMLStr(value) + "</" + key + "> \r\n");
            }
            rowString.append(com.seeyon.ctp.form.util.StringUtils.space(4) + "</row> \r\n");
            xmlsb.append(com.seeyon.ctp.form.util.StringUtils.space(2) + "<" + masterTable.getTableName() + "> \r\n");
            xmlsb.append(rowString);
            xmlsb.append(com.seeyon.ctp.form.util.StringUtils.space(2) + "</" + masterTable.getTableName() + "> \r\n");
            rowString.setLength(0);

            // 处理重复表

            Map<String, List<FormDataSubBean>> subDataBeans = masterData.getSubTables();
            Iterator<String> it = subDataBeans.keySet().iterator();
            while (it.hasNext()) {
                List<FormDataSubBean> subBeans = subDataBeans.get(it.next());

                for (FormDataSubBean subBean : subBeans) {
                    FormTableBean subTableBean = subBean.getFormTable();
                    Map<String, Object> rows = subBean.getRowData();
                    for (String key : rows.keySet()) {
                        String value = "";
                        Object objVal = rows.get(key);
                        FormFieldBean fbSub = formBean.getFieldBeanByName(key);
                        if (objVal != null){
                            value = fbSub==null?objVal.toString():TransDateUtil.format2DEE(fbSub.getFieldType(),objVal);
                        }
                        String display = "";
                        if (fbSub != null){
                            display = " display='" + (fbSub.getDisplay() == null ? "" : fbSub.getDisplay()) + "'";
                        }
                        rowString.append(com.seeyon.ctp.form.util.StringUtils.space(6) + "<" + key + display + ">" + com.seeyon.ctp.form.util.StringUtils.Java2XMLStr(value) + "</" + key + "> \r\n");
                    }
                    List<String> subTable = mapSubTable.get(subTableBean.getTableName());
                    if (subTable == null) {
                        List<String> list = new ArrayList<String>();
                        list.add(rowString.toString());
                        mapSubTable.put(subTableBean.getTableName(), list);
                    } else {
                        subTable.add(rowString.toString());
                        mapSubTable.remove(subTableBean.getTableName());
                        mapSubTable.put(subTableBean.getTableName(), subTable);
                    }
                    rowString.setLength(0);
                }
            }
        }
        //合并子表数据
        for (String key : mapSubTable.keySet()) {
            List<String> rowList = mapSubTable.get(key);
            String display = "";
            if (formBean.getFieldBeanByName(key) != null) {
                display = " display='" + (formBean.getFieldBeanByName(key).getDisplay() == null ? "" : formBean.getFieldBeanByName(key).getDisplay()) + "'";
            }
            xmlsb.append(com.seeyon.ctp.form.util.StringUtils.space(2) + "<" + key + display + "> \r\n");
            for (String rowString : rowList) {
                xmlsb.append(com.seeyon.ctp.form.util.StringUtils.space(4) + "<row> \r\n");
                xmlsb.append(rowString);
                xmlsb.append(com.seeyon.ctp.form.util.StringUtils.space(4) + "</row> \r\n");
            }
            xmlsb.append(com.seeyon.ctp.form.util.StringUtils.space(2) + "</" + key + "> \r\n");
        }
        xmlsb.append("</root> \r\n");
        logger.info(xmlsb.toString());
        return xmlsb.toString();
    }




    public String startFormDevelopmentOfAdvCheck(Map params) {
        String returnString = "";
        Long contentDataId = Long.valueOf(params.get("contentDataId").toString());
//        String moduleId=(String) params.get("moduleId");
//        String moduleTemplateId=(String) params.get("moduleTemplateId");
//        String formAppId=(String) params.get("formAppId");
        String operationId = (String) params.get("operationId");
        logger.info("开发高级--发起事件");
        logger.info("开发高级--发起事件contentDataId：" + contentDataId);
        logger.info("开发高级--发起事件operationId：" + operationId);
        FormDataMasterBean formDataMasterBean = formManager.getSessioMasterDataBean(contentDataId);
        FormBean formBean = formCacheManager.getForm(formDataMasterBean.formTable.getFormId());
        try {
            FormAuthViewBean formAuthViewBean = formBean.getAuthViewBeanById(Long.valueOf(operationId));
            List<Operation_BindEvent> formEentBind = formAuthViewBean.getOperationBindEvent();
            if (formEentBind == null) {
                return returnString;
            }

            for (Operation_BindEvent event : formEentBind) {
                if (event == null) {
                    continue;
                }
                if (!ColEvent.start.name().equalsIgnoreCase(event.getOperationType())) {
                    continue;
                }
                //扩展控件
                if (event.getTaskType().equals(TaskType.ext.name())) {
                    if (collaborationEvent.isEmpty()) {
                        this.init();
                    }
                    String formData = createXMLStr(formBean, formDataMasterBean);//组建form数据为xml
                    logger.info("开发高级--发起事件触发模式：" + event.getModel());

                    if (CollectionUtils.isEmpty(collaborationEvent.values())) {
                        continue;
                    }
                    //获取第三方任务
                    CollaborationEventTask task = collaborationEvent.get(event.getTaskName());
                    if (task == null) {
                        continue;
                    }
                    if (event.getModel().equals(EventModel.block.name())) {
//                        return task.onBeforeStart(formData);
                    }
                    if (event.getModel().equals(EventModel.concurrent.name())) {
                        this.excuteStartFormStartTask(task, formData);
                        continue;
                    }
                }
                //DEE控件
                if (event.getTaskType().equals(TaskType.dee.name())) {
                    List<InfoPath_DeeTask> deeTaskList = formAuthViewBean.getDEEBindEvent();
                    final String formData = createXMLStr(formBean, formDataMasterBean);//组建form数据为xml
                    if (event.getModel().equals(EventModel.block.name())) {
                        if (deeTaskList == null) {
                            return ResourceUtil.getString("form.operhigh.start.error.deeTask");
                        }
                        DEEClient client = new DEEClient();
                        XMLDataSource xml = new XMLDataSource(formData);
                        Document document = DocumentHelper.parseText(xml.parse().toString());

                        for (InfoPath_DeeTask deeTask : deeTaskList) {
                            if (deeTask.getId().equals(event.getTaskId())) {
                                Parameters deeParams = new Parameters();
                                List<InfoPath_DeeParam> deeParamList = deeTask.getTaskParamList();
                                processDEEParamsValue(document, deeTask, deeParams, deeParamList);
                                deeParams.add("FormData", formData);//整体表单数据传递
                                com.seeyon.v3x.dee.Document deeDocument = client.execute(deeTask.getId(), deeParams);
                                return (String) deeDocument.getContext().getAttribute("deeblockrtn");
                            }
                        }
                    }
                    if (event.getModel().equals(EventModel.concurrent.name())) {
                        //并发条件下执行DEE
//                            excuteDeeStartTask(event.getTaskId(),formData);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            returnString = ResourceUtil.getString("form.operhigh.start.error.AllTask");
        }
        return returnString;
    }

    private void processDEEParamsValue(Document document, InfoPath_DeeTask deeTask, Parameters deeParams,
                                       List<InfoPath_DeeParam> deeParamList) {
        for (InfoPath_DeeParam deeParam : deeParamList) {
            String deeParamValue = "";
            List listNode = document.selectNodes("//*[@display='" + deeParam.getValue() + "']");
            //等于0--DEE参数不是从表单中选的字段，是输入的默认值
            if (listNode.size() == 0) {
                deeParamValue = deeParam.getValue();
            }
            //等于1--DEE参数是从表单中选的字段而且是主表字段
            if (listNode.size() == 1) {
                Iterator iter = listNode.iterator();
                while (iter.hasNext()) {
                    Element valueElement = (Element) iter.next();
                    deeParamValue = valueElement.getText();
                }
            }
            //>1的情况是子表字段，暂时不赋值
            deeParams.add(deeParam.getName(), deeParamValue);
            logger.info("DEE任务" + deeTask.getName() + ":参数" + deeParam.getName() + "::值" + deeParamValue);
        }
    }

    /**
     * 功能：开发高级--第三方扩展--并发模式
     *
     * @param task
     * @param formData
     * @throws TransformException
     */
    private void excuteStartFormStartTask(final CollaborationEventTask task, final String formData) throws TransformException {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    task.onBeforeStart(formData);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        thread1.start();
    }

    /**
     * 触发表单节点中设置的DEE任务
     *
     * @param event
     * @throws TransformException
     */
    private void excuteDeeTask(final Operation_BindEvent event, final ColSummary summary) throws TransformException {
        Thread thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                DEEClient client = new DEEClient();
                Parameters params = new Parameters();
                long masterId = summary.getFormRecordid();
                params.add("masterId", masterId);
				try {
					Map<String, String> formFlow_Data = FlowFormUtil.getFlowFormData(
							summary.getSubject(), Long.toString(summary.getId()), event.getOperationType());
	    			params.add("formFlow_Data", formFlow_Data);
				} catch (Exception e1) {
					logger.error("获取formFlow_Data异常：" + e1.getMessage());
				}
                if (StringUtils.isBlank(event.getTaskId())) {
                    logger.info("表单节点中设置的DEE任务为空" + event.getName());
                }
                try {
                    com.seeyon.v3x.dee.Document deeDoc = client.execute(event.getTaskId(), params);

                    if (deeDoc == null) {
                        logger.warn("节点事件触发DEE任务失败" + event.getName());
                    }
                    if (logger.isDebugEnabled()) {
                        if (deeDoc != null) {
                            String deeResultXml = deeDoc.toString();
                            logger.debug(deeResultXml);
                        }
                    }
                } catch (TransformException e) {
                    logger.error(e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                } catch (NoSuchMethodException e) {
                    logger.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }

            }
        });
        thread1.start();
    }

    /**
     * 发送事件专用
     *
     * @param attitude
     * @param formDataMasterBean
     * @param formBean
     * @param event
     * @param task
     * @return
     * @throws Exception
     */
    private String[] operatingReturnStart(final String attitude, FormDataMasterBean formDataMasterBean,
                                          FormBean formBean, Operation_BindEvent event,
                                          final CollaborationEventTask task) throws Exception {
        if (attitude.equals(ColEvent.start.name())) {
            if (formDataMasterBean == null) {
                return null;
            }
            if (formBean == null) {
                return null;
            }
            final String formData = createXMLStr(formBean, formDataMasterBean);
            if (!EventModel.block.name().equals(event.getModel())) {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.onBeforeStart(formData);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                });
                thread1.start();
            } else
                return task.onBeforeStart(formData);
        }

        return null;
    }

    public String[] afterHandler() {
        return null;
    }

    enum ColEvent {
        /**
         * 发起操作
         */
        start,
        /**
         * 提交操作
         */
        submit,
        /**
         * 终止操作
         */
        stepstop,
        /**
         * 回退操作
         */
        stepBack,
        /**
         * 取消操作
         */
        repeal,
        /**
         * 取回操作
         */
        takeback,
    }

    enum ColHandler {
        //提交前事件
        beforeTriger,
        //提交后事件
        afterTriger,
    }

    enum EventModel {
        /**
         * 阻塞式
         */
        block,
        /**
         * 并发式
         */
        concurrent,
    }

    enum TaskType {
        /**
         * 扩展类
         */
        ext,
        dee,
    }

    class SortEvent implements Comparable<SortEvent> {
        private CollaborationEventTask collaborationBeforeEvent;

        public SortEvent(CollaborationEventTask collaborationBeforeEvent) {
            this.collaborationBeforeEvent = collaborationBeforeEvent;
        }

        public CollaborationEventTask getEvent() {
            return collaborationBeforeEvent;
        }

        @Override
        public int compareTo(SortEvent o) {
            return new Integer(this.collaborationBeforeEvent.sort()).compareTo(o.getEvent().sort());
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

}

