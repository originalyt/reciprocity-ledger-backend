package com.reciprocityledger.backend.common.enums;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;

public enum RecordTypeEnum {

    GIVE,
    RECEIVE;

    public static RecordTypeEnum of(String code) {
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "recordType不能为空");
        }
        try {
            return RecordTypeEnum.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "recordType不合法");
        }
    }
}
