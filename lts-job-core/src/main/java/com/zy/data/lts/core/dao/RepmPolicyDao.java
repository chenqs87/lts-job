package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.RepmPolicy;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author chenqingsong
 * @date 2019/5/14 14:20
 */
@Mapper
@Repository
public interface RepmPolicyDao {
    @Insert("insert into repm_policy(policy_name, type, resource, permit, create_time) " +
            "values(#{policyName}, #{type}, #{resource}, #{permit}, #{createTime})")
    void insert(RepmPolicy repmPolicy);

    @Select("select permit from repm_policy where policy_name=concat('u_',#{policyName}) and type = #{type} and resource = #{resource}")
    Integer findUserPermit(@Param("policyName") String policyName,
                           @Param("type") String type,
                           @Param("resource") Integer resource);

    @Select("select permit from repm_policy where policy_name=concat('g_', #{policyName}) and type = #{type} and resource = #{resource}")
    Integer findGroupPermit(@Param("policyName") String policyName,
                            @Param("type") String type,
                            @Param("resource") Integer resource);

    @Delete("delete from repm_policy where type =#{type} and resource=#{resource}")
    void delete(@Param("type") String type, @Param("resource") Integer resource);


    @Update("update repm_policy set permit=#{permit} " +
            "where policy_name=#{policyName} and type=#{type} and resource =#{resource}")
    void update(RepmPolicy repmPolicy);


    default String wrapUsername(String username) {
        return "u_" + username;
    }

    default String wrapGroup(String group) {
        return "g_" + group;
    }

}
