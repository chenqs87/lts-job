package com.zy.data.lts.executor.type;

import com.zy.data.lts.executor.model.JobExecuteEvent;

import java.nio.file.Paths;

/**
 * @author chenqingsong
 * @date 2019/5/23 12:29
 */
@JobType("shell")
public class ShellCommandTypeHandler implements IJobTypeHandler {

    private static final String SHELL_COMMAND = "sh";

    @Override
    public String[] createCommand(JobExecuteEvent event) {
        String scriptPath = Paths.get(event.getOutput().toString(), getScriptName()).toString();
        return new String[]{SHELL_COMMAND, scriptPath, event.getParams()};
    }
}
