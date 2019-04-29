package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.Flow;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:45
 */
@Mapper
@Repository
public interface FlowDao extends BaseDao{

    @Results(id = "flow", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "flowConfig", column = "flow_config"),
            @Result(property = "cron", column = "cron"),
            @Result(property = "flowStatus", column = "flow_status"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "createUser", column = "create_user"),
            @Result(property = "permit", column = "permit"),
            @Result(property = "params", column = "params"),
            @Result(property = "isSchedule", column = "is_schedule"),
            @Result(property = "flowEditorInfo", column = "flow_editor_info")
    })
    @Select("select * from flow where id = #{id}")
    Flow findById(@Param("id") int id);

    @Update("update flow set name=#{name},flow_config=#{flowConfig},cron=#{cron},flow_status=#{flowStatus}," +
            "permit=#{permit},params=#{params},start_time=#{startTime},flow_editor_info=#{flowEditorInfo}," +
            "is_schedule=#{isSchedule} where id=#{id}")
    void update(Flow flow);

    @Insert("insert into flow(name,flow_config,cron,flow_status,create_user,create_time,permit,params) " +
            "values(#{name},#{flowConfig},#{cron},#{flowStatus},#{createUser},#{createTime},#{permit},#{params})")
    void insert(Flow flow);

    @Delete("delete from flow where id = #{id}")
    void delete(@Param("id") int id);

    @ResultMap("flow")
    @Select("select * from flow")
    List<Flow> select();



}
