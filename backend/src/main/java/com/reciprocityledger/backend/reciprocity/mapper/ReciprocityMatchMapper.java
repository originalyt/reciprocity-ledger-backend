package com.reciprocityledger.backend.reciprocity.mapper;

import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityPageItemResponse;
import com.reciprocityledger.backend.reciprocity.entity.ReciprocityMatch;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReciprocityMatchMapper {

    @Select("select id, source_record_id, target_record_id, match_type, match_status, cancel_reason, remark, create_time, update_time, user_id from rl_reciprocity_match where id = #{matchId} limit 1")
    ReciprocityMatch selectById(@Param("matchId") String matchId);

    @Select("select id, source_record_id, target_record_id, match_type, match_status, cancel_reason, remark, create_time, update_time, user_id from rl_reciprocity_match where match_status = 'ACTIVE' and (source_record_id = #{recordId} or target_record_id = #{recordId}) limit 1")
    ReciprocityMatch selectActiveByRecordId(@Param("recordId") String recordId);

    @Insert("insert into rl_reciprocity_match(id, source_record_id, target_record_id, match_type, match_status, cancel_reason, remark, user_id) values(#{id}, #{sourceRecordId}, #{targetRecordId}, #{matchType}, #{matchStatus}, #{cancelReason}, #{remark}, #{userId})")
    int insert(ReciprocityMatch match);

    @Update("update rl_reciprocity_match set match_status = 'CANCELED', cancel_reason = #{cancelReason}, update_time = now() where id = #{matchId}")
    int cancel(@Param("matchId") String matchId, @Param("cancelReason") String cancelReason);

    @Select({
            "<script>",
            "select r.id as record_id, m.id as reciprocity_match_id, r.contact_id, c.contact_name, r.event_id, e.event_name, t.type_code as event_type_code, t.type_name as event_type_name,",
            "r.direction, r.amount, r.record_date, r.reciprocity_status, mr.id as matched_record_id, mr.amount as matched_amount, mr.record_date as matched_record_date",
            "from rl_gift_record r",
            "join rl_contact c on c.id = r.contact_id",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "left join rl_reciprocity_match m on m.match_status = 'ACTIVE' and (m.source_record_id = r.id or m.target_record_id = r.id)",
            "left join rl_gift_record mr on mr.id = case when m.source_record_id = r.id then m.target_record_id else m.source_record_id end",
            "<where>",
            "  1 = 1",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
            "  </if>",
            "  <if test='reciprocityStatus != null and reciprocityStatus != \"\"'>",
            "    and r.reciprocity_status = #{reciprocityStatus}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "order by r.record_date desc, r.id desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<ReciprocityPageItemResponse> selectPage(@Param("userId") String userId,
                                                 @Param("contactId") String contactId,
                                                 @Param("eventTypeCode") String eventTypeCode,
                                                 @Param("reciprocityStatus") String reciprocityStatus,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("offset") int offset,
                                                 @Param("pageSize") int pageSize);

    @Select({
            "<script>",
            "select count(1)",
            "from rl_gift_record r",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  1 = 1",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
            "  </if>",
            "  <if test='reciprocityStatus != null and reciprocityStatus != \"\"'>",
            "    and r.reciprocity_status = #{reciprocityStatus}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countPage(@Param("userId") String userId,
                   @Param("contactId") String contactId,
                   @Param("eventTypeCode") String eventTypeCode,
                   @Param("reciprocityStatus") String reciprocityStatus,
                   @Param("startDate") LocalDate startDate,
                   @Param("endDate") LocalDate endDate);
}
