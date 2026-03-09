package com.reciprocityledger.backend.contact.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContactDetailRequest extends BaseAccessTokenRequest {

    @NotNull(message = "contactId不能为空")
    private Long contactId;
}
