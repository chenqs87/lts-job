package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.Job;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:44
 */
@Mapper
@Repository
public interface JobDao extends BaseDao {

    @Results(id = "job", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "handler", column = "handler"),
            @Result(property = "jobType", column = "job_type"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "createUser", column = "create_user"),
            @Result(property = "content", column = "content"),
            @Result(property = "permit", column = "permit"),
            @Result(property = "shardType", column = "shard_type"),
            @Result(property = "config", column = "config"),
            @Result(property = "group", column = "group")

    })
    @Select("select * from job where id=#{id}")
    Job findById(@Param("id") int id);

    @ResultMap("job")
    @Select("<script>" +
            "select * from job where 1=1" +
            "<if test='name != null'>" +
            " and name like CONCAT('%',#{name},'%') " +
            " </if>" +
            "<if test='group != null'>" +
            " and `group` like CONCAT('%',#{group},'%') " +
            " </if>" +
            "</script>")
    List<Job> select(Object params);

    @Insert("insert into job (name, handler,job_type,create_time,create_user, content, permit,shard_type, config, group) " +
            "values(#{name},#{handler},#{jobType},#{createTime},#{createUser},#{content}, #{permit}, #{shardType}, " +
            "#{config}, #{group})")
    void insert(Job job);

    @Update("update job set name=#{name},handler=#{handler},job_type=#{jobType},content=#{content}," +
            "permit=#{permit},shard_type=#{shardType},config=#{config},`group`=#{group} where id=#{id}")
    void update(Job job);

    @Delete("delete from job where id = #{id}")
    void delete(@Param("id") int id);
}
