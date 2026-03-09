package com.reciprocityledger.backend.contact.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateContactRequest extends BaseAccessTokenRequest {

    @NotNull(message = "contactId不能为空")
    private Long contactId;

    @NotBlank(message = "name不能为空")
    private String name;

    private String relation;

    private String phone;

    private String note;
}
