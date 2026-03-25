package com.reciprocityledger.backend.contact.service;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.common.util.PageUtils;
import com.reciprocityledger.backend.contact.dto.request.ContactDetailRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactPageRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactSaveRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactUpdateRequest;
import com.reciprocityledger.backend.contact.dto.response.ContactDetailResponse;
import com.reciprocityledger.backend.contact.dto.response.ContactPageItemResponse;
import com.reciprocityledger.backend.contact.entity.Contact;
import com.reciprocityledger.backend.contact.mapper.ContactMapper;
import com.reciprocityledger.backend.record.mapper.RecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMapper contactMapper;
    private final RecordMapper recordMapper;
    private final IdGenerator idGenerator;

    public PageResponse<ContactPageItemResponse> page(ContactPageRequest request) {
        int pageNo = PageUtils.safePageNo(request.getPageNo());
        int pageSize = PageUtils.safePageSize(request.getPageSize());
        String keyword = normalizeNullable(request.getKeyword());
        String relationType = normalizeNullable(request.getRelationType());
        long total = contactMapper.countPage(keyword, relationType);
        List<ContactPageItemResponse> list = contactMapper.selectPage(keyword, relationType, PageUtils.offset(pageNo, pageSize), pageSize);
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    public ContactDetailResponse detail(ContactDetailRequest request) {
        Contact contact = requireContact(request.getContactId());
        return buildDetail(contact);
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse save(ContactSaveRequest request) {
        Contact contact = new Contact();
        contact.setId(idGenerator.nextId());
        contact.setContactName(normalizeRequired(request.getContactName(), "contactName不能为空", 64));
        contact.setAliasName(normalizeNullableLength(request.getAliasName(), 64));
        contact.setSalutation(normalizeNullableLength(request.getSalutation(), 64));
        contact.setMobile(normalizeMobile(request.getMobile()));
        contact.setRelationType(normalizeNullableLength(request.getRelationType(), 32));
        contact.setRemark(normalizeNullableLength(request.getRemark(), 500));
        contact.setStatus("NORMAL");
        contactMapper.insert(contact);
        return new IdResponse(contact.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse update(ContactUpdateRequest request) {
        Contact contact = requireContact(request.getContactId());
        contact.setContactName(normalizeRequired(request.getContactName(), "contactName不能为空", 64));
        contact.setAliasName(normalizeNullableLength(request.getAliasName(), 64));
        contact.setSalutation(normalizeNullableLength(request.getSalutation(), 64));
        contact.setMobile(normalizeMobile(request.getMobile()));
        contact.setRelationType(normalizeNullableLength(request.getRelationType(), 32));
        contact.setRemark(normalizeNullableLength(request.getRemark(), 500));
        contactMapper.update(contact);
        return new IdResponse(contact.getId());
    }

    public Contact requireContact(String contactId) {
        Contact contact = contactMapper.selectById(contactId);
        if (contact == null) {
            throw new BusinessException(ErrorCode.CONTACT_NOT_FOUND, "联系人不存在");
        }
        return contact;
    }

    private ContactDetailResponse buildDetail(Contact contact) {
        ContactDetailResponse response = new ContactDetailResponse();
        response.setContactId(contact.getId());
        response.setContactName(contact.getContactName());
        response.setAliasName(contact.getAliasName());
        response.setSalutation(contact.getSalutation());
        response.setMobile(contact.getMobile());
        response.setRelationType(contact.getRelationType());
        response.setRemark(contact.getRemark());
        BigDecimal receiveTotalAmount = recordMapper.sumAmountByContactAndDirection(contact.getId(), "RECEIVE");
        BigDecimal sendTotalAmount = recordMapper.sumAmountByContactAndDirection(contact.getId(), "SEND");
        LocalDate lastRecordDate = recordMapper.selectLastRecordDateByContactId(contact.getId());
        Long unclosedCount = recordMapper.countByContactAndReciprocityStatus(contact.getId(), "UNMATCHED");
        receiveTotalAmount = receiveTotalAmount == null ? BigDecimal.ZERO : receiveTotalAmount;
        sendTotalAmount = sendTotalAmount == null ? BigDecimal.ZERO : sendTotalAmount;
        response.setReceiveTotalAmount(receiveTotalAmount);
        response.setSendTotalAmount(sendTotalAmount);
        response.setNetAmount(receiveTotalAmount.subtract(sendTotalAmount));
        response.setLastRecordDate(lastRecordDate);
        response.setUnclosedReciprocityCount(unclosedCount == null ? 0L : unclosedCount);
        return response;
    }

    private String normalizeRequired(String value, String message, int maxLength) {
        String normalized = StrUtil.trim(value);
        if (StrUtil.isBlank(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, message);
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "字段长度超出限制");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = StrUtil.trim(value);
        return StrUtil.isBlank(normalized) ? null : normalized;
    }

    private String normalizeNullableLength(String value, int maxLength) {
        String normalized = normalizeNullable(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "字段长度超出限制");
        }
        return normalized;
    }

    private String normalizeMobile(String mobile) {
        String normalized = normalizeNullableLength(mobile, 20);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        if (!PhoneUtil.isMobile(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "手机号格式不正确");
        }
        return normalized;
    }
}
