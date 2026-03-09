package com.reciprocityledger.backend.timeline.mapper;

import com.reciprocityledger.backend.timeline.dto.response.TimelineItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TimelineMapper {

    @Select({
            "<script>",
            "select lr.id as record_id, lr.event_exchange_id, ee.contact_id, c.name as contact_name, c.relation, lr.record_type, ee.event_type_code, ee.event_note, lr.occurred_on, lr.amount, lr.remark, ee.reciprocity_status",
            "from ledger_record lr",
            "inner join event_exchange ee on lr.event_exchange_id = ee.id and lr.user_id = ee.user_id",
            "inner join contact c on ee.contact_id = c.id and ee.user_id = c.user_id",
            "where lr.user_id = #{userId}",
            "<if test='recordType != null and recordType != \"\"'>",
            "  and lr.record_type = #{recordType}",
            "</if>",
            "<if test='contactId != null'>",
            "  and ee.contact_id = #{contactId}",
            "</if>",
            "<if test='relation != null and relation != \"\"'>",
            "  and c.relation = #{relation}",
            "</if>",
            "<if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "  and ee.event_type_code = #{eventTypeCode}",
            "</if>",
            "<if test='startDate != null'>",
            "  and lr.occurred_on <![CDATA[>=]]> #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  and lr.occurred_on <![CDATA[<=]]> #{endDate}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "  and (c.name like concat('%', #{keyword}, '%') or coalesce(ee.event_note, '') like concat('%', #{keyword}, '%') or coalesce(lr.remark, '') like concat('%', #{keyword}, '%'))",
            "</if>",
            "order by lr.occurred_on desc, lr.id desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<TimelineItemResponse> selectPage(@Param("userId") Long userId,
                                          @Param("recordType") String recordType,
                                          @Param("contactId") Long contactId,
                                          @Param("relation") String relation,
                                          @Param("eventTypeCode") String eventTypeCode,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("keyword") String keyword,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize);

    @Select({
            "<script>",
            "select count(1)",
            "from ledger_record lr",
            "inner join event_exchange ee on lr.event_exchange_id = ee.id and lr.user_id = ee.user_id",
            "inner join contact c on ee.contact_id = c.id and ee.user_id = c.user_id",
            "where lr.user_id = #{userId}",
            "<if test='recordType != null and recordType != \"\"'>",
            "  and lr.record_type = #{recordType}",
            "</if>",
            "<if test='contactId != null'>",
            "  and ee.contact_id = #{contactId}",
            "</if>",
            "<if test='relation != null and relation != \"\"'>",
            "  and c.relation = #{relation}",
            "</if>",
            "<if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "  and ee.event_type_code = #{eventTypeCode}",
            "</if>",
            "<if test='startDate != null'>",
            "  and lr.occurred_on <![CDATA[>=]]> #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  and lr.occurred_on <![CDATA[<=]]> #{endDate}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "  and (c.name like concat('%', #{keyword}, '%') or coalesce(ee.event_note, '') like concat('%', #{keyword}, '%') or coalesce(lr.remark, '') like concat('%', #{keyword}, '%'))",
            "</if>",
            "</script>"
    })
    long countPage(@Param("userId") Long userId,
                   @Param("recordType") String recordType,
                   @Param("contactId") Long contactId,
                   @Param("relation") String relation,
                   @Param("eventTypeCode") String eventTypeCode,
                   @Param("startDate") LocalDate startDate,
                   @Param("endDate") LocalDate endDate,
                   @Param("keyword") String keyword);
}
