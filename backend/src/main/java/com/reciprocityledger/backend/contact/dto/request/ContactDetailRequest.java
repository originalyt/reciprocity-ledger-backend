package com.reciprocityledger.backend.contact.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactDetailRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;
}
