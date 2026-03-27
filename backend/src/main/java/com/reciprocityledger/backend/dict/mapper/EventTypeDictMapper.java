package com.reciprocityledger.backend.dict.mapper;

import com.reciprocityledger.backend.dict.dto.response.DictItemResponse;
import com.reciprocityledger.backend.dict.entity.EventTypeDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EventTypeDictMapper {

    @Select({
            "<script>",
            "select id, type_code, type_name, sort_no, enabled_flag, built_in_flag, remark, create_time, update_time",
            "from rl_event_type",
            "<where>",
            "  <if test='enabledFlag != null'>",
            "    enabled_flag = #{enabledFlag}",
            "  </if>",
            "</where>",
            "order by sort_no asc, id asc",
            "</script>"
    })
    List<EventTypeDict> selectList(@Param("enabledFlag") Boolean enabledFlag);

    @Select("select id, type_code, type_name, sort_no, enabled_flag, built_in_flag, remark, create_time, update_time from rl_event_type where id = #{id} limit 1")
    EventTypeDict selectById(@Param("id") String id);

    @Select("select id, type_code, type_name, sort_no, enabled_flag, built_in_flag, remark, create_time, update_time from rl_event_type where type_code = #{typeCode} limit 1")
    EventTypeDict selectByTypeCode(@Param("typeCode") String typeCode);

    @Select({
            "<script>",
            "select id as id, type_code as code, type_name as name from rl_event_type",
            "<where>",
            "  <if test='enabledFlag != null'>",
            "    enabled_flag = #{enabledFlag}",
            "  </if>",
            "</where>",
            "order by sort_no asc, id asc",
            "</script>"
    })
    List<DictItemResponse> selectDictItems(@Param("enabledFlag") Boolean enabledFlag);
}
