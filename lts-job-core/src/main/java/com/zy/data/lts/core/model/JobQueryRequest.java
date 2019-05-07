package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/5/7 09:42
 */
public class JobQueryRequest extends PagerRequest {

    private String name;
    private String group;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
