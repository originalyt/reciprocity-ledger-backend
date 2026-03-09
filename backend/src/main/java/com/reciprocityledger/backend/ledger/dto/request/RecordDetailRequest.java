package com.reciprocityledger.backend.ledger.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecordDetailRequest extends BaseAccessTokenRequest {

    @NotNull(message = "recordId不能为空")
    private Long recordId;
}
