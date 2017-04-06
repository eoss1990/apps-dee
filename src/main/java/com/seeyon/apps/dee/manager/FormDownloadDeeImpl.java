package com.seeyon.apps.dee.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.Operation_BindEvent;
import com.seeyon.ctp.form.modules.engin.manager.FormBaseDownloadManagerImpl;
import com.seeyon.ctp.form.util.Enums;
import com.seeyon.ctp.form.util.FormCharset;
import com.seeyon.ctp.util.ZipUtil;
import com.seeyon.v3x.dee.common.exportflow.ExportFlow;
import com.seeyon.v3x.dee.common.hotdeploy.HotDeploy;
import com.seeyon.v3x.dee.util.DesUtil;
import org.apache.commons.logging.Log;
import org.dom4j.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 表单pak 导入导出 dee实现
 * Created by daiy on 2016-1-19.
 */
public class FormDownloadDeeImpl extends FormBaseDownloadManagerImpl {

    private static final Log logger = CtpLogFactory.getLog(FormDownloadDeeImpl.class);

    @Override
    public void onSingleFormDownload(FormBean formBean, File baseFolder, List<File> baseFileList, File folder, List<File> fileList) throws BusinessException {
        super.onSingleFormDownload(formBean, baseFolder, baseFileList, folder, fileList);
    }

    @Override
    public void onAllFileDownload(File baseFolder, List<File> baseFileList, List<FormBean> formBeenList) throws BusinessException {
        Map<String, String> flowMap = new HashMap<String, String>();
        for (FormBean formBean : formBeenList) {
            getDeeFlowIdByFormBean(formBean, flowMap);
        }
        File deeFile = null;
        try {
            deeFile = exportDeeFlow(baseFolder.getAbsolutePath(),flowMap);
        } catch (Exception e) {
            logger.error("导出dee任务包时异常：", e);
        }
        if(deeFile != null) {
            baseFileList.add(deeFile);
        }
    }

    /**
     * 获取单个表单模板的关联DEE任务ID
     *
     * @param fb
     * @param flowMap
     * @return
     */
    private void getDeeFlowIdByFormBean(FormBean fb,Map<String,String> flowMap){
        //扩展控件
        for(FormFieldBean ffb:fb.getAllFieldBeans()){
            if(ffb == null) continue;
            if(ffb.getDeeTask() != null && ffb.getDeeTask().getId() != null && !"".equals(ffb.getDeeTask().getId())){
                flowMap.put(ffb.getDeeTask().getId(), "");
            }
        }
        //开发高级
        for(FormAuthViewBean favb:fb.getAllFormAuthViewBeans()){
            if(favb == null) continue;
            for(Operation_BindEvent e:favb.getOperationBindEvent()){
                if(e == null) continue;
                if(!"".equals(e.getTaskId())){
                    flowMap.put(e.getTaskId(), "");
                }
                else if(e.getDeeTask() != null && e.getDeeTask().getId() != null && !"".equals(e.getDeeTask().getId())){
                    flowMap.put(e.getDeeTask().getId(), "");
                }
            }
        }
    }

    /**
     * 导出表单关联DEE任务
     *
     * @param flowMap
     * @return
     * @throws Exception
     */
    private File exportDeeFlow(String baseFolder,Map<String,String> flowMap) throws Exception{
        String flowIds = "";
        // 获取Dee folwid拼接字符串
        for(Map.Entry<String, String> entry : flowMap.entrySet()){
            if("".equals(entry.getKey()))
                continue;
            flowIds += entry.getKey() + ",";
        }
        if(!"".equals(flowIds)){
            flowIds = flowIds.substring(0, flowIds.length()-1);
        }
        if("".equals(flowIds)) return null;
        ExportFlow ef = new ExportFlow();
        String xmlFlow = ef.doReader(flowIds).toString();
        if("".equals(xmlFlow)) return null;
        // 配置文件打包
        OutputStream fout = null;
        PrintStream writer = null;
        File deeFile = new File(baseFolder, "dee.xml");
        try {
            fout = new FileOutputStream(deeFile);
            writer = new PrintStream(fout, false, FormCharset.getInstance().getJDKFile());
            writer.print(xmlFlow);
            writer.flush();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(),e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
        } finally {
            if (writer != null)
                writer.close();
            if (fout != null)
                fout.close();
        }
        // 导出打包
        List<File> deeFileList = new ArrayList<File>();
        deeFileList.add(deeFile);
        File zipFile = new File(baseFolder, "dee.drp");
        ZipUtil.zip(deeFileList, zipFile);
        // 删除中间文件
        for (File file : deeFileList) {
            file.delete();
        }
        DesUtil desUtil = new DesUtil("drp_encrypt");
        File newZipFile = new File(baseFolder, "encryptdee.drp");
        desUtil.encryptFile(zipFile.getAbsolutePath(),newZipFile.getAbsolutePath());
        //删除压缩drp
        zipFile.delete();
        return newZipFile;
    }

    @Override
    public void onStartImport(File baseFolder) throws BusinessException {
        new HotDeploy(baseFolder.getPath(), Pattern.compile("(?:.+\\.drp)"));
    }

    @Override
    public void onSingleFormImport(FormBean formBean, Element paramXml, File baseFolder, File currentFormFolder) throws BusinessException {
        super.onSingleFormImport(formBean, paramXml, baseFolder, currentFormFolder);
    }

    @Override
    public void onAllFormImport(Element paramXml, File baseFolder) throws BusinessException {
        super.onAllFormImport(paramXml, baseFolder);
    }

    @Override
    public Integer getSort() {
        return 1;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canUse4FormType(Enums.FormType formType) {
        return true;
    }
}
