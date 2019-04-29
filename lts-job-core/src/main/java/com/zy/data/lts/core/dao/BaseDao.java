package com.zy.data.lts.core.dao;

import org.apache.ibatis.annotations.Select;

/**
 * @author chenqingsong
 * @date 2019/4/24 18:25
 */
public interface BaseDao {
    @Select("select LAST_INSERT_ID()")
    int getId();
}
