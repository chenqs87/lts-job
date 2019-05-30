package com.zy.data.lts.job.type;

import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.type.IJobTypeHandler;
import com.zy.data.lts.executor.type.JobType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private static final Logger logger = LoggerFactory.getLogger(CheckFileScriptJobTypeHandler.class);
    private static final String CHECK_FILE_NAME = "CheckFileContent.sh";
    @Autowired
    private ExecutorConfig config;

    @Override
    public String[] createCommand(JobExecuteEvent event) {
        try(InputStream is = getCheckFileInput()) {
            Path checkFileOutputPath = Paths.get(event.getOutput().toString(), CHECK_FILE_NAME);
            Files.copy(is, checkFileOutputPath, StandardCopyOption.REPLACE_EXISTING);
            String scriptFile = Paths.get(event.getOutput().toString(), getScriptName()).toString();
            return new String[]{"sh", checkFileOutputPath.toString(), scriptFile, event.getParams()};
        } catch (Exception e) {
            logger.error("Fail to create command! Event: [{}]", event, e);
            throw new IllegalStateException(e);
        }
    }

    private InputStream getCheckFileInput() throws FileNotFoundException {
        String filePath = config.getExecuteEnv().get("CheckInputContentScript");
        if(StringUtils.isNotBlank(filePath)) {
            return new FileInputStream(filePath);
        } else {
            return CheckFileScriptJobTypeHandler.class.getClassLoader().getResourceAsStream(CHECK_FILE_NAME);
        }
    }
}