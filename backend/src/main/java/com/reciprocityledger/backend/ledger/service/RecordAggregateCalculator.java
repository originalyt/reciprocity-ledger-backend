package com.reciprocityledger.backend.ledger.service;

import com.reciprocityledger.backend.common.enums.RecordTypeEnum;
import com.reciprocityledger.backend.common.enums.ReciprocityStatusEnum;
import com.reciprocityledger.backend.ledger.entity.LedgerRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 事件聚合快照计算器。
 * 所有 event_exchange 的金额、最近日期和回礼状态统一从明细反算，避免多处重复维护。
 */
@Component
public class RecordAggregateCalculator {

    /**
     * 当前版本每个聚合最多只有一条 GIVE 和一条 RECEIVE 记录，因此直接按类型覆盖即可。
     */
    public EventExchangeSnapshot calculate(List<LedgerRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }
        Long giveRecordId = null;
        Long receiveRecordId = null;
        BigDecimal giveAmount = BigDecimal.ZERO;
        BigDecimal receiveAmount = BigDecimal.ZERO;
        LocalDate latestOccurredOn = null;
        for (LedgerRecord record : records) {
            if (RecordTypeEnum.GIVE.name().equals(record.getRecordType())) {
                giveRecordId = record.getId();
                giveAmount = record.getAmount();
            } else if (RecordTypeEnum.RECEIVE.name().equals(record.getRecordType())) {
                receiveRecordId = record.getId();
                receiveAmount = record.getAmount();
            }
            if (latestOccurredOn == null || record.getOccurredOn().isAfter(latestOccurredOn)) {
                latestOccurredOn = record.getOccurredOn();
            }
        }

        // 回礼状态只看当前聚合内 GIVE / RECEIVE 的存在性，不直接根据金额大小判断。
        String reciprocityStatus;
        if (giveRecordId != null && receiveRecordId != null) {
            reciprocityStatus = ReciprocityStatusEnum.MUTUAL.name();
        } else if (giveRecordId != null) {
            reciprocityStatus = ReciprocityStatusEnum.WAIT_OTHER.name();
        } else {
            reciprocityStatus = ReciprocityStatusEnum.WAIT_ME.name();
        }
        return new EventExchangeSnapshot(giveRecordId, receiveRecordId, giveAmount, receiveAmount, latestOccurredOn, reciprocityStatus);
    }
}
