package com.zy.data.lts.security;

import com.zy.data.lts.core.LtsPermitEnum;
import com.zy.data.lts.core.RoleEnum;
import com.zy.data.lts.core.dao.RepmPolicyDao;
import com.zy.data.lts.core.dao.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author chenqingsong
 * @date 2019/5/14 15:31
 */
@Component
public class LtsPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    RepmPolicyDao repmPolicyDao;
    @Autowired
    UserDao userDao;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

        User user = (User) authentication.getPrincipal();

        Collection<GrantedAuthority> authorities = user.getAuthorities();

        SimpleGrantedAuthority admin = new SimpleGrantedAuthority(RoleEnum.ROLE_ADMIN.name());
        if(authorities.contains(admin)) {
            return true;
        }

        if ((targetDomainObject instanceof Integer) && (permission instanceof String)) {
            Integer id = (Integer) targetDomainObject;
            LtsPermitEnum ltsPermit = LtsPermitEnum.valueOf((String) permission);


            Integer userPermitCode = repmPolicyDao.findUserPermit(user.getUsername(), ltsPermit.type.name(), id);
            Integer groupPermitCode = repmPolicyDao.findGroupPermit(
                    userDao.findByName(user.getUsername()).getGroupName(), ltsPermit.type.name(), id);

            return (userPermitCode != null && (userPermitCode & ltsPermit.code) > 0) || (groupPermitCode != null
                    && (groupPermitCode & ltsPermit.code) > 0);
        }
        return false;
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {
        throw new UnsupportedOperationException();
    }
}
