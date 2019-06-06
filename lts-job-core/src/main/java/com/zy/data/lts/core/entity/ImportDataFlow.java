package com.zy.data.lts.core.entity;

/**
 * @author yjm
 * @date 2019-06-05 20:30
 */
public class ImportDataFlow {
    private String checkGroupName;
    private  String checkSize;
    private String checkContent;
    private String ipDataConfig;


    public String getCheckGroupName() {
        return checkGroupName;
    }

    public void setCheckGroupName(String checkGroupName) {
        this.checkGroupName = checkGroupName;
    }

    public String getCheckSize() {
        return checkSize;
    }

    public void setCheckSize(String checkSize) {
        this.checkSize = checkSize;
    }

    public String getCheckContent() {
        return checkContent;
    }

    public void setCheckContent(String checkContent) {
        this.checkContent = checkContent;
    }

    public String getIpDataConfig() {
        return ipDataConfig;
    }

    public void setIpDataConfig(String ipDataConfig) {
        this.ipDataConfig = ipDataConfig;
    }
}
