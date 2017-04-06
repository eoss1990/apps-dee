package com.seeyon.apps.dee.util;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.form.util.Enums;
import com.seeyon.ctp.util.Datetimes;

import java.util.Date;

/**
 * Created by Administrator on 2016-6-15.
 * 时间日期等转换
 */
public class TransDateUtil {
//    private final static Log log = LogFactory.getLog(TransDateUtil.class);

    /**
     * 格式化表单数据
     * @param fType
     * @param fValue
     * @return 格式化结果
     * @throws BusinessException
     */
    public static String format2DEE(String fType,Object fValue){
        String retData = "";
        if (fValue == null) return retData;
        Enums.FieldType fieldType = Enums.FieldType.getEnumByKey(fType.toUpperCase());
        switch(fieldType){
            case TIMESTAMP:
                retData = date2Str(fValue,"yyyy-MM-dd");
                break;
            case DATETIME:
                retData = date2Str(fValue,"yyyy-MM-dd HH:mm");
                break;
            default:
                retData = fValue.toString();
                break;
        }
        return retData;
    }


    /**
     * 将表单传入的参数，如果是时间类型，转换为字符串
     * @param dt
     * @param pattern
     * @return
     */
    public static String date2Str(Object dt,String pattern){
        String retData = "";
        if (dt == null) return retData;
        try{

//            java.text.SimpleDateFormat sf = new java.text.SimpleDateFormat(pattern);
            if(dt instanceof Date){
//                retData = sf.format(dt);
                retData = Datetimes.format((Date) dt,pattern);
            }
            else if(dt instanceof String){
//                java.text.SimpleDateFormat sfe = new java.text.SimpleDateFormat("EEE MMM DD HH:mm:ss z yyyy", Locale.ENGLISH);
                Date newDt = Datetimes.parse((String) dt,"EEE MMM DD HH:mm:ss z yyyy");
                retData = Datetimes.format(newDt,pattern);
            }
            else {
               retData = dt.toString();
            }
        } catch(Exception e){
            retData = dt.toString();
        }
        return retData;
    }

}
