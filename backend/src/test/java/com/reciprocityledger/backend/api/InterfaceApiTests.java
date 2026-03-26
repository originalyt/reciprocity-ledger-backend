package com.reciprocityledger.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InterfaceApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        JsonNode response = postJson("/health", Map.of());

        assertSuccess(response);
        Assertions.assertEquals("ok", response.at("/data/status").asText());
    }

    @Test
    void dictEndpointsShouldReturnEventTypesAndRelationTypes() throws Exception {
        String marker = marker();
        insertEventType("it-event-type-" + marker, "IT_TYPE_" + marker, "接口测试类型" + marker, true);

        JsonNode eventTypeResponse = postJson("/app/dict/event-type/list", Map.of("enabledFlag", true));
        assertSuccess(eventTypeResponse);
        Assertions.assertTrue(containsByField(eventTypeResponse.at("/data/list"), "code", "IT_TYPE_" + marker));

        JsonNode relationTypeResponse = postJson("/app/dict/relation-type/list", Map.of("keyword", "亲"));
        assertSuccess(relationTypeResponse);
        Assertions.assertTrue(containsByField(relationTypeResponse.at("/data/list"), "name", "亲戚"));
    }

    @Test
    void contactEndpointsShouldSupportSaveUpdateDetailAndPage() throws Exception {
        String marker = marker();
        String contactName = "接口联系人" + marker;

        String contactId = saveContact(contactName, "别名" + marker, "叔叔", "13800138000", "RELATIVE", "初始备注" + marker);

        JsonNode updateResponse = postJson("/app/contact/update", mapOf(
                "contactId", contactId,
                "contactName", contactName + "更新",
                "aliasName", "新别名" + marker,
                "salutation", "伯伯",
                "mobile", "13900139000",
                "relationType", "FRIEND",
                "remark", "更新备注" + marker
        ));
        assertSuccess(updateResponse);
        Assertions.assertEquals(contactId, updateResponse.at("/data/id").asText());

        JsonNode detailResponse = postJson("/app/contact/detail", Map.of("contactId", contactId));
        assertSuccess(detailResponse);
        Assertions.assertEquals(contactId, detailResponse.at("/data/contactId").asText());
        Assertions.assertEquals(contactName + "更新", detailResponse.at("/data/contactName").asText());
        Assertions.assertEquals("FRIEND", detailResponse.at("/data/relationType").asText());
        assertBigDecimal(detailResponse.at("/data/receiveTotalAmount"), "0");
        assertBigDecimal(detailResponse.at("/data/sendTotalAmount"), "0");

        JsonNode pageResponse = postJson("/app/contact/page", mapOf(
                "keyword", marker,
                "relationType", "FRIEND",
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(pageResponse);
        Assertions.assertTrue(pageResponse.at("/data/total").asLong() >= 1);
        Assertions.assertTrue(containsByField(pageResponse.at("/data/list"), "contactId", contactId));
    }

    @Test
    void eventEndpointsShouldSupportSaveUpdateDetailAndPage() throws Exception {
        String marker = marker();
        String eventTypeId = "it-event-type-" + marker;
        insertEventType(eventTypeId, "IT_EVENT_" + marker, "事件接口类型" + marker, true);
        String contactId = saveContact("事件联系人" + marker, "事件别名" + marker, "老师", "13700137000", "FRIEND", "事件联系人备注");

        String eventId = saveEvent("事件" + marker, eventTypeId, "SELF", null, LocalDate.of(2099, 10, 1), "事件初始备注" + marker);

        JsonNode updateResponse = postJson("/app/event/update", mapOf(
                "eventId", eventId,
                "eventName", "事件更新" + marker,
                "eventTypeId", eventTypeId,
                "eventOwnerType", "CONTACT",
                "ownerContactId", contactId,
                "eventDate", "2099-10-02",
                "remark", "事件更新备注" + marker
        ));
        assertSuccess(updateResponse);
        Assertions.assertEquals(eventId, updateResponse.at("/data/id").asText());

        JsonNode detailResponse = postJson("/app/event/detail", Map.of("eventId", eventId));
        assertSuccess(detailResponse);
        Assertions.assertEquals(eventId, detailResponse.at("/data/eventId").asText());
        Assertions.assertEquals("事件更新" + marker, detailResponse.at("/data/eventName").asText());
        Assertions.assertEquals("CONTACT", detailResponse.at("/data/eventOwnerType").asText());
        Assertions.assertEquals(contactId, detailResponse.at("/data/ownerContactId").asText());
        Assertions.assertEquals("IT_EVENT_" + marker, detailResponse.at("/data/eventTypeCode").asText());

        JsonNode pageResponse = postJson("/app/event/page", mapOf(
                "keyword", marker,
                "eventTypeCode", "IT_EVENT_" + marker,
                "eventOwnerType", "CONTACT",
                "ownerContactId", contactId,
                "startDate", "2099-10-01",
                "endDate", "2099-10-31",
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(pageResponse);
        Assertions.assertTrue(pageResponse.at("/data/total").asLong() >= 1);
        Assertions.assertTrue(containsByField(pageResponse.at("/data/list"), "eventId", eventId));
    }

    @Test
    void recordEndpointsShouldSupportSaveUpdateDetailAndTimelines() throws Exception {
        String marker = marker();
        String eventTypeId = "it-event-type-" + marker;
        String eventTypeCode = "IT_RECORD_" + marker;
        insertEventType(eventTypeId, eventTypeCode, "记录接口类型" + marker, true);
        String contactId = saveContact("记录联系人" + marker, "记录别名" + marker, "阿姨", "13600136000", "RELATIVE", "记录联系人备注");
        String eventId = saveEvent("记录事件" + marker, eventTypeId, "SELF", null, LocalDate.of(2099, 11, 1), "记录事件备注");

        String recordId = saveRecord(contactId, eventId, "RECEIVE", "188.88", LocalDate.of(2099, 11, 2), "记录初始备注" + marker);

        JsonNode updateResponse = postJson("/app/record/update", mapOf(
                "recordId", recordId,
                "contactId", contactId,
                "eventId", eventId,
                "direction", "RECEIVE",
                "amount", "288.66",
                "recordDate", "2099-11-03",
                "remark", "记录更新备注" + marker
        ));
        assertSuccess(updateResponse);
        Assertions.assertEquals(recordId, updateResponse.at("/data/id").asText());

        JsonNode detailResponse = postJson("/app/record/detail", Map.of("recordId", recordId));
        assertSuccess(detailResponse);
        Assertions.assertEquals(recordId, detailResponse.at("/data/recordId").asText());
        assertBigDecimal(detailResponse.at("/data/amount"), "288.66");
        Assertions.assertEquals("RECEIVE", detailResponse.at("/data/direction").asText());
        Assertions.assertEquals("2099-11-03", detailResponse.at("/data/recordDate").asText());

        JsonNode pageResponse = postJson("/app/record/page", mapOf(
                "contactId", contactId,
                "eventId", eventId,
                "direction", "RECEIVE",
                "eventTypeCode", eventTypeCode,
                "startDate", "2099-11-01",
                "endDate", "2099-11-30",
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(pageResponse);
        Assertions.assertEquals(1, pageResponse.at("/data/list").size());
        Assertions.assertEquals(recordId, pageResponse.at("/data/list/0/recordId").asText());

        JsonNode selfTimelineResponse = postJson("/app/record/self-timeline", mapOf(
                "startDate", "2099-11-01",
                "endDate", "2099-11-30",
                "direction", "RECEIVE",
                "eventTypeCode", eventTypeCode,
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(selfTimelineResponse);
        assertBigDecimal(selfTimelineResponse.at("/data/summaryInfo/receiveTotalAmount"), "288.66");
        Assertions.assertEquals(1, selfTimelineResponse.at("/data/pageResult/list").size());

        JsonNode contactTimelineResponse = postJson("/app/record/contact-timeline", mapOf(
                "contactId", contactId,
                "startDate", "2099-11-01",
                "endDate", "2099-11-30",
                "direction", "RECEIVE",
                "eventTypeCode", eventTypeCode,
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(contactTimelineResponse);
        assertBigDecimal(contactTimelineResponse.at("/data/summaryInfo/receiveTotalAmount"), "288.66");
        Assertions.assertEquals(recordId, contactTimelineResponse.at("/data/pageResult/list/0/recordId").asText());
    }

    @Test
    void reciprocityEndpointsShouldSupportAutoMatchCancelConfirmAndHistory() throws Exception {
        String marker = marker();
        String eventTypeId = "it-event-type-" + marker;
        insertEventType(eventTypeId, "IT_RECIPROCITY_" + marker, "闭环接口类型" + marker, true);
        String contactId = saveContact("闭环联系人" + marker, "闭环别名" + marker, "同学", "13500135000", "CLASSMATE", "闭环联系人备注");
        String selfEventId = saveEvent("本人事件" + marker, eventTypeId, "SELF", null, LocalDate.of(2099, 12, 1), "本人事件备注");
        String contactEventId = saveEvent("联系人事件" + marker, eventTypeId, "CONTACT", contactId, LocalDate.of(2099, 12, 2), "联系人事件备注");

        String receiveRecordId = saveRecord(contactId, selfEventId, "RECEIVE", "500.00", LocalDate.of(2099, 12, 3), "收到礼金" + marker);
        String sendRecordId = saveRecord(contactId, contactEventId, "SEND", "520.00", LocalDate.of(2099, 12, 4), "送出礼金" + marker);

        JsonNode matchedDetailResponse = postJson("/app/reciprocity/detail", Map.of("recordId", sendRecordId));
        assertSuccess(matchedDetailResponse);
        Assertions.assertEquals(receiveRecordId, matchedDetailResponse.at("/data/matchedRecordInfo/recordId").asText());
        Assertions.assertEquals("AUTO", matchedDetailResponse.at("/data/manualFlagInfo/matchType").asText());
        assertBigDecimal(matchedDetailResponse.at("/data/historyReference/sameTypeReceiveAmount"), "500.00");
        assertBigDecimal(matchedDetailResponse.at("/data/historyReference/sameTypeSendAmount"), "520.00");
        String matchId = matchedDetailResponse.at("/data/manualFlagInfo/reciprocityMatchId").asText();
        Assertions.assertFalse(matchId.isEmpty());

        JsonNode matchedPageResponse = postJson("/app/reciprocity/page", mapOf(
                "contactId", contactId,
                "eventTypeCode", "IT_RECIPROCITY_" + marker,
                "reciprocityStatus", "MATCHED",
                "startDate", "2099-12-01",
                "endDate", "2099-12-31",
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(matchedPageResponse);
        Assertions.assertTrue(matchedPageResponse.at("/data/total").asLong() >= 2);

        JsonNode cancelResponse = postJson("/app/reciprocity/manual-cancel", mapOf(
                "reciprocityMatchId", matchId,
                "cancelReason", "接口测试取消" + marker
        ));
        assertSuccess(cancelResponse);
        Assertions.assertEquals(matchId, cancelResponse.at("/data/id").asText());

        JsonNode canceledPageResponse = postJson("/app/reciprocity/page", mapOf(
                "contactId", contactId,
                "eventTypeCode", "IT_RECIPROCITY_" + marker,
                "reciprocityStatus", "MANUAL_CANCELED",
                "startDate", "2099-12-01",
                "endDate", "2099-12-31",
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(canceledPageResponse);
        Assertions.assertTrue(canceledPageResponse.at("/data/total").asLong() >= 2);

        JsonNode confirmResponse = postJson("/app/reciprocity/manual-confirm", mapOf(
                "sourceRecordId", receiveRecordId,
                "targetRecordId", sendRecordId,
                "remark", "接口测试手工确认" + marker
        ));
        assertSuccess(confirmResponse);
        String manualMatchId = confirmResponse.at("/data/id").asText();
        Assertions.assertFalse(manualMatchId.isEmpty());

        JsonNode manualDetailResponse = postJson("/app/reciprocity/detail", Map.of("recordId", receiveRecordId));
        assertSuccess(manualDetailResponse);
        Assertions.assertEquals(sendRecordId, manualDetailResponse.at("/data/matchedRecordInfo/recordId").asText());
        Assertions.assertEquals("MANUAL", manualDetailResponse.at("/data/manualFlagInfo/matchType").asText());
        Assertions.assertEquals("接口测试手工确认" + marker, manualDetailResponse.at("/data/manualFlagInfo/remark").asText());

        JsonNode historyReferenceResponse = postJson("/app/reciprocity/history-reference", mapOf(
                "contactId", contactId,
                "eventTypeId", eventTypeId
        ));
        assertSuccess(historyReferenceResponse);
        assertBigDecimal(historyReferenceResponse.at("/data/sameTypeReceiveAmount"), "500.00");
        assertBigDecimal(historyReferenceResponse.at("/data/sameTypeSendAmount"), "520.00");
        Assertions.assertEquals(sendRecordId, historyReferenceResponse.at("/data/lastSameTypeRecord/recordId").asText());

        JsonNode manualPageResponse = postJson("/app/reciprocity/page", mapOf(
                "contactId", contactId,
                "eventTypeCode", "IT_RECIPROCITY_" + marker,
                "reciprocityStatus", "MANUAL_CONFIRMED",
                "startDate", "2099-12-01",
                "endDate", "2099-12-31",
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(manualPageResponse);
        Assertions.assertTrue(manualPageResponse.at("/data/total").asLong() >= 2);
    }

    @Test
    void statsAndHomeEndpointsShouldReturnExpectedAggregates() throws Exception {
        String marker = marker();
        String eventTypeId = "it-event-type-" + marker;
        String eventTypeCode = "IT_STATS_" + marker;
        insertEventType(eventTypeId, eventTypeCode, "统计接口类型" + marker, true);
        Long pendingBefore = jdbcTemplate.queryForObject(
                "select count(1) from rl_gift_record where reciprocity_status = 'UNMATCHED'",
                Long.class
        );
        String contactId = saveContact("统计联系人" + marker, "统计别名" + marker, "朋友", "13400134000", "FRIEND", "统计联系人备注");
        String selfEventId = saveEvent("统计本人事件" + marker, eventTypeId, "SELF", null, LocalDate.of(2099, 12, 28), "统计本人事件备注");
        String contactEventId = saveEvent("统计联系人事件" + marker, eventTypeId, "CONTACT", contactId, LocalDate.of(2099, 12, 29), "统计联系人事件备注");
        String unmatchedEventId = saveEvent("统计未闭环事件" + marker, eventTypeId, "SELF", null, LocalDate.of(2099, 12, 30), "统计未闭环事件备注");

        saveRecord(contactId, selfEventId, "RECEIVE", "300.00", LocalDate.of(2099, 12, 29), "统计收礼" + marker);
        saveRecord(contactId, contactEventId, "SEND", "120.00", LocalDate.of(2099, 12, 30), "统计送礼" + marker);
        String latestRecordId = saveRecord(contactId, unmatchedEventId, "RECEIVE", "80.00", LocalDate.of(2099, 12, 31), "统计未闭环记录" + marker);

        JsonNode overviewResponse = postJson("/app/stats/overview", mapOf(
                "startDate", "2099-12-28",
                "endDate", "2099-12-31"
        ));
        assertSuccess(overviewResponse);
        assertBigDecimal(overviewResponse.at("/data/receiveTotalAmount"), "380.00");
        assertBigDecimal(overviewResponse.at("/data/sendTotalAmount"), "120.00");
        assertBigDecimal(overviewResponse.at("/data/netAmount"), "260.00");
        Assertions.assertEquals(2L, overviewResponse.at("/data/receiveCount").asLong());
        Assertions.assertEquals(1L, overviewResponse.at("/data/sendCount").asLong());

        JsonNode byContactResponse = postJson("/app/stats/by-contact", mapOf(
                "startDate", "2099-12-28",
                "endDate", "2099-12-31",
                "relationType", "FRIEND",
                "keyword", marker,
                "pageNo", 1,
                "pageSize", 10
        ));
        assertSuccess(byContactResponse);
        Assertions.assertEquals(1L, byContactResponse.at("/data/total").asLong());
        Assertions.assertEquals(contactId, byContactResponse.at("/data/list/0/contactId").asText());
        assertBigDecimal(byContactResponse.at("/data/list/0/netAmount"), "260.00");

        JsonNode byEventTypeResponse = postJson("/app/stats/by-event-type", mapOf(
                "startDate", "2099-12-28",
                "endDate", "2099-12-31"
        ));
        assertSuccess(byEventTypeResponse);
        JsonNode eventTypeItem = findFirstByField(byEventTypeResponse.at("/data/list"), "eventTypeCode", eventTypeCode);
        Assertions.assertNotNull(eventTypeItem);
        assertBigDecimal(eventTypeItem.get("receiveTotalAmount"), "380.00");
        assertBigDecimal(eventTypeItem.get("sendTotalAmount"), "120.00");

        JsonNode homeResponse = postJson("/app/home/overview", mapOf(
                "startDate", "2099-12-28",
                "endDate", "2099-12-31"
        ));
        assertSuccess(homeResponse);
        assertBigDecimal(homeResponse.at("/data/receiveTotalAmount"), "380.00");
        assertBigDecimal(homeResponse.at("/data/sendTotalAmount"), "120.00");
        Assertions.assertEquals((pendingBefore == null ? 0L : pendingBefore) + 1L, homeResponse.at("/data/pendingReciprocityCount").asLong());
        Assertions.assertEquals(latestRecordId, homeResponse.at("/data/recentRecordList/0/recordId").asText());
        Assertions.assertEquals(unmatchedEventId, homeResponse.at("/data/recentEventList/0/eventId").asText());
    }

    private String saveContact(String contactName, String aliasName, String salutation, String mobile, String relationType, String remark) throws Exception {
        JsonNode response = postJson("/app/contact/save", mapOf(
                "contactName", contactName,
                "aliasName", aliasName,
                "salutation", salutation,
                "mobile", mobile,
                "relationType", relationType,
                "remark", remark
        ));
        assertSuccess(response);
        return response.at("/data/id").asText();
    }

    private String saveEvent(String eventName, String eventTypeId, String eventOwnerType, String ownerContactId, LocalDate eventDate, String remark) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("eventName", eventName);
        request.put("eventTypeId", eventTypeId);
        request.put("eventOwnerType", eventOwnerType);
        request.put("ownerContactId", ownerContactId);
        request.put("eventDate", eventDate.toString());
        request.put("remark", remark);
        JsonNode response = postJson("/app/event/save", request);
        assertSuccess(response);
        return response.at("/data/id").asText();
    }

    private String saveRecord(String contactId, String eventId, String direction, String amount, LocalDate recordDate, String remark) throws Exception {
        JsonNode response = postJson("/app/record/save", mapOf(
                "contactId", contactId,
                "eventId", eventId,
                "direction", direction,
                "amount", amount,
                "recordDate", recordDate.toString(),
                "remark", remark
        ));
        assertSuccess(response);
        return response.at("/data/id").asText();
    }

    private JsonNode postJson(String url, Object body) throws Exception {
        MvcResult result = mockMvc.perform(
                        post(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private void insertEventType(String id, String typeCode, String typeName, boolean enabledFlag) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(
                "insert into rl_event_type(id, type_code, type_name, sort_no, enabled_flag, built_in_flag, remark, create_time, update_time) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                typeCode,
                typeName,
                999,
                enabledFlag,
                false,
                "接口测试事件类型",
                now,
                now
        );
    }

    private void assertSuccess(JsonNode response) {
        Assertions.assertEquals(0, response.path("code").asInt(), response.toPrettyString());
        Assertions.assertEquals("success", response.path("message").asText(), response.toPrettyString());
    }

    private void assertBigDecimal(JsonNode node, String expectedValue) {
        Assertions.assertEquals(0, node.decimalValue().compareTo(new BigDecimal(expectedValue)));
    }

    private boolean containsByField(JsonNode arrayNode, String fieldName, String expectedValue) {
        return findFirstByField(arrayNode, fieldName, expectedValue) != null;
    }

    private JsonNode findFirstByField(JsonNode arrayNode, String fieldName, String expectedValue) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }
        for (JsonNode item : arrayNode) {
            if (expectedValue.equals(item.path(fieldName).asText())) {
                return item;
            }
        }
        return null;
    }

    private Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return result;
    }

    private String marker() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
