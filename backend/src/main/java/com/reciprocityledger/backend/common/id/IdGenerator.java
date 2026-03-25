package com.reciprocityledger.backend.common.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * 应用层主键生成器。
 * 当前统一使用雪花算法生成字符串主键，避免依赖数据库自增。
 */
@Component
public class IdGenerator {

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 生成下一个字符串主键。
     * 这样做是为了与数据库中的 varchar 主键保持一致，减少跨层转换成本。
     */
    public String nextId() {
        return String.valueOf(snowflake.nextId());
    }
}
