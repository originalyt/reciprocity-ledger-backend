package com.reciprocityledger.backend.event.mapper;

import com.reciprocityledger.backend.event.dto.response.EventPageItemResponse;
import com.reciprocityledger.backend.event.entity.GiftEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface EventMapper {

    @Select({
            "<script>",
            "select e.id as event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name,",
            "e.event_owner_type, e.owner_contact_id, c.contact_name as owner_contact_name, e.event_date, count(r.id) as record_count",
            "from rl_event e",
            "join rl_event_type t on t.id = e.event_type_id",
            "left join rl_contact c on c.id = e.owner_contact_id",
            "left join rl_gift_record r on r.event_id = e.id",
            "<where>",
            "  e.status = 'NORMAL'",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    and e.event_name like concat('%', #{keyword}, '%')",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
            "  </if>",
            "  <if test='eventOwnerType != null and eventOwnerType != \"\"'>",
            "    and e.event_owner_type = #{eventOwnerType}",
            "  </if>",
            "  <if test='ownerContactId != null and ownerContactId != \"\"'>",
            "    and e.owner_contact_id = #{ownerContactId}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and e.event_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and e.event_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "group by e.id, e.event_name, e.event_type_id, t.type_code, t.type_name, e.event_owner_type, e.owner_contact_id, c.contact_name, e.event_date",
            "order by e.event_date desc, e.id desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<EventPageItemResponse> selectPage(@Param("keyword") String keyword,
                                           @Param("eventTypeCode") String eventTypeCode,
                                           @Param("eventOwnerType") String eventOwnerType,
                                           @Param("ownerContactId") String ownerContactId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("offset") int offset,
                                           @Param("pageSize") int pageSize);

    @Select({
            "<script>",
            "select count(1) from rl_event e join rl_event_type t on t.id = e.event_type_id",
            "<where>",
            "  e.status = 'NORMAL'",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    and e.event_name like concat('%', #{keyword}, '%')",
            "  </if>",
            "  <if test='eventTypeCode != null and eventTypeCode != \"\"'>",
            "    and t.type_code = #{eventTypeCode}",
            "  </if>",
            "  <if test='eventOwnerType != null and eventOwnerType != \"\"'>",
            "    and e.event_owner_type = #{eventOwnerType}",
            "  </if>",
            "  <if test='ownerContactId != null and ownerContactId != \"\"'>",
            "    and e.owner_contact_id = #{ownerContactId}",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and e.event_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and e.event_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countPage(@Param("keyword") String keyword,
                   @Param("eventTypeCode") String eventTypeCode,
                   @Param("eventOwnerType") String eventOwnerType,
                   @Param("ownerContactId") String ownerContactId,
                   @Param("startDate") LocalDate startDate,
                   @Param("endDate") LocalDate endDate);

    @Select("select id, event_name, event_type_id, event_owner_type, owner_contact_id, event_date, remark, status, create_time, update_time from rl_event where id = #{eventId} limit 1")
    GiftEvent selectById(@Param("eventId") String eventId);

    @Insert("insert into rl_event(id, event_name, event_type_id, event_owner_type, owner_contact_id, event_date, remark, status) values(#{id}, #{eventName}, #{eventTypeId}, #{eventOwnerType}, #{ownerContactId}, #{eventDate}, #{remark}, #{status})")
    int insert(GiftEvent event);

    @Update("update rl_event set event_name = #{eventName}, event_type_id = #{eventTypeId}, event_owner_type = #{eventOwnerType}, owner_contact_id = #{ownerContactId}, event_date = #{eventDate}, remark = #{remark}, update_time = now() where id = #{id}")
    int update(GiftEvent event);

    @Select({
            "select e.id as event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name,",
            "e.event_owner_type, e.owner_contact_id, c.contact_name as owner_contact_name, e.event_date, count(r.id) as record_count",
            "from rl_event e",
            "join rl_event_type t on t.id = e.event_type_id",
            "left join rl_contact c on c.id = e.owner_contact_id",
            "left join rl_gift_record r on r.event_id = e.id",
            "where e.id = #{eventId}",
            "group by e.id, e.event_name, e.event_type_id, t.type_code, t.type_name, e.event_owner_type, e.owner_contact_id, c.contact_name, e.event_date"
    })
    EventPageItemResponse selectSummaryById(@Param("eventId") String eventId);

    @Select("select e.id as event_id, e.event_name, e.event_type_id, t.type_code as event_type_code, t.type_name as event_type_name, e.event_owner_type, e.owner_contact_id, c.contact_name as owner_contact_name, e.event_date, 0 as record_count from rl_event e join rl_event_type t on t.id = e.event_type_id left join rl_contact c on c.id = e.owner_contact_id where e.status = 'NORMAL' order by e.event_date desc, e.id desc limit #{limit}")
    List<EventPageItemResponse> selectRecent(@Param("limit") int limit);
}
