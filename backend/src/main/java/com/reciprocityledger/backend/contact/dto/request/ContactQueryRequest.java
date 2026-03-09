package com.reciprocityledger.backend.contact.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContactQueryRequest extends BaseAccessTokenRequest {

    private String keyword;
    private String relation;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
