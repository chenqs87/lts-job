package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.Job;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:44
 */
@Mapper
@Repository
public interface JobDao {

    @Results(id = "job", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "handler", column = "handler"),
            @Result(property = "jobType", column = "job_type"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "createUser", column = "create_user"),
            @Result(property = "content", column = "content"),
            @Result(property = "shardType", column = "shard_type"),
            @Result(property = "config", column = "config"),
            @Result(property = "group", column = "group")

    })
    @Select("select * from job where id=#{id}")
    Job findById(@Param("id") int id);

    @ResultMap("job")
    @Select("<script>" +
            "select * from job where " +
            "<if test='name != null'>" +
            " name like CONCAT('%',#{name},'%') and " +
            " </if>" +
            "<if test='group != null'>" +
            " `group` like CONCAT('%',#{group},'%') and " +
            " </if>"
            + "1=1" +
            "</script>")
    List<Job> select(Object params);

    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
    @Insert("insert into job (name, handler,job_type,create_time,create_user, content,shard_type, config, `group`) " +
            "values(#{name},#{handler},#{jobType},#{createTime},#{createUser},#{content}, #{shardType}, #{config}, #{group})")
    void insert(Job job);

    @Update("update job set name=#{name},handler=#{handler},job_type=#{jobType},content=#{content}," +
            "shard_type=#{shardType},config=#{config},`group`=#{group} where id=#{id}")
    void update(Job job);

    @Delete("delete from job where id = #{id}")
    void delete(@Param("id") int id);

    @ResultMap("job")
    @Select("<script>" +
            "select j.*,rp.permit from repm_policy rp inner join job j on j.id =  rp.resource" +
            " where policy_name=concat('u_',#{username}) and `type` = 'Job' and (rp.permit &amp; #{permit}) > 0 " +

            "<if test='name != null'>" +
            " and j.name like CONCAT('%',#{name},'%') " +
            " </if>" +
            "<if test='group != null'>" +
            " and j.group like CONCAT('%',#{group},'%') " +
            " </if>" +

            "</script>")
    List<Job> selectByUser(Object params);


    @ResultMap("job")
    @Select("<script>" +
            "select j.*,rp.permit from repm_policy rp inner join job j on  j.id =  rp.resource" +
            " where policy_name=concat('g_',#{userGroup}) and `type` = 'Job' and (rp.permit &amp; #{permit}) > 0 " +
            "<if test='name != null'>" +
            " and j.name like CONCAT('%',#{name},'%') " +
            " </if>" +
            "<if test='group != null'>" +
            " and j.group like CONCAT('%',#{group},'%') " +
            " </if>" +

            "</script>")
    List<Job> selectByGroup(Object params);

}
