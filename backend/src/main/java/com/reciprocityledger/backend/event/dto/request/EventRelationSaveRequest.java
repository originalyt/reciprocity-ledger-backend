package com.reciprocityledger.backend.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建事件关联请求。
 */
@Data
public class EventRelationSaveRequest {

    @NotBlank(message = "selfEventId不能为空")
    private String selfEventId;

    @NotBlank(message = "contactEventId不能为空")
    private String contactEventId;
}
