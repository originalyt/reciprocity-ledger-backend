package com.reciprocityledger.backend.event.mapper;

import com.reciprocityledger.backend.event.dto.response.EventRelationVO;
import com.reciprocityledger.backend.event.dto.response.SuggestRelationVO;
import com.reciprocityledger.backend.event.dto.response.UnlinkedEventVO;
import com.reciprocityledger.backend.event.entity.EventRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EventRelationMapper {

    @Insert("insert into rl_event_relation(id, self_event_id, contact_event_id, user_id) values(#{id}, #{selfEventId}, #{contactEventId}, #{userId})")
    int insert(EventRelation relation);

    @Delete("delete from rl_event_relation where id = #{id} and user_id = #{userId}")
    int deleteById(@Param("id") String id, @Param("userId") String userId);

    @Select("select id, self_event_id, contact_event_id, user_id, create_time, update_time from rl_event_relation where id = #{id}")
    EventRelation selectById(@Param("id") String id);

    @Select("select id, self_event_id, contact_event_id, user_id, create_time, update_time from rl_event_relation where self_event_id = #{selfEventId} and contact_event_id = #{contactEventId}")
    EventRelation selectByEvents(@Param("selfEventId") String selfEventId, @Param("contactEventId") String contactEventId);

    @Select("select id from rl_event_relation where contact_event_id = #{contactEventId} limit 1")
    String selectIdByContactEventId(@Param("contactEventId") String contactEventId);

    /**
     * 查询某个SELF事件关联的所有CONTACT事件。
     */
    @Select({
            "select er.id, er.contact_event_id as contactEventId, e.event_name as eventName, e.event_date as eventDate,",
            "c.contact_name as contactName, coalesce(sum(r.amount), 0) as sendAmount",
            "from rl_event_relation er",
            "join rl_event e on e.id = er.contact_event_id",
            "left join rl_contact c on c.id = e.owner_contact_id",
            "left join rl_gift_record r on r.event_id = e.id and r.direction = 'SEND'",
            "where er.self_event_id = #{selfEventId} and er.user_id = #{userId}",
            "group by er.id, er.contact_event_id, e.event_name, e.event_date, c.contact_name",
            "order by e.event_date desc"
    })
    List<EventRelationVO> selectRelationList(@Param("selfEventId") String selfEventId, @Param("userId") String userId);

    /**
     * 查询可关联的CONTACT事件建议（同类型且未关联）。
     */
    @Select({
            "select e.id as eventId, e.event_name as eventName, e.event_date as eventDate,",
            "c.contact_name as contactName, coalesce(sum(r.amount), 0) as sendAmount",
            "from rl_event e",
            "join rl_contact c on c.id = e.owner_contact_id",
            "left join rl_gift_record r on r.event_id = e.id and r.direction = 'SEND'",
            "where e.event_owner_type = 'CONTACT'",
            "  and e.event_type_id = #{eventTypeId}",
            "  and e.user_id = #{userId}",
            "  and e.status = 'NORMAL'",
            "  and not exists (select 1 from rl_event_relation er where er.contact_event_id = e.id)",
            "group by e.id, e.event_name, e.event_date, c.contact_name",
            "order by e.event_date desc"
    })
    List<SuggestRelationVO> selectSuggestRelations(@Param("eventTypeId") String eventTypeId, @Param("userId") String userId);

    /**
     * 查询未关联的CONTACT事件（待还提醒），分页。
     */
    @Select({
            "<script>",
            "select e.id as eventId, e.event_name as eventName, e.event_type_id as eventTypeId, t.type_name as eventTypeName,",
            "e.event_date as eventDate, e.owner_contact_id as contactId, c.contact_name as contactName,",
            "coalesce(sum(r.amount), 0) as sendAmount",
            "from rl_event e",
            "join rl_contact c on c.id = e.owner_contact_id",
            "join rl_event_type t on t.id = e.event_type_id",
            "left join rl_gift_record r on r.event_id = e.id and r.direction = 'SEND'",
            "<where>",
            "  e.event_owner_type = 'CONTACT'",
            "  and e.user_id = #{userId}",
            "  and e.status = 'NORMAL'",
            "  and not exists (select 1 from rl_event_relation er where er.contact_event_id = e.id)",
            "  <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "    and e.event_type_id = #{eventTypeId}",
            "  </if>",
            "</where>",
            "group by e.id, e.event_name, e.event_type_id, t.type_name, e.event_date, e.owner_contact_id, c.contact_name",
            "order by sendAmount desc, e.event_date desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<UnlinkedEventVO> selectUnlinkedEvents(@Param("userId") String userId,
                                                @Param("eventTypeId") String eventTypeId,
                                                @Param("offset") int offset,
                                                @Param("pageSize") int pageSize);

    /**
     * 统计未关联的CONTACT事件总数。
     */
    @Select({
            "<script>",
            "select count(1) from (",
            "  select e.id",
            "  from rl_event e",
            "  join rl_contact c on c.id = e.owner_contact_id",
            "  left join rl_gift_record r on r.event_id = e.id and r.direction = 'SEND'",
            "  <where>",
            "    e.event_owner_type = 'CONTACT'",
            "    and e.user_id = #{userId}",
            "    and e.status = 'NORMAL'",
            "    and not exists (select 1 from rl_event_relation er where er.contact_event_id = e.id)",
            "    <if test='eventTypeId != null and eventTypeId != \"\"'>",
            "      and e.event_type_id = #{eventTypeId}",
            "    </if>",
            "  </where>",
            "  group by e.id",
            ") t",
            "</script>"
    })
    long countUnlinkedEvents(@Param("userId") String userId, @Param("eventTypeId") String eventTypeId);
}
