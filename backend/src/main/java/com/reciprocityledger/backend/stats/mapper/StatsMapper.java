package com.reciprocityledger.backend.stats.mapper;

import com.reciprocityledger.backend.stats.dto.response.StatsByContactItemResponse;
import com.reciprocityledger.backend.stats.dto.response.StatsByEventTypeItemResponse;
import com.reciprocityledger.backend.stats.dto.response.StatsOverviewResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StatsMapper {

    @Select({
            "<script>",
            "select",
            "coalesce(sum(case when direction = 'RECEIVE' then amount else 0 end), 0) as receive_total_amount,",
            "coalesce(sum(case when direction = 'SEND' then amount else 0 end), 0) as send_total_amount,",
            "coalesce(sum(case when direction = 'RECEIVE' then amount else 0 end), 0) - coalesce(sum(case when direction = 'SEND' then amount else 0 end), 0) as net_amount,",
            "count(case when direction = 'RECEIVE' then 1 end) as receive_count,",
            "count(case when direction = 'SEND' then 1 end) as send_count",
            "from rl_gift_record",
            "<where>",
            "  1 = 1",
            "  <if test='startDate != null'>",
            "    and record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "</script>"
    })
    StatsOverviewResponse selectOverview(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select({
            "<script>",
            "select c.id as contact_id, c.contact_name, c.alias_name, c.relation_type,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) as receive_total_amount,",
            "coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as send_total_amount,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) - coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as net_amount,",
            "max(r.record_date) as last_record_date",
            "from rl_contact c join rl_gift_record r on r.contact_id = c.id",
            "<where>",
            "  c.status = 'NORMAL'",
            "  <if test='relationType != null and relationType != \"\"'>",
            "    and c.relation_type = #{relationType}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    and (c.contact_name like concat('%', #{keyword}, '%') or coalesce(c.alias_name, '') like concat('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "group by c.id, c.contact_name, c.alias_name, c.relation_type",
            "order by max(r.record_date) desc, c.id desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<StatsByContactItemResponse> selectByContact(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("relationType") String relationType,
                                                     @Param("keyword") String keyword,
                                                     @Param("offset") int offset,
                                                     @Param("pageSize") int pageSize);

    @Select({
            "<script>",
            "select count(1) from (",
            "select c.id from rl_contact c join rl_gift_record r on r.contact_id = c.id",
            "<where>",
            "  c.status = 'NORMAL'",
            "  <if test='relationType != null and relationType != \"\"'>",
            "    and c.relation_type = #{relationType}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    and (c.contact_name like concat('%', #{keyword}, '%') or coalesce(c.alias_name, '') like concat('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "group by c.id",
            ") tmp",
            "</script>"
    })
    long countByContact(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("relationType") String relationType,
                        @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select t.id as event_type_id, t.type_code as event_type_code, t.type_name as event_type_name,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) as receive_total_amount,",
            "coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as send_total_amount,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) - coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as net_amount,",
            "count(r.id) as record_count",
            "from rl_event_type t join rl_event e on e.event_type_id = t.id join rl_gift_record r on r.event_id = e.id",
            "<where>",
            "  1 = 1",
            "  <if test='startDate != null'>",
            "    and r.record_date &gt;= #{startDate}",
            "  </if>",
            "  <if test='endDate != null'>",
            "    and r.record_date &lt;= #{endDate}",
            "  </if>",
            "</where>",
            "group by t.id, t.type_code, t.type_name",
            "order by count(r.id) desc, t.id asc",
            "</script>"
    })
    List<StatsByEventTypeItemResponse> selectByEventType(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("select count(1) from rl_gift_record where reciprocity_status = 'UNMATCHED'")
    Long countPendingReciprocity();
}
