package com.reciprocityledger.backend.contact.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactUpdateRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;
    @NotBlank(message = "contactName不能为空")
    private String contactName;
    private String aliasName;
    private String salutation;
    private String mobile;
    private String relationType;
    private String remark;
}
