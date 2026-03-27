package com.reciprocityledger.backend.dict.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DictItemResponse {

    private String id;
    private String code;
    private String name;

    public DictItemResponse(String code, String name) {
        this.id = code;
        this.code = code;
        this.name = name;
    }

    public DictItemResponse(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
}
