package com.reciprocityledger.backend.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 删除事件关联请求。
 */
@Data
public class EventRelationDeleteRequest {

    @NotBlank(message = "id不能为空")
    private String id;
}
