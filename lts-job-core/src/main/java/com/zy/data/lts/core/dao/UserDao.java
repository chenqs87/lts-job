package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/5/14 14:13
 */
@Mapper
@Repository
public interface UserDao {

    @Insert("insert into users(user_name,password,role,iphone,email,group_name,create_time) " +
            "values(#{username},#{password},#{role},#{iphone},#{email},#{groupName},#{createTime})")
    void insert(User user);

    @Results(id = "user", value = {
            @Result(property = "username", column = "user_name"),
            @Result(property = "password", column = "password"),
            @Result(property = "role", column = "role"),
            @Result(property = "iphone", column = "iphone"),
            @Result(property = "email", column = "email"),
            @Result(property = "groupName", column = "group_name"),
            @Result(property = "createTime", column = "create_time")
    })
    @Select("select * from users where user_name=#{username}")
    User findByName(@Param("username") String username);

    @ResultMap("user")
    @Select("select user_name,role,iphone,email,group_name,create_time from users")
    List<User> findAll();

    @Update("update users set password=#{password},role=#{role},iphone=#{iphone},email=#{email},group_name=#{groupName} " +
            "where user_name=#{username}")
    void update(User user);

    @Delete("delete from users where user_name=#{username}")
    void delete(@Param("username") String username);

}
