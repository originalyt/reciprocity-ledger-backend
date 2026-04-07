package com.reciprocityledger.backend.record.mapper;

import com.reciprocityledger.backend.record.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.record.dto.response.RecordPageItemResponse;
import com.reciprocityledger.backend.record.dto.response.TimelineSummaryResponse;
import com.reciprocityledger.backend.record.entity.GiftRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RecordMapper {

    @Select({
            "<script>",
            "select r.id as record_id, r.contact_id, c.contact_name, r.event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name,",
            "r.direction, r.amount, r.record_date, r.reciprocity_status",
            "from rl_gift_record r",
            "join rl_contact c on c.id = r.contact_id",
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
            "  <if test='eventId != null and eventId != \"\"'>",
            "    and r.event_id = #{eventId}",
            "  </if>",
            "  <if test='direction != null and direction != \"\"'>",
            "    and r.direction = #{direction}",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
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
    List<RecordPageItemResponse> selectPage(@Param("userId") String userId,
                                            @Param("contactId") String contactId,
                                            @Param("eventId") String eventId,
                                            @Param("direction") String direction,
                                            @Param("eventTypeCode") String eventTypeCode,
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
            "  <if test='eventId != null and eventId != \"\"'>",
            "    and r.event_id = #{eventId}",
            "  </if>",
            "  <if test='direction != null and direction != \"\"'>",
            "    and r.direction = #{direction}",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
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
                   @Param("eventId") String eventId,
                   @Param("direction") String direction,
                   @Param("eventTypeCode") String eventTypeCode,
                   @Param("startDate") LocalDate startDate,
                   @Param("endDate") LocalDate endDate);

    @Select({
            "select r.id as record_id, r.contact_id, c.contact_name, c.alias_name, c.salutation, r.event_id, e.event_name, e.event_type_id, t.type_code as event_type_code,",
            "t.type_name as event_type_name, e.event_owner_type, e.owner_contact_id, oc.contact_name as owner_contact_name, r.direction, r.amount, r.record_date, r.remark, r.reciprocity_status",
            "from rl_gift_record r",
            "join rl_contact c on c.id = r.contact_id",
            "join rl_event e on e.id = r.event_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "left join rl_contact oc on oc.id = e.owner_contact_id",
            "where r.id = #{recordId} limit 1"
    })
    RecordDetailResponse selectDetailById(@Param("recordId") String recordId);

    @Select("select id, contact_id, event_id, direction, amount, record_date, remark, reciprocity_status, create_time, update_time, user_id from rl_gift_record where id = #{recordId} limit 1")
    GiftRecord selectEntityById(@Param("recordId") String recordId);

    @Insert("insert into rl_gift_record(id, contact_id, event_id, direction, amount, record_date, remark, reciprocity_status, user_id) values(#{id}, #{contactId}, #{eventId}, #{direction}, #{amount}, #{recordDate}, #{remark}, #{reciprocityStatus}, #{userId})")
    int insert(GiftRecord record);

    @Update("update rl_gift_record set contact_id = #{contactId}, event_id = #{eventId}, direction = #{direction}, amount = #{amount}, record_date = #{recordDate}, remark = #{remark}, update_time = now() where id = #{id}")
    int update(GiftRecord record);

    @Update("update rl_gift_record set reciprocity_status = #{status}, update_time = now() where id = #{recordId}")
    int updateReciprocityStatus(@Param("recordId") String recordId, @Param("status") String status);

    @Update("update rl_gift_record set reciprocity_status = #{status}, no_need_reason = #{reason}, update_time = now() where id = #{recordId}")
    int updateReciprocityStatusWithReason(@Param("recordId") String recordId, @Param("status") String status, @Param("reason") String reason);

    @Select("select coalesce(sum(amount), 0) from rl_gift_record where contact_id = #{contactId} and direction = #{direction}")
    BigDecimal sumAmountByContactAndDirection(@Param("contactId") String contactId, @Param("direction") String direction);

    @Select("select max(record_date) from rl_gift_record where contact_id = #{contactId}")
    LocalDate selectLastRecordDateByContactId(@Param("contactId") String contactId);

    @Select("select count(1) from rl_gift_record where contact_id = #{contactId} and reciprocity_status = #{status}")
    Long countByContactAndReciprocityStatus(@Param("contactId") String contactId, @Param("status") String status);

    @Select("select coalesce(sum(amount), 0) from rl_gift_record where event_id = #{eventId} and direction = #{direction} and user_id = #{userId}")
    BigDecimal sumAmountByEventAndDirectionAndUserId(@Param("eventId") String eventId, @Param("direction") String direction, @Param("userId") String userId);

    @Select("select count(distinct contact_id) from rl_gift_record where event_id = #{eventId} and user_id = #{userId}")
    Long countDistinctContactByEventIdAndUserId(@Param("eventId") String eventId, @Param("userId") String userId);

    @Select("select r.id as record_id, r.contact_id, c.contact_name, r.event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name, r.direction, r.amount, r.record_date, r.reciprocity_status from rl_gift_record r join rl_contact c on c.id = r.contact_id join rl_event e on e.id = r.event_id join rl_event_type t on t.id = e.event_type_id order by r.record_date desc, r.id desc limit #{limit}")
    List<RecordPageItemResponse> selectRecent(@Param("limit") int limit);

    @Select("select r.id as record_id, r.contact_id, c.contact_name, r.event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name, r.direction, r.amount, r.record_date, r.reciprocity_status from rl_gift_record r join rl_contact c on c.id = r.contact_id join rl_event e on e.id = r.event_id join rl_event_type t on t.id = e.event_type_id where r.user_id = #{userId} order by r.record_date desc, r.id desc limit #{limit}")
    List<RecordPageItemResponse> selectRecentByUserId(@Param("userId") String userId, @Param("limit") int limit);

    @Select({
            "<script>",
            "select",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) as receive_total_amount,",
            "coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as send_total_amount,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) - coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as net_amount,",
            "count(1) as record_count",
            "from rl_gift_record r join rl_event e on e.id = r.event_id join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  1 = 1",
            "  <if test='userId != null and userId != \"\"'>",
            "    and r.user_id = #{userId}",
            "  </if>",
            "  <if test='contactId != null and contactId != \"\"'>",
            "    and r.contact_id = #{contactId}",
            "  </if>",
            "  <if test='direction != null and direction != \"\"'>",
            "    and r.direction = #{direction}",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
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
    TimelineSummaryResponse selectTimelineSummary(@Param("userId") String userId,
                                                  @Param("contactId") String contactId,
                                                  @Param("direction") String direction,
                                                  @Param("eventTypeCode") String eventTypeCode,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Select("select coalesce(sum(r.amount), 0) from rl_gift_record r join rl_event e on e.id = r.event_id where r.contact_id = #{contactId} and e.event_type_id = #{eventTypeId} and r.direction = #{direction} and r.user_id = #{userId}")
    BigDecimal sumAmountByContactAndEventTypeAndDirection(@Param("userId") String userId,
                                                          @Param("contactId") String contactId,
                                                          @Param("eventTypeId") String eventTypeId,
                                                          @Param("direction") String direction);

    @Select("select count(1) from rl_gift_record r join rl_event e on e.id = r.event_id where r.contact_id = #{contactId} and e.event_type_id = #{eventTypeId} and r.reciprocity_status = #{status} and r.user_id = #{userId}")
    Long countByContactAndEventTypeAndStatus(@Param("userId") String userId,
                                             @Param("contactId") String contactId,
                                             @Param("eventTypeId") String eventTypeId,
                                             @Param("status") String status);

    @Select("select r.id as record_id, r.contact_id, c.contact_name, r.event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name, r.direction, r.amount, r.record_date, r.reciprocity_status from rl_gift_record r join rl_contact c on c.id = r.contact_id join rl_event e on e.id = r.event_id join rl_event_type t on t.id = e.event_type_id where r.contact_id = #{contactId} and e.event_type_id = #{eventTypeId} and r.user_id = #{userId} order by r.record_date desc, r.id desc limit 1")
    RecordPageItemResponse selectLastRecordByContactAndEventType(@Param("userId") String userId,
                                                                 @Param("contactId") String contactId,
                                                                 @Param("eventTypeId") String eventTypeId);

    @Select({
            "select r.id, r.contact_id, r.event_id, r.direction, r.amount, r.record_date, r.remark, r.reciprocity_status, r.create_time, r.update_time",
            "from rl_gift_record r",
            "join rl_event e on e.id = r.event_id",
            "where r.id <> #{recordId}",
            "and r.contact_id = #{contactId}",
            "and e.event_type_id = #{eventTypeId}",
            "and r.direction <> #{direction}",
            "and e.event_owner_type <> #{eventOwnerType}",
            "and r.reciprocity_status = 'UNMATCHED'",
            "and not exists (select 1 from rl_reciprocity_match m where m.match_status = 'ACTIVE' and (m.source_record_id = r.id or m.target_record_id = r.id))",
            "order by r.record_date desc, r.id desc",
            "limit 1"
    })
    GiftRecord selectLatestAutoMatchCandidate(@Param("recordId") String recordId,
                                              @Param("contactId") String contactId,
                                              @Param("eventTypeId") String eventTypeId,
                                              @Param("direction") String direction,
                                              @Param("eventOwnerType") String eventOwnerType);
}

