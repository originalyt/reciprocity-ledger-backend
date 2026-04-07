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

    /**
     * 查询未闭环记录（UNMATCHED）- 从 rl_gift_record 直接查询
     */
    @Select({
            "<script>",
            "select r.id as reciprocity_match_id, r.contact_id, c.contact_name, t.id as event_type_id, t.type_name as event_type_name,",
            "e.id as event_id, e.event_name, e.event_owner_type, r.amount, r.record_date,",
            "'UNMATCHED' as reciprocity_status, null as match_type",
            "from rl_gift_record r",
            "join rl_event e on e.id = r.event_id",
            "join rl_contact c on c.id = r.contact_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  r.reciprocity_status = 'UNMATCHED'",
            "  and not exists (select 1 from rl_reciprocity_match m where m.match_status = 'ACTIVE' and (m.source_record_id = r.id or m.target_record_id = r.id))",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "order by r.id desc",
            "limit #{pageSize}",
            "</script>"
    })
    List<ReciprocityPageItemResponse> selectUnmatchedPage(@Param("userId") String userId,
                                                           @Param("contactId") String contactId,
                                                           @Param("eventTypeId") String eventTypeId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           @Param("pageSize") int pageSize);

    /**
     * 查询已闭环记录（MATCHED/MANUAL_CONFIRMED）- 从 rl_reciprocity_match 查询
     */
    @Select({
            "<script>",
            "select m.id as reciprocity_match_id, r.contact_id, c.contact_name, t.id as event_type_id, t.type_name as event_type_name,",
            "e.id as event_id, e.event_name, e.event_owner_type, r.amount, r.record_date,",
            "m.match_type, r.reciprocity_status",
            "from rl_reciprocity_match m",
            "join rl_gift_record r on (r.id = m.source_record_id or r.id = m.target_record_id)",
            "join rl_event e on e.id = r.event_id",
            "join rl_contact c on c.id = r.contact_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  m.match_status = 'ACTIVE'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and m.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
            "  </if>",
            "  <if test='matchType != null and matchType != \"\"'>",
            "    and m.match_type = #{matchType}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "order by m.id desc, e.event_owner_type desc",
            "limit #{pageSize}",
            "</script>"
    })
    List<ReciprocityPageItemResponse> selectMatchedPage(@Param("userId") String userId,
                                                        @Param("contactId") String contactId,
                                                        @Param("eventTypeId") String eventTypeId,
                                                        @Param("matchType") String matchType,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("pageSize") int pageSize);

    /**
     * 统计未闭环记录数量
     */
    @Select({
            "<script>",
            "select count(*)",
            "from rl_gift_record r",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  r.reciprocity_status = 'UNMATCHED'",
            "  and not exists (select 1 from rl_reciprocity_match m where m.match_status = 'ACTIVE' and (m.source_record_id = r.id or m.target_record_id = r.id))",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
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
    long countUnmatchedPage(@Param("userId") String userId,
                            @Param("contactId") String contactId,
                            @Param("eventTypeId") String eventTypeId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * 统计已闭环记录数量
     */
    @Select({
            "<script>",
            "select count(distinct m.id)",
            "from rl_reciprocity_match m",
            "join rl_gift_record r on (r.id = m.source_record_id or r.id = m.target_record_id)",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  m.match_status = 'ACTIVE'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and m.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
            "  </if>",
            "  <if test='matchType != null and matchType != \"\"'>",
            "    and m.match_type = #{matchType}",
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
    long countMatchedPage(@Param("userId") String userId,
                          @Param("contactId") String contactId,
                          @Param("eventTypeId") String eventTypeId,
                          @Param("matchType") String matchType,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    /**
     * 查询已取消的闭环记录（MANUAL_CANCELED）- 从 rl_reciprocity_match 查询
     */
    @Select({
            "<script>",
            "select m.id as reciprocity_match_id, r.contact_id, c.contact_name, t.id as event_type_id, t.type_name as event_type_name,",
            "e.id as event_id, e.event_name, e.event_owner_type, r.amount, r.record_date,",
            "m.match_type, r.reciprocity_status",
            "from rl_reciprocity_match m",
            "join rl_gift_record r on (r.id = m.source_record_id or r.id = m.target_record_id)",
            "join rl_event e on e.id = r.event_id",
            "join rl_contact c on c.id = r.contact_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  m.match_status = 'CANCELED'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and m.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "order by m.id desc, e.event_owner_type desc",
            "limit #{pageSize}",
            "</script>"
    })
    List<ReciprocityPageItemResponse> selectCanceledPage(@Param("userId") String userId,
                                                         @Param("contactId") String contactId,
                                                         @Param("eventTypeId") String eventTypeId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("pageSize") int pageSize);

    /**
     * 统计已取消的闭环记录数量
     */
    @Select({
            "<script>",
            "select count(distinct m.id)",
            "from rl_reciprocity_match m",
            "join rl_gift_record r on (r.id = m.source_record_id or r.id = m.target_record_id)",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  m.match_status = 'CANCELED'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and m.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
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
    long countCanceledPage(@Param("userId") String userId,
                           @Param("contactId") String contactId,
                           @Param("eventTypeId") String eventTypeId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);

    /**
     * 查询无需往来的记录（NO_NEED）- 从 rl_gift_record 直接查询
     */
    @Select({
            "<script>",
            "select r.id as reciprocity_match_id, r.id as record_id, r.contact_id, c.contact_name, t.id as event_type_id, t.type_name as event_type_name,",
            "e.id as event_id, e.event_name, e.event_owner_type, r.amount, r.record_date, r.direction, r.remark,",
            "'NO_NEED' as reciprocity_status, null as match_type, r.no_need_reason",
            "from rl_gift_record r",
            "join rl_event e on e.id = r.event_id",
            "join rl_contact c on c.id = r.contact_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  r.reciprocity_status = 'NO_NEED'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "order by r.id desc",
            "limit #{pageSize}",
            "</script>"
    })
    List<ReciprocityPageItemResponse> selectNoNeedPage(@Param("userId") String userId,
                                                        @Param("contactId") String contactId,
                                                        @Param("eventTypeId") String eventTypeId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("pageSize") int pageSize);

    /**
     * 统计无需往来的记录数量
     */
    @Select({
            "<script>",
            "select count(*)",
            "from rl_gift_record r",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  r.reciprocity_status = 'NO_NEED'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and t.id = #{eventTypeId}",
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
    long countNoNeedPage(@Param("userId") String userId,
                         @Param("contactId") String contactId,
                         @Param("eventTypeId") String eventTypeId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate);
}