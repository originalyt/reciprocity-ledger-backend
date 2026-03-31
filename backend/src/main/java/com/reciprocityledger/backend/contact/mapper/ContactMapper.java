package com.reciprocityledger.backend.contact.mapper;

import com.reciprocityledger.backend.contact.dto.response.ContactPageItemResponse;
import com.reciprocityledger.backend.contact.entity.Contact;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ContactMapper {

    @Select({
            "<script>",
            "select c.id as contact_id, c.contact_name, c.alias_name, c.salutation, c.mobile, c.relation_type,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) as receive_total_amount,",
            "coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as send_total_amount,",
            "coalesce(sum(case when r.direction = 'RECEIVE' then r.amount else 0 end), 0) - coalesce(sum(case when r.direction = 'SEND' then r.amount else 0 end), 0) as net_amount,",
            "max(r.record_date) as last_record_date",
            "from rl_contact c",
            "left join rl_gift_record r on r.contact_id = c.id",
            "<where>",
            "  c.status = 'NORMAL'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and c.user_id = #{userId}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    and (c.contact_name like concat('%', #{keyword}, '%') or coalesce(c.alias_name, '') like concat('%', #{keyword}, '%') or coalesce(c.mobile, '') like concat('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='relationType != null and relationType != \"\"'>",
            "    and c.relation_type = #{relationType}",
            "  </if>",
            "</where>",
            "group by c.id, c.contact_name, c.alias_name, c.salutation, c.mobile, c.relation_type",
            "order by max(r.record_date) desc nulls last, c.id desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<ContactPageItemResponse> selectPage(@Param("keyword") String keyword,
                                             @Param("relationType") String relationType,
                                             @Param("userId") String userId,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    @Select({
            "<script>",
            "select count(1) from rl_contact c",
            "<where>",
            "  c.status = 'NORMAL'",
            "  <if test='userId != null and userId != \"\"'>",
            "    and c.user_id = #{userId}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    and (c.contact_name like concat('%', #{keyword}, '%') or coalesce(c.alias_name, '') like concat('%', #{keyword}, '%') or coalesce(c.mobile, '') like concat('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='relationType != null and relationType != \"\"'>",
            "    and c.relation_type = #{relationType}",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countPage(@Param("keyword") String keyword, @Param("relationType") String relationType, @Param("userId") String userId);

    @Select("select id, contact_name, alias_name, salutation, mobile, relation_type, remark, status, create_time, update_time, user_id from rl_contact where id = #{contactId} limit 1")
    Contact selectById(@Param("contactId") String contactId);

    @Insert("insert into rl_contact(id, contact_name, alias_name, salutation, mobile, relation_type, remark, status, user_id) values(#{id}, #{contactName}, #{aliasName}, #{salutation}, #{mobile}, #{relationType}, #{remark}, #{status}, #{userId})")
    int insert(Contact contact);

    @Update("update rl_contact set contact_name = #{contactName}, alias_name = #{aliasName}, salutation = #{salutation}, mobile = #{mobile}, relation_type = #{relationType}, remark = #{remark}, update_time = now() where id = #{id}")
    int update(Contact contact);
}
