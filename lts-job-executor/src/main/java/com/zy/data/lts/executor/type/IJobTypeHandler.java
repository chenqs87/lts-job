package com.zy.data.lts.executor.type;

import com.google.gson.Gson;
import com.zy.data.lts.executor.model.JobExecuteEvent;

import java.util.Collections;
import java.util.Map;

/**
 * @author chenqingsong
 * @date 2019/5/23 11:13
 */
public interface IJobTypeHandler {
    /**
     * java runtime process 执行的命令
     * @param event
     * @return
     */
    String[] createCommand(JobExecuteEvent event);

    /**
     * 解析params　参数，如果该参数时json格式，则添加到环境变量中
     * @param event
     * @return
     */
    default Map<String, Object> getEnv(JobExecuteEvent event) {
        Map<String, Object> ret =  Collections.emptyMap();
        if(event.getParams() == null) {
            return ret;
        }

        try {
            Gson gson = new Gson();
            return gson.fromJson(event.getParams(), Map.class);
        } catch (Exception e) {
            return ret;
        }
    }

    default String getScriptName() {
        return "exec";
    }
}
