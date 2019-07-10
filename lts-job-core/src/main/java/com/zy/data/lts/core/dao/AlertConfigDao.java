package com.zy.data.lts.core.dao;

import com.zy.data.lts.core.entity.AlertConfig;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author chenqingsong
 * @date 2019/5/20 20:51
 */
@Mapper
@Repository
public interface AlertConfigDao {

    @Insert("insert into alert values(#{flowId},#{phoneList},#{emailList})")
    void insert(AlertConfig config);

    @Results(id = "alertConfig", value = {
            @Result(property = "flowId", column = "flow_id"),
            @Result(property = "phoneList", column = "phone_list"),
            @Result(property = "emailList", column = "email_list")
    })
    @Select("select * from alert where flow_id = #{flowId}")
    AlertConfig findByFlowId(@Param("flowId") Integer flowId);

    @Update("update alert set phone_list=#{phoneList},email_list=#{emailList} where flow_id=#{flowId}")
    void update(AlertConfig config);

}
