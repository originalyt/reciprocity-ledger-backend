package com.reciprocityledger.backend.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventByContactResponse {

    private String contactId;
    private String contactName;
    private List<EventByContactItemResponse> selfEventList;
    private List<EventByContactItemResponse> contactEventList;
}
