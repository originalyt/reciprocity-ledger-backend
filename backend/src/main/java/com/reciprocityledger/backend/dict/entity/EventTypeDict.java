package com.reciprocityledger.backend.dict.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件类型字典实体。
 * 映射数据库表：rl_event_type。
 */
@Data
public class EventTypeDict {

    /**
     * 主键，事件类型唯一标识。
     */
    private String id;

    /**
     * 业务唯一键，事件类型编码。
     */
    private String typeCode;

    /**
     * 事件类型名称。
     */
    private String typeName;

    /**
     * 排序号，用于保证事件类型在选择器中的稳定展示顺序。
     */
    private Integer sortNo;

    /**
     * 是否启用，控制该类型是否允许继续用于新增事件。
     */
    private Boolean enabledFlag;

    /**
     * 是否系统内置，用于区分预置类型和后续可能扩展的自定义类型。
     */
    private Boolean builtInFlag;

    /**
     * 备注，用于补充该事件类型的业务说明。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
