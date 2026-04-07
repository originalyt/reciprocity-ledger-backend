package com.reciprocityledger.backend.common.enums;

public enum ReciprocityStatusEnum {

    UNMATCHED,          // 待往来
    MATCHED,            // 已往来（自动匹配）
    MANUAL_CANCELED,    // 已取消
    MANUAL_CONFIRMED,   // 手动确认
    NO_NEED             // 无需往来
}
