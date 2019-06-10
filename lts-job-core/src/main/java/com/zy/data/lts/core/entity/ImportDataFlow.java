package com.zy.data.lts.core.entity;

/**
 * @author yjm
 * @date 2019-06-05 20:30
 */
public class ImportDataFlow {
    public static final String CHECK_FILE_CONTENT_JOB_TYPE = "ChkFile";
    public static final String IMPORT_DATA_JOB_NAME = "ImpData";

    private String group;
    private String size;
    private String sizeHandler;
    private String content;
    private String contentHandler;
    private String config;
    private String importDataHandler;
    private String cron;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getSizeHandler() {
        return sizeHandler;
    }

    public void setSizeHandler(String sizeHandler) {
        this.sizeHandler = sizeHandler;
    }

    public String getContentHandler() {
        return contentHandler;
    }

    public void setContentHandler(String contentHandler) {
        this.contentHandler = contentHandler;
    }

    public String getImportDataHandler() {
        return importDataHandler;
    }

    public void setImportDataHandler(String importDataHandler) {
        this.importDataHandler = importDataHandler;
    }
}
