package com.reciprocityledger.backend.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListResponse<T> {

    private List<T> list;

    public static <T> ListResponse<T> of(List<T> list) {
        return new ListResponse<>(list);
    }
}
