package com.reciprocityledger.backend.stats.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface StatsMapper {

    @Select({
            "<script>",
            "select coalesce(sum(case when record_type = 'GIVE' then amount else 0 end), 0) from ledger_record",
            "where user_id = #{userId}",
            "<if test='startDate != null'>",
            "  and occurred_on <![CDATA[>=]]> #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  and occurred_on <![CDATA[<=]]> #{endDate}",
            "</if>",
            "</script>"
    })
    BigDecimal sumGiveAmount(@Param("userId") Long userId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    @Select({
            "<script>",
            "select coalesce(sum(case when record_type = 'RECEIVE' then amount else 0 end), 0) from ledger_record",
            "where user_id = #{userId}",
            "<if test='startDate != null'>",
            "  and occurred_on <![CDATA[>=]]> #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  and occurred_on <![CDATA[<=]]> #{endDate}",
            "</if>",
            "</script>"
    })
    BigDecimal sumReceiveAmount(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
