package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.Group;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/5/14 14:13
 */
@Mapper
@Repository
public interface GroupDao {

    @Insert("insert into groups(group_name,remark,create_time) " +
            "values(#{groupName},#{remark},#{createTime})")
    void insert(Group group);

    @Results(id = "group", value = {
            @Result(property = "groupName", column = "group_name"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "createTime", column = "create_time")
    })
    @Select("select * from groups where group_name=#{groupName}")
    Group findByName(@Param("groupName") String groupName);

    @ResultMap("group")
    @Select("select * from groups")
    List<Group> findAll();

    @Delete("delete from groups where group_name=#{groupName}")
    void delete(@Param("groupName") String groupName);

    @Update("update users set remark=#{remark} " +
            "where group_name=#{groupName}")
    void update(Group group);


}
