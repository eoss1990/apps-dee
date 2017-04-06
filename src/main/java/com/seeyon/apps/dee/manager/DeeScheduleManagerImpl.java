package com.seeyon.apps.dee.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.dee.TransformException;
import com.seeyon.v3x.dee.client.service.DEEConfigService;
import com.seeyon.v3x.dee.common.db.schedule.model.ScheduleBean;

public class DeeScheduleManagerImpl implements DeeScheduleManager {
    /**
     * DEE实例化
     */
    private static final DEEConfigService configService = DEEConfigService.getInstance();

    @Override
    public ScheduleBean findById(String id) throws TransformException {
        // TODO Auto-generated method stub
        return configService.getScheduleByFlowId(id);
    }

    @Override
    public void update(ScheduleBean sdBean) throws TransformException {
        // TODO Auto-generated method stub
        configService.updateSchedule(sdBean);
    }

    @Override
    public void deleteSchedule(String[] ids) throws TransformException {
        // TODO Auto-generated method stub

    }

    @Override
    public FlipInfo findScheduleList(FlipInfo fi, Map<String, Object> param) {
        String dis_name = "";
        String flow_name = "";
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            if (entry.getKey().equals("dis_name")) {
                Object tmp = entry.getValue();
                dis_name = tmp == null ? "" : tmp.toString();
            }
            if (entry.getKey().equals("flow_name")) {
                Object tmp = entry.getValue();
                flow_name = tmp == null ? "" : tmp.toString();
            }
        }

        List<ScheduleBean> datas = new ArrayList<ScheduleBean>();
        List<ScheduleBean> result = configService.getAllScheduleList();
        List<ScheduleBean> resultList = new ArrayList<ScheduleBean>();


        for (ScheduleBean scheduleBean : result) {
            if ((scheduleBean.getDis_name() == null ? "" : scheduleBean.getDis_name()).contains(dis_name) &&
                    (scheduleBean.getFlow_name() == null ? "" : scheduleBean.getFlow_name()).contains(flow_name)) {
                resultList.add(scheduleBean);
            }
        }
        for (int i = 0; i < resultList.size(); i++) {
            //这里进行内存分页
            if (i >= (fi.getPage() - 1) * fi.getSize() && i < fi.getPage() * fi.getSize()) {
                datas.add(resultList.get(i));
            }
        }
        fi.setData(datas);
        fi.setTotal(resultList.size());
        return fi;
    }
}
