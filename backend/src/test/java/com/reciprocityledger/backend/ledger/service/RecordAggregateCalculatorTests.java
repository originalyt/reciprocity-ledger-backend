package com.reciprocityledger.backend.ledger.service;

import com.reciprocityledger.backend.common.enums.RecordTypeEnum;
import com.reciprocityledger.backend.common.enums.ReciprocityStatusEnum;
import com.reciprocityledger.backend.ledger.entity.LedgerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

class RecordAggregateCalculatorTests {

    private final RecordAggregateCalculator calculator = new RecordAggregateCalculator();

    @Test
    void shouldCalculateMutualSnapshot() {
        LedgerRecord giveRecord = buildRecord(1L, RecordTypeEnum.GIVE.name(), LocalDate.of(2026, 3, 1), new BigDecimal("200.00"));
        LedgerRecord receiveRecord = buildRecord(2L, RecordTypeEnum.RECEIVE.name(), LocalDate.of(2026, 3, 3), new BigDecimal("500.00"));

        EventExchangeSnapshot snapshot = calculator.calculate(Arrays.asList(giveRecord, receiveRecord));

        Assertions.assertNotNull(snapshot);
        Assertions.assertEquals(1L, snapshot.getGiveRecordId());
        Assertions.assertEquals(2L, snapshot.getReceiveRecordId());
        Assertions.assertEquals(new BigDecimal("200.00"), snapshot.getGiveAmount());
        Assertions.assertEquals(new BigDecimal("500.00"), snapshot.getReceiveAmount());
        Assertions.assertEquals(LocalDate.of(2026, 3, 3), snapshot.getLatestOccurredOn());
        Assertions.assertEquals(ReciprocityStatusEnum.MUTUAL.name(), snapshot.getReciprocityStatus());
    }

    @Test
    void shouldCalculateWaitOtherWhenOnlyGiveExists() {
        LedgerRecord giveRecord = buildRecord(3L, RecordTypeEnum.GIVE.name(), LocalDate.of(2026, 2, 8), new BigDecimal("88.00"));

        EventExchangeSnapshot snapshot = calculator.calculate(Collections.singletonList(giveRecord));

        Assertions.assertNotNull(snapshot);
        Assertions.assertEquals(3L, snapshot.getGiveRecordId());
        Assertions.assertNull(snapshot.getReceiveRecordId());
        Assertions.assertEquals(new BigDecimal("88.00"), snapshot.getGiveAmount());
        Assertions.assertEquals(BigDecimal.ZERO, snapshot.getReceiveAmount());
        Assertions.assertEquals(ReciprocityStatusEnum.WAIT_OTHER.name(), snapshot.getReciprocityStatus());
    }

    @Test
    void shouldReturnNullWhenRecordsEmpty() {
        Assertions.assertNull(calculator.calculate(Collections.emptyList()));
    }

    private LedgerRecord buildRecord(Long id, String recordType, LocalDate occurredOn, BigDecimal amount) {
        LedgerRecord ledgerRecord = new LedgerRecord();
        ledgerRecord.setId(id);
        ledgerRecord.setRecordType(recordType);
        ledgerRecord.setOccurredOn(occurredOn);
        ledgerRecord.setAmount(amount);
        return ledgerRecord;
    }
}
