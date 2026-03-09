package com.reciprocityledger.backend.contact.mapper;

import com.reciprocityledger.backend.contact.dto.response.ContactSummaryResponse;
import com.reciprocityledger.backend.contact.entity.Contact;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ContactMapper {

    @Select({
            "<script>",
            "select id, name, relation, phone, last_interaction_on from contact",
            "where user_id = #{userId}",
            "<if test='keyword != null and keyword != \"\"'>",
            "  and (name like concat('%', #{keyword}, '%') or coalesce(phone, '') like concat('%', #{keyword}, '%'))",
            "</if>",
            "<if test='relation != null and relation != \"\"'>",
            "  and relation = #{relation}",
            "</if>",
            "order by last_interaction_on desc nulls last, id desc",
            "offset #{offset} limit #{pageSize}",
            "</script>"
    })
    List<ContactSummaryResponse> selectPage(@Param("userId") Long userId,
                                            @Param("keyword") String keyword,
                                            @Param("relation") String relation,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    @Select({
            "<script>",
            "select count(1) from contact",
            "where user_id = #{userId}",
            "<if test='keyword != null and keyword != \"\"'>",
            "  and (name like concat('%', #{keyword}, '%') or coalesce(phone, '') like concat('%', #{keyword}, '%'))",
            "</if>",
            "<if test='relation != null and relation != \"\"'>",
            "  and relation = #{relation}",
            "</if>",
            "</script>"
    })
    long countPage(@Param("userId") Long userId,
                   @Param("keyword") String keyword,
                   @Param("relation") String relation);

    @Select("select id, user_id, name, relation, phone, note, last_interaction_on, created_at, updated_at, created_by, updated_by from contact where user_id = #{userId} and id = #{contactId} limit 1")
    Contact selectById(@Param("userId") Long userId, @Param("contactId") Long contactId);

    @Insert("insert into contact(id, user_id, name, relation, phone, note, last_interaction_on, created_by, updated_by) values(#{id}, #{userId}, #{name}, #{relation}, #{phone}, #{note}, #{lastInteractionOn}, #{createdBy}, #{updatedBy})")
    int insert(Contact contact);

    @Update("update contact set name = #{name}, relation = #{relation}, phone = #{phone}, note = #{note}, updated_at = now(), updated_by = #{updatedBy} where user_id = #{userId} and id = #{id}")
    int update(Contact contact);

    @Update("update contact set last_interaction_on = #{lastInteractionOn}, updated_at = now(), updated_by = #{userId} where user_id = #{userId} and id = #{contactId}")
    int updateLastInteractionOn(@Param("userId") Long userId, @Param("contactId") Long contactId, @Param("lastInteractionOn") LocalDate lastInteractionOn);
}
