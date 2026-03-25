package com.reciprocityledger.backend.common.api;

import lombok.Data;

@Data
public class PageRequest {

    private Integer pageNo;
    private Integer pageSize;
}
