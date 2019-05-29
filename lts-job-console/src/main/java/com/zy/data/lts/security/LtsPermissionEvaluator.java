package com.zy.data.lts.security;

import com.zy.data.lts.core.dao.RepmPolicyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author chenqingsong
 * @date 2019/5/14 15:31
 */
@Component
public class LtsPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    RepmPolicyDao repmPolicyDao;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if ((targetDomainObject instanceof Integer) && (permission instanceof String)) {
            Integer id = (Integer) targetDomainObject;
            LtsPermitEnum ltsPermit = LtsPermitEnum.valueOf((String) permission);
            User username = (User) authentication.getPrincipal();
            Integer permitCode = repmPolicyDao.findUserPermit(username.getUsername(), ltsPermit.type.name(), id);

            return permitCode != null && (permitCode & ltsPermit.code) > 0;
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        throw new UnsupportedOperationException();
    }
}
