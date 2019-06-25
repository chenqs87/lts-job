package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.FlowTask;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:45
 */
@Mapper
@Repository
public interface FlowTaskDao {

    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
    @Insert("insert into flow_task(flow_id,status,begin_time,end_time,params,trigger_mode,host) " +
            "values(#{flowId},#{status},#{beginTime},#{endTime},#{params},#{triggerMode},#{host})")
    void insert(FlowTask flowTask);

    @Results(id = "flowTask", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "flowId", column = "flow_id"),
            @Result(property = "beginTime", column = "begin_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "params", column = "params"),
            @Result(property = "triggerMode", column = "trigger_mode"),
            @Result(property = "host", column = "host")
    })
    @Select("select * from flow_task where id=#{id}")
    FlowTask findById(@Param("id") int id);

    @Update("update flow_task set status=#{status},params=#{params},end_time=#{endTime},host=#{host} where id=#{id}")
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
             " order by id desc"+
            "</script>")
    List<FlowTask> select(@Param("flowId") int flowId, @Param("statusId") int statusId);

    @Select("select DATE_FORMAT(begin_time,'%Y-%m-%d') as day, " +
            "sum(case when status = 3 then 1 else 0 end) as failed, " +
            "sum(case when status = 4 then 1 else 0 end) as success from flow_task " +
            "where begin_time between #{fromDate} and #{toDate} group by day order by day asc")
    List<Map<String, Object>> countTasksByDay(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate) ;

    @ResultType(Integer.class)
    @Select("select count(1) from flow_task where status = 2")
    int findRunningFlowTasks();
}
