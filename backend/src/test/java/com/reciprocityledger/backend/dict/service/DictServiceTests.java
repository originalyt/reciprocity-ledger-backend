package com.reciprocityledger.backend.dict.service;

import com.reciprocityledger.backend.common.api.ListResponse;
import com.reciprocityledger.backend.dict.dto.request.RelationTypeListRequest;
import com.reciprocityledger.backend.dict.dto.response.DictItemResponse;
import com.reciprocityledger.backend.dict.mapper.EventTypeDictMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DictServiceTests {

    @Test
    void listRelationTypesShouldSupportKeywordFilter() {
        DictService dictService = new DictService(Mockito.mock(EventTypeDictMapper.class));
        RelationTypeListRequest request = new RelationTypeListRequest();
        request.setKeyword("亲");

        ListResponse<DictItemResponse> response = dictService.listRelationTypes(request);

        Assertions.assertEquals(1, response.getList().size());
        Assertions.assertEquals("亲戚", response.getList().get(0).getName());
    }
}
