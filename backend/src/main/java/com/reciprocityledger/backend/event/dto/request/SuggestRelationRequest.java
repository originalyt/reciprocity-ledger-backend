package com.reciprocityledger.backend.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 查询可关联事件建议请求。
 */
@Data
public class SuggestRelationRequest {

    @NotBlank(message = "selfEventId不能为空")
    private String selfEventId;
}
