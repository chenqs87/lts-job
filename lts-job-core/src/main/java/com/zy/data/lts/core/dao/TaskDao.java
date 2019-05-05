package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.Task;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:44
 */
@Mapper
@Repository
public interface TaskDao extends BaseDao {

    @Insert("insert into task values(#{flowTaskId},#{taskId},#{jobId},#{flowId},#{taskStatus}," +
            "#{preTask},#{postTask},#{beginTime},#{endTime},#{shardStatus},#{handler},#{host})")
    void insert(Task task);

    @Results(id = "task", value = {
            @Result(property = "flowTaskId", column = "flow_task_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "flowId", column = "flow_id"),
            @Result(property = "taskStatus", column = "task_status"),
            @Result(property = "preTask", column = "pre_task"),
            @Result(property = "postTask", column = "post_task"),
            @Result(property = "beginTime", column = "begin_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "shardStatus", column = "shard_status"),
            @Result(property = "handler", column = "handler"),
            @Result(property = "host", column = "host")
    })
    @Select("select * from task where flow_task_id=#{flowTaskId} and task_id=#{taskId}")
    Task findById(@Param("flowTaskId") int flowTaskId, @Param("taskId") int taskId);

    @Update("update task set pre_task=#{preTask},task_status=#{taskStatus}," +
            "end_time=#{endTime},shard_status=#{shardStatus},handler=#{handler}," +
            "host=#{host} " +
            "where flow_task_id=#{flowTaskId} and task_id=#{taskId}")
    void update(Task task);

    @ResultMap("task")
    @Select("select * from task where flow_task_id=#{flowTaskId}")
    List<Task> findByFlowTaskId(@Param("flowTaskId") int flowTaskId);
}
