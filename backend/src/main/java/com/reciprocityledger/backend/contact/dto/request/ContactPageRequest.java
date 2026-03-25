package com.reciprocityledger.backend.contact.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContactPageRequest extends PageRequest {

    private String keyword;
    private String relationType;
}
