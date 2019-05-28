package com.zy.data.lts.job.type;

import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.type.IJobTypeHandler;
import com.zy.data.lts.executor.type.JobType;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author chenqingsong
 * @date 2019/5/23 17:36
 */
@JobType("checkFileContent")
public class CheckFileScriptJobTypeHandler implements IJobTypeHandler {
    private static final String CHECK_FILE_NAME = "CheckFileContent.sh";

    @Override
    public String[] createCommand(JobExecuteEvent event) {
        try {
            InputStream is = CheckFileScriptJobTypeHandler.class.getClassLoader().getResourceAsStream(CHECK_FILE_NAME);
            Path checkFileOutputPath = Paths.get(event.getOutput().toString(), CHECK_FILE_NAME);
            Files.copy(is, checkFileOutputPath, StandardCopyOption.REPLACE_EXISTING);
            String scriptFile = Paths.get(event.getOutput().toString(), getScriptName()).toString();
            return new String[]{"sh", checkFileOutputPath.toString(), scriptFile, event.getParams()};
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}