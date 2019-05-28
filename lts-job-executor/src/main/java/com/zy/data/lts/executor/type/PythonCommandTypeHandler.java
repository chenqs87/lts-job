package com.zy.data.lts.executor.type;

import com.zy.data.lts.executor.model.JobExecuteEvent;

import java.nio.file.Paths;

/**
 * @author chenqingsong
 * @date 2019/5/23 12:29
 */
@JobType("python")
public class PythonCommandTypeHandler implements IJobTypeHandler {

    private static final String PYTHON_COMMAND = "python";

    @Override
    public String[] createCommand(JobExecuteEvent event) {
        String scriptPath = Paths.get(event.getOutput().toString(), getScriptName()).toString();
        return new String[]{PYTHON_COMMAND, scriptPath, event.getParams()};
    }
}
