package com.zy.data.lts.core.entity;

/**
 * @author chenqingsong
 * @date 2019/5/20 20:51
 */
public class AlertConfig extends Flow {
    private Integer flowId;
    private String phoneList;
    private String emailList;

    public Integer getFlowId() {
        return flowId;
    }

    public void setFlowId(Integer flowId) {
        this.flowId = flowId;
    }

    public String getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(String phoneList) {
        this.phoneList = phoneList;
    }

    public String getEmailList() {
        return emailList;
    }

    public void setEmailList(String emailList) {
        this.emailList = emailList;
    }
}
