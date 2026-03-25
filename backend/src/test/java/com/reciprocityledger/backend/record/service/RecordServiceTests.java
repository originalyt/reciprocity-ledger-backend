package com.reciprocityledger.backend.record.service;

import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.contact.service.ContactService;
import com.reciprocityledger.backend.event.entity.GiftEvent;
import com.reciprocityledger.backend.event.service.EventService;
import com.reciprocityledger.backend.reciprocity.service.ReciprocityService;
import com.reciprocityledger.backend.record.dto.request.RecordSaveRequest;
import com.reciprocityledger.backend.record.mapper.RecordMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

class RecordServiceTests {

    @Test
    void saveShouldRejectInvalidDirection() {
        RecordMapper recordMapper = Mockito.mock(RecordMapper.class);
        ContactService contactService = Mockito.mock(ContactService.class);
        EventService eventService = Mockito.mock(EventService.class);
        ReciprocityService reciprocityService = Mockito.mock(ReciprocityService.class);
        IdGenerator idGenerator = Mockito.mock(IdGenerator.class);
        RecordService recordService = new RecordService(recordMapper, contactService, eventService, reciprocityService, idGenerator);

        GiftEvent giftEvent = new GiftEvent();
        giftEvent.setId("event-1");
        giftEvent.setEventOwnerType("SELF");
        Mockito.when(eventService.requireEvent("event-1")).thenReturn(giftEvent);
        Mockito.when(idGenerator.nextId()).thenReturn("record-1");

        RecordSaveRequest request = new RecordSaveRequest();
        request.setContactId("contact-1");
        request.setEventId("event-1");
        request.setDirection("INVALID");
        request.setAmount(new BigDecimal("100"));
        request.setRecordDate(LocalDate.now());

        Assertions.assertThrows(BusinessException.class, () -> recordService.save(request));
    }
}
