package com.zy.data.lts.model;

/**
 * @author chenqingsong
 * @date 2019/5/20 12:00
 */
public class PermitRequest {
    private String userOrGroup;
    private String name;
    private Integer resource;
    private String resourceType;
    private Integer permit;

    public String getUserOrGroup() {
        return userOrGroup;
    }

    public void setUserOrGroup(String userOrGroup) {
        this.userOrGroup = userOrGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getResource() {
        return resource;
    }

    public void setResource(Integer resource) {
        this.resource = resource;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getPermit() {
        return permit;
    }

    public void setPermit(Integer permit) {
        this.permit = permit;
    }
}
