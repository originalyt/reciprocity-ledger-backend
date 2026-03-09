package com.reciprocityledger.backend.common.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * 应用层主键生成器。
 * 当前统一使用雪花算法生成 Long 类型主键，避免依赖数据库自增。
 */
@Component
public class IdGenerator {

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 生成下一个 Long 类型主键。
     */
    public Long nextId() {
        return snowflake.nextId();
    }
}
