package com.zy.data.lts.service;

import com.github.pagehelper.PageHelper;
import com.zy.data.lts.core.dao.GroupDao;
import com.zy.data.lts.core.dao.RepmPolicyDao;
import com.zy.data.lts.core.dao.UserDao;
import com.zy.data.lts.core.entity.Group;
import com.zy.data.lts.core.entity.RepmPolicy;
import com.zy.data.lts.core.entity.User;
import com.zy.data.lts.core.model.PagerRequest;
import com.zy.data.lts.model.PermitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.zy.data.lts.security.utils.PasswordEncoderUtil.getDefaultPassword;

/**
 * @author chenqingsong
 * @date 2019/5/15 16:59
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    RepmPolicyDao repmPolicyDao;

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public List<Group> getAllGroups() {
        return groupDao.findAll();
    }

    public List<User> getAllUsers(PagerRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return userDao.findAll();
    }

    public List<Group> getAllGroups(PagerRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return groupDao.findAll();
    }

    public void update(User user) {
        userDao.update(user);
    }

    public void insert(User user) {
        user.setCreateTime(new Date());
        user.setPassword(getDefaultPassword());
        userDao.insert(user);
    }

    public void insert(Group group) {
        group.setCreateTime(new Date());
        groupDao.insert(group);
    }

    public void update(Group group) {
        groupDao.update(group);
    }

    public void deleteUser(String userName) {
        userDao.delete(userName);
    }

    public void deleteGroup(String groupName) {
        groupDao.delete(groupName);
    }

    public Integer getResourcePermit(PermitRequest request) {
        if ("User".equalsIgnoreCase(request.getUserOrGroup())) {
            return repmPolicyDao.findUserPermit(request.getName(), request.getResourceType(), request.getResource());
        } else if ("Group".equalsIgnoreCase(request.getUserOrGroup())) {
            return repmPolicyDao.findGroupPermit(request.getName(), request.getResourceType(), request.getResource());
        } else {
            return 0;
        }
    }

    public void updatePermit(PermitRequest request) {
        if ("User".equalsIgnoreCase(request.getUserOrGroup())) {
            RepmPolicy rp = new RepmPolicy();
            rp.setCreateTime(new Date());
            rp.setPermit(request.getPermit());
            rp.setType(request.getResourceType());
            rp.setPolicyName(repmPolicyDao.wrapUsername(request.getName()));
            rp.setResource(request.getResource());

            if (repmPolicyDao.findUserPermit(request.getName(), request.getResourceType(), request.getResource()) != null) {
                repmPolicyDao.update(rp);
            } else {
                repmPolicyDao.insert(rp);
            }

        } else if ("Group".equalsIgnoreCase(request.getUserOrGroup())) {
            RepmPolicy rp = new RepmPolicy();
            rp.setCreateTime(new Date());
            rp.setPermit(request.getPermit());
            rp.setType(request.getResourceType());
            rp.setPolicyName(repmPolicyDao.wrapGroup(request.getName()));
            rp.setResource(request.getResource());

            if (repmPolicyDao.findGroupPermit(request.getName(), request.getResourceType(), request.getResource()) != null) {
                repmPolicyDao.update(rp);
            } else {
                repmPolicyDao.insert(rp);
            }
        }
    }
}
