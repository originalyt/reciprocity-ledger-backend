package com.reciprocityledger.backend.reciprocity.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闭环匹配实体。
 * 映射数据库表：rl_reciprocity_match。
 */
@Data
public class ReciprocityMatch {

    /**
     * 主键，闭环匹配唯一标识。
     */
    private String id;

    /**
     * 源记录主键。
     */
    private String sourceRecordId;

    /**
     * 目标记录主键。
     */
    private String targetRecordId;

    /**
     * 匹配类型，区分自动识别和人工确认。
     */
    private String matchType;

    /**
     * 匹配状态，控制这条闭环关系是否仍然有效。
     */
    private String matchStatus;

    /**
     * 取消原因，用于说明为什么手工取消闭环。
     */
    private String cancelReason;

    /**
     * 备注，用于补充人工确认或取消时的业务说明。
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
