package com.reciprocityledger.backend.event.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询未关联事件请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnlinkedEventsRequest extends PageRequest {

    /**
     * 事件类型ID，可选筛选条件。
     */
    private String eventTypeId;
}
