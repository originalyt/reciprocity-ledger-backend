package com.reciprocityledger.backend.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 真实场景数据生成测试。
 * 这组测试不回滚，目的是通过接口向数据库落一批可用于联调和手工验收的业务数据。
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RealScenarioDataGenerationTests {

    private static final String EVENT_TYPE_WEDDING = "1001";
    private static final String EVENT_TYPE_BIRTHDAY = "1006";
    private static final String EVENT_TYPE_FESTIVAL = "1009";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String batchNo = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

    private String zhangSanId;
    private String liSiId;
    private String wangWuId;
    private String zhangSanWeddingEventId;
    private String myWeddingEventId;
    private String liSiBirthdayEventId;
    private String myFestivalEventId;
    private String wangWuFestivalEventId;
    private String zhangSanReceiveRecordId;
    private String myWeddingSendRecordId;
    private String liSiBirthdaySendRecordId;
    private String wangWuFestivalReceiveRecordId;
    private String wangWuFestivalSendRecordId;
    private String wangWuManualMatchId;

    @Test
    @Order(1)
    void generateScenarioData() throws Exception {
        zhangSanId = saveContact(
                "张三-场景-" + batchNo,
                "老张-" + batchNo,
                "张哥",
                "13800010001",
                "FRIEND",
                "真实场景测试批次-" + batchNo + "，用于婚礼往来链路"
        );
        liSiId = saveContact(
                "李四-场景-" + batchNo,
                "小李-" + batchNo,
                "李姐",
                "13800010002",
                "COLLEAGUE",
                "真实场景测试批次-" + batchNo + "，用于生日送礼链路"
        );
        wangWuId = saveContact(
                "王五-场景-" + batchNo,
                "老王-" + batchNo,
                "王叔",
                "13800010003",
                "RELATIVE",
                "真实场景测试批次-" + batchNo + "，用于节日闭环链路"
        );

        zhangSanWeddingEventId = saveEvent(
                "张三结婚-" + batchNo,
                EVENT_TYPE_WEDDING,
                "CONTACT",
                zhangSanId,
                LocalDate.of(2025, 5, 1),
                "张三本人婚礼事件"
        );
        myWeddingEventId = saveEvent(
                "我家婚礼回礼-" + batchNo,
                EVENT_TYPE_WEDDING,
                "SELF",
                null,
                LocalDate.of(2026, 10, 1),
                "用于模拟婚礼回礼闭环"
        );
        liSiBirthdayEventId = saveEvent(
                "李四生日宴-" + batchNo,
                EVENT_TYPE_BIRTHDAY,
                "CONTACT",
                liSiId,
                LocalDate.of(2026, 6, 18),
                "用于模拟联系人生日送礼"
        );
        myFestivalEventId = saveEvent(
                "中秋家宴-" + batchNo,
                EVENT_TYPE_FESTIVAL,
                "SELF",
                null,
                LocalDate.of(2026, 9, 20),
                "用于模拟节日往来自主闭环"
        );
        wangWuFestivalEventId = saveEvent(
                "王五中秋回礼-" + batchNo,
                EVENT_TYPE_FESTIVAL,
                "CONTACT",
                wangWuId,
                LocalDate.of(2026, 9, 21),
                "用于模拟节日手工闭环"
        );

        zhangSanReceiveRecordId = saveRecord(
                zhangSanId,
                zhangSanWeddingEventId,
                "RECEIVE",
                "500.00",
                LocalDate.of(2025, 5, 1),
                "张三婚礼随礼收入"
        );
        myWeddingSendRecordId = saveRecord(
                zhangSanId,
                myWeddingEventId,
                "SEND",
                "800.00",
                LocalDate.of(2026, 10, 1),
                "张三参加我家婚礼回礼"
        );
        liSiBirthdaySendRecordId = saveRecord(
                liSiId,
                liSiBirthdayEventId,
                "SEND",
                "300.00",
                LocalDate.of(2026, 6, 18),
                "李四生日送礼"
        );
        wangWuFestivalReceiveRecordId = saveRecord(
                wangWuId,
                myFestivalEventId,
                "RECEIVE",
                "200.00",
                LocalDate.of(2026, 9, 20),
                "王五节日来往收礼"
        );
        wangWuFestivalSendRecordId = saveRecord(
                wangWuId,
                wangWuFestivalEventId,
                "SEND",
                "260.00",
                LocalDate.of(2026, 9, 21),
                "王五节日来往回礼"
        );

        JsonNode autoMatchDetail = postJson("/app/reciprocity/detail", Map.of("recordId", myWeddingSendRecordId));
        assertSuccess(autoMatchDetail);
        Assertions.assertEquals(zhangSanReceiveRecordId, autoMatchDetail.at("/data/matchedRecordInfo/recordId").asText());
        Assertions.assertEquals("AUTO", autoMatchDetail.at("/data/manualFlagInfo/matchType").asText());

        JsonNode wangWuAutoMatchDetail = postJson("/app/reciprocity/detail", Map.of("recordId", wangWuFestivalSendRecordId));
        assertSuccess(wangWuAutoMatchDetail);
        String wangWuAutoMatchId = wangWuAutoMatchDetail.at("/data/manualFlagInfo/reciprocityMatchId").asText();
        Assertions.assertFalse(wangWuAutoMatchId.isEmpty());
        Assertions.assertEquals("AUTO", wangWuAutoMatchDetail.at("/data/manualFlagInfo/matchType").asText());

        JsonNode manualCancelResponse = postJson("/app/reciprocity/manual-cancel", mapOf(
                "reciprocityMatchId", wangWuAutoMatchId,
                "cancelReason", "真实场景测试批次-" + batchNo + "，转为手工确认"
        ));
        assertSuccess(manualCancelResponse);

        JsonNode manualConfirmResponse = postJson("/app/reciprocity/manual-confirm", mapOf(
                "sourceRecordId", wangWuFestivalReceiveRecordId,
                "targetRecordId", wangWuFestivalSendRecordId,
                "remark", "真实场景测试批次-" + batchNo + "，手工确认节日往来"
        ));
        assertSuccess(manualConfirmResponse);
        wangWuManualMatchId = manualConfirmResponse.at("/data/id").asText();
        Assertions.assertFalse(wangWuManualMatchId.isEmpty());

        System.out.println("[scenario-batch] " + batchNo);
        System.out.println("[scenario-contact] zhangSanId=" + zhangSanId + ", liSiId=" + liSiId + ", wangWuId=" + wangWuId);
        System.out.println("[scenario-event] zhangSanWeddingEventId=" + zhangSanWeddingEventId + ", myWeddingEventId=" + myWeddingEventId
                + ", liSiBirthdayEventId=" + liSiBirthdayEventId + ", myFestivalEventId=" + myFestivalEventId + ", wangWuFestivalEventId=" + wangWuFestivalEventId);
        System.out.println("[scenario-record] zhangSanReceiveRecordId=" + zhangSanReceiveRecordId + ", myWeddingSendRecordId=" + myWeddingSendRecordId
                + ", liSiBirthdaySendRecordId=" + liSiBirthdaySendRecordId + ", wangWuFestivalReceiveRecordId=" + wangWuFestivalReceiveRecordId
                + ", wangWuFestivalSendRecordId=" + wangWuFestivalSendRecordId);
        System.out.println("[scenario-match] wangWuManualMatchId=" + wangWuManualMatchId);
    }

    @Test
    @Order(2)
    void verifyScenarioQueries() throws Exception {
        JsonNode contactPageResponse = postJson("/app/contact/page", mapOf(
                "keyword", batchNo,
                "pageNo", 1,
                "pageSize", 20
        ));
        assertSuccess(contactPageResponse);
        Assertions.assertTrue(contactPageResponse.at("/data/total").asLong() >= 3);

        JsonNode zhangSanDetailResponse = postJson("/app/contact/detail", Map.of("contactId", zhangSanId));
        assertSuccess(zhangSanDetailResponse);
        assertBigDecimal(zhangSanDetailResponse.at("/data/receiveTotalAmount"), "500.00");
        assertBigDecimal(zhangSanDetailResponse.at("/data/sendTotalAmount"), "800.00");
        Assertions.assertEquals(0L, zhangSanDetailResponse.at("/data/unclosedReciprocityCount").asLong());

        JsonNode recordPageResponse = postJson("/app/record/page", mapOf(
                "contactId", zhangSanId,
                "pageNo", 1,
                "pageSize", 20
        ));
        assertSuccess(recordPageResponse);
        Assertions.assertTrue(recordPageResponse.at("/data/total").asLong() >= 2);

        JsonNode reciprocityPageResponse = postJson("/app/reciprocity/page", mapOf(
                "contactId", wangWuId,
                "reciprocityStatus", "MANUAL_CONFIRMED",
                "pageNo", 1,
                "pageSize", 20
        ));
        assertSuccess(reciprocityPageResponse);
        Assertions.assertTrue(reciprocityPageResponse.at("/data/total").asLong() >= 2);

        JsonNode statsOverviewResponse = postJson("/app/stats/overview", mapOf(
                "startDate", "2025-01-01",
                "endDate", "2026-12-31"
        ));
        assertSuccess(statsOverviewResponse);
        Assertions.assertTrue(statsOverviewResponse.at("/data/receiveCount").asLong() >= 2);
        Assertions.assertTrue(statsOverviewResponse.at("/data/sendCount").asLong() >= 3);

        JsonNode byContactResponse = postJson("/app/stats/by-contact", mapOf(
                "keyword", batchNo,
                "pageNo", 1,
                "pageSize", 20
        ));
        assertSuccess(byContactResponse);
        Assertions.assertTrue(byContactResponse.at("/data/total").asLong() >= 3);

        JsonNode homeOverviewResponse = postJson("/app/home/overview", mapOf(
                "startDate", "2025-01-01",
                "endDate", "2026-12-31"
        ));
        assertSuccess(homeOverviewResponse);
        Assertions.assertTrue(homeOverviewResponse.at("/data/recentRecordList").size() >= 1);
        Assertions.assertTrue(homeOverviewResponse.at("/data/recentEventList").size() >= 1);
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
        JsonNode response = postJson("/app/event/save", mapOf(
                "eventName", eventName,
                "eventTypeId", eventTypeId,
                "eventOwnerType", eventOwnerType,
                "ownerContactId", ownerContactId,
                "eventDate", eventDate.toString(),
                "remark", remark
        ));
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

    private void assertSuccess(JsonNode response) {
        Assertions.assertEquals(0, response.path("code").asInt(), response.toPrettyString());
        Assertions.assertEquals("success", response.path("message").asText(), response.toPrettyString());
    }

    private void assertBigDecimal(JsonNode node, String expectedValue) {
        Assertions.assertEquals(0, node.decimalValue().compareTo(new BigDecimal(expectedValue)));
    }

    private Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return result;
    }
}
