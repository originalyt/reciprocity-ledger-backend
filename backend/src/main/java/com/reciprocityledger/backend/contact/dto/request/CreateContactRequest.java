package com.reciprocityledger.backend.contact.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateContactRequest extends BaseAccessTokenRequest {

    @NotBlank(message = "name不能为空")
    private String name;

    private String relation;

    private String phone;

    private String note;
}
