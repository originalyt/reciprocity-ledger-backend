package com.reciprocityledger.backend.dict.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.ListResponse;
import com.reciprocityledger.backend.dict.dto.request.EventTypeListRequest;
import com.reciprocityledger.backend.dict.dto.request.RelationTypeListRequest;
import com.reciprocityledger.backend.dict.dto.response.DictItemResponse;
import com.reciprocityledger.backend.dict.entity.EventTypeDict;
import com.reciprocityledger.backend.dict.mapper.EventTypeDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictService {

    private static final List<DictItemResponse> RELATION_TYPES = Arrays.asList(
            new DictItemResponse("RELATIVE", "RELATIVE", "亲戚"),
            new DictItemResponse("FRIEND", "FRIEND", "朋友"),
            new DictItemResponse("COLLEAGUE", "COLLEAGUE", "同事"),
            new DictItemResponse("CLASSMATE", "CLASSMATE", "同学"),
            new DictItemResponse("NEIGHBOR", "NEIGHBOR", "邻居"),
            new DictItemResponse("OTHER", "OTHER", "其他")
    );

    private final EventTypeDictMapper eventTypeDictMapper;

    public ListResponse<DictItemResponse> listEventTypes(EventTypeListRequest request) {
        return ListResponse.of(eventTypeDictMapper.selectDictItems(request.getEnabledFlag()));
    }

    public ListResponse<DictItemResponse> listRelationTypes(RelationTypeListRequest request) {
        String keyword = StrUtil.trim(request.getKeyword());
        if (StrUtil.isBlank(keyword)) {
            return ListResponse.of(RELATION_TYPES);
        }
        List<DictItemResponse> result = new ArrayList<>();
        for (DictItemResponse item : RELATION_TYPES) {
            if (StrUtil.contains(item.getCode(), keyword) || StrUtil.contains(item.getName(), keyword)) {
                result.add(item);
            }
        }
        return ListResponse.of(result);
    }

    public EventTypeDict getEventType(String eventTypeId) {
        return eventTypeDictMapper.selectById(eventTypeId);
    }
}
