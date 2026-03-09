package com.reciprocityledger.backend.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> list;
    private Integer pageNo;
    private Integer pageSize;
    private Long total;
    private Boolean hasMore;

    public static <T> PageResponse<T> of(List<T> list, int pageNo, int pageSize, long total) {
        return new PageResponse<>(list, pageNo, pageSize, total, (long) pageNo * pageSize < total);
    }
}
