package com.reciprocityledger.backend.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 简化的记录保存请求。
 * 不需要关心事件概念，只需提供事件类型即可，后端自动处理事件的创建或复用。
 */
@Data
public class RecordSaveSimpleRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;

    @NotBlank(message = "direction不能为空")
    private String direction;

    @NotNull(message = "amount不能为空")
    private BigDecimal amount;

    @NotNull(message = "recordDate不能为空")
    private LocalDate recordDate;

    @NotBlank(message = "eventTypeId不能为空")
    private String eventTypeId;

    /**
     * 事件名称，可选。
     * 如果不提供，则使用事件类型名称作为事件名称。
     */
    private String eventName;

    private String remark;
}
