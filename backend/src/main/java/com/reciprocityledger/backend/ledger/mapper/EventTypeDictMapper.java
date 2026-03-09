package com.reciprocityledger.backend.ledger.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EventTypeDictMapper {

    @Select("select count(1) from event_type_dict where code = #{code} and enabled = true")
    int countEnabledByCode(@Param("code") String code);
}
