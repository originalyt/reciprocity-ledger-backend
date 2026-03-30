package com.reciprocityledger.backend.record.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordQuickSaveContactResponse {

    private String contactId;
    private String contactName;
    private String aliasName;
    private String relationType;
}
