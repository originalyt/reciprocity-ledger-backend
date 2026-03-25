package com.reciprocityledger.backend.contact.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactSaveRequest {

    @NotBlank(message = "contactName不能为空")
    private String contactName;
    private String aliasName;
    private String salutation;
    private String mobile;
    private String relationType;
    private String remark;
}
