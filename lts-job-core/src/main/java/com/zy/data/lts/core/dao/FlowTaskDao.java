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
    @Insert("insert into flow_task(flow_id,status,begin_time,end_time,params,trigger_mode) " +
            "values(#{flowId},#{status},#{beginTime},#{endTime},#{params},#{triggerMode})")
    void insert(FlowTask flowTask);

    @Results(id = "flowTask", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "flowId", column = "flow_id"),
            @Result(property = "beginTime", column = "begin_time"),
            @Result(property = "endTime", column = "end_time"),
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
    @Select("<script>" +
            "select * from flow_task where" +
            "<if test='flowId != -1'>" +
            " flow_id =#{flowId} and" +
            " </if>" +
            "<if test='statusId != -1'>" +
            " status =#{statusId} and" +
            " </if>" +
            " 1=1" +
             " order by begin_time desc"+
            "</script>")
    List<FlowTask> select(@Param("flowId") int flowId,@Param("statusId") int statusId);

}
