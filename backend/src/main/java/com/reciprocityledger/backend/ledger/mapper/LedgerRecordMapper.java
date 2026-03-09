package com.reciprocityledger.backend.ledger.mapper;

import com.reciprocityledger.backend.ledger.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.ledger.entity.LedgerRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LedgerRecordMapper {

    @Select("select id, user_id, event_exchange_id, record_type, occurred_on, amount, remark, created_at, updated_at, created_by, updated_by from ledger_record where user_id = #{userId} and id = #{recordId} limit 1")
    LedgerRecord selectById(@Param("userId") Long userId, @Param("recordId") Long recordId);

    @Select("select id, user_id, event_exchange_id, record_type, occurred_on, amount, remark, created_at, updated_at, created_by, updated_by from ledger_record where user_id = #{userId} and event_exchange_id = #{eventExchangeId} order by id asc")
    List<LedgerRecord> selectByExchangeId(@Param("userId") Long userId, @Param("eventExchangeId") Long eventExchangeId);

    @Select("select id, user_id, event_exchange_id, record_type, occurred_on, amount, remark, created_at, updated_at, created_by, updated_by from ledger_record where user_id = #{userId} and event_exchange_id = #{eventExchangeId} and record_type = #{recordType} limit 1")
    LedgerRecord selectByExchangeIdAndType(@Param("userId") Long userId,
                                           @Param("eventExchangeId") Long eventExchangeId,
                                           @Param("recordType") String recordType);

    @Insert("insert into ledger_record(id, user_id, event_exchange_id, record_type, occurred_on, amount, remark, created_by, updated_by) values(#{id}, #{userId}, #{eventExchangeId}, #{recordType}, #{occurredOn}, #{amount}, #{remark}, #{createdBy}, #{updatedBy})")
    int insert(LedgerRecord ledgerRecord);

    @Update("update ledger_record set event_exchange_id = #{eventExchangeId}, record_type = #{recordType}, occurred_on = #{occurredOn}, amount = #{amount}, remark = #{remark}, updated_at = now(), updated_by = #{updatedBy} where user_id = #{userId} and id = #{id}")
    int update(LedgerRecord ledgerRecord);

    @Select("select max(lr.occurred_on) from ledger_record lr inner join event_exchange ee on lr.event_exchange_id = ee.id and lr.user_id = ee.user_id where lr.user_id = #{userId} and ee.contact_id = #{contactId}")
    LocalDate selectMaxOccurredOnByContact(@Param("userId") Long userId, @Param("contactId") Long contactId);

    @Select("select lr.id as record_id, lr.event_exchange_id, ee.contact_id, c.name as contact_name, c.relation, lr.record_type, ee.event_type_code, ee.event_note, lr.occurred_on, lr.amount, lr.remark, ee.reciprocity_status from ledger_record lr inner join event_exchange ee on lr.event_exchange_id = ee.id and lr.user_id = ee.user_id inner join contact c on ee.contact_id = c.id and ee.user_id = c.user_id where lr.user_id = #{userId} and lr.id = #{recordId} limit 1")
    RecordDetailResponse selectDetailById(@Param("userId") Long userId, @Param("recordId") Long recordId);
}
