package com.reciprocityledger.backend.ledger.entity;

import lombok.Data;

/**
 * 事件类型字典实体。
 * 映射数据库表：event_type_dict。
 */
@Data
public class EventTypeDict {

    /**
     * 主键ID，对应表主键 event_type_dict.id，由脚本初始化或应用层生成，非自增。
     */
    private Long id;

    /**
     * 事件类型编码，业务唯一。
     */
    private String code;

    /**
     * 事件类型名称。
     */
    private String name;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 是否内置。
     */
    private Boolean builtIn;
}
