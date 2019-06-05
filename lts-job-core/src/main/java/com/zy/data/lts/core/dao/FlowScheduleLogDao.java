package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.FlowScheduleLog;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FlowScheduleLogDao {

    @Results(id = "flow", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "flowTaskId", column = "flow_task_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time")

    })
    @Select("select * from `flow_schedule_log` where flow_task_id = #{flowTaskId}")
    List<FlowScheduleLog> select(@Param("flowTaskId") int flowTaskId);

    @Insert("insert into flow_schedule_log (flow_task_id, content, create_time) " +
            "values (#{flowTaskId},#{content},#{createTime})")
    void insert(FlowScheduleLog log);
}
