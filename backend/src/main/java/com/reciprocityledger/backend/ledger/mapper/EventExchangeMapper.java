package com.reciprocityledger.backend.ledger.mapper;

import com.reciprocityledger.backend.ledger.entity.EventExchange;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EventExchangeMapper {

    @Select("select id, user_id, contact_id, event_type_code, event_note, give_record_id, receive_record_id, give_amount, receive_amount, net_amount, latest_occurred_on, reciprocity_status, created_at, updated_at, created_by, updated_by from event_exchange where user_id = #{userId} and id = #{exchangeId} limit 1")
    EventExchange selectById(@Param("userId") Long userId, @Param("exchangeId") Long exchangeId);

    @Select("select id, user_id, contact_id, event_type_code, event_note, give_record_id, receive_record_id, give_amount, receive_amount, net_amount, latest_occurred_on, reciprocity_status, created_at, updated_at, created_by, updated_by from event_exchange where user_id = #{userId} and contact_id = #{contactId} and event_type_code = #{eventTypeCode} and event_note = #{eventNote} limit 1")
    EventExchange selectByKey(@Param("userId") Long userId,
                              @Param("contactId") Long contactId,
                              @Param("eventTypeCode") String eventTypeCode,
                              @Param("eventNote") String eventNote);

    @Insert("insert into event_exchange(id, user_id, contact_id, event_type_code, event_note, give_record_id, receive_record_id, give_amount, receive_amount, latest_occurred_on, reciprocity_status, created_by, updated_by) values(#{id}, #{userId}, #{contactId}, #{eventTypeCode}, #{eventNote}, #{giveRecordId}, #{receiveRecordId}, #{giveAmount}, #{receiveAmount}, #{latestOccurredOn}, #{reciprocityStatus}, #{createdBy}, #{updatedBy})")
    int insert(EventExchange eventExchange);

    @Update("update event_exchange set give_record_id = #{giveRecordId}, receive_record_id = #{receiveRecordId}, give_amount = #{giveAmount}, receive_amount = #{receiveAmount}, latest_occurred_on = #{latestOccurredOn}, reciprocity_status = #{reciprocityStatus}, updated_at = now(), updated_by = #{updatedBy} where user_id = #{userId} and id = #{id}")
    int updateSnapshot(EventExchange eventExchange);

    @Delete("delete from event_exchange where user_id = #{userId} and id = #{exchangeId}")
    int deleteById(@Param("userId") Long userId, @Param("exchangeId") Long exchangeId);
}
