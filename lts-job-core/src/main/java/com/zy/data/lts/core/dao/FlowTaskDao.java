package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.FlowTask;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:45
 */
@Mapper
@Repository
public interface FlowTaskDao {

    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
    @Insert("insert into flow_task(flow_id,create_user,status,begin_time,end_time,params,trigger_mode) " +
            "values(#{flowId},#{createUser},#{status},#{beginTime},#{endTime},#{params},#{triggerMode})")
    void insert(FlowTask flowTask);

    @Results(id = "flowTask", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "flowId", column = "flow_id"),
            @Result(property = "beginTime", column = "begin_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "createUser", column = "create_user"),
            @Result(property = "status", column = "status"),
            @Result(property = "params", column = "params"),
            @Result(property = "triggerMode", column = "trigger_mode"),

    })
    @Select("select * from flow_task where id=#{id}")
    FlowTask findById(@Param("id") int id);

    @Update("update flow_task set status=#{status},params=#{params},end_time=#{endTime} where id=#{id}")
    void update(FlowTask flowTask);


    @ResultMap("flowTask")
    @Select("select * from flow_task where status in (0,1,2)")
    List<FlowTask> findUnFinishedFlowTasks();

    @ResultMap("flowTask")
    @Select("select * from flow_task order by begin_time desc")
    List<FlowTask> select();

    @ResultMap("flowTask")
    @Select("select * from flow_task where flow_id=#{flowId} order by begin_time desc")
    List<FlowTask> findByFlowId(@Param("flowId") int flowId);
}
