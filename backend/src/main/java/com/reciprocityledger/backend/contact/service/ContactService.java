package com.reciprocityledger.backend.contact.service;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.auth.AuthSessionService;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.contact.dto.request.ContactDetailRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactQueryRequest;
import com.reciprocityledger.backend.contact.dto.request.CreateContactRequest;
import com.reciprocityledger.backend.contact.dto.request.UpdateContactRequest;
import com.reciprocityledger.backend.contact.dto.response.ContactDetailResponse;
import com.reciprocityledger.backend.contact.dto.response.ContactSummaryResponse;
import com.reciprocityledger.backend.contact.entity.Contact;
import com.reciprocityledger.backend.contact.mapper.ContactMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final AuthSessionService authSessionService;
    private final ContactMapper contactMapper;
    private final IdGenerator idGenerator;

    public PageResponse<ContactSummaryResponse> query(ContactQueryRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        int pageNo = safePageNo(request.getPageNo());
        int pageSize = safePageSize(request.getPageSize());
        String keyword = normalizeNullable(request.getKeyword());
        String relation = normalizeNullable(request.getRelation());
        long total = contactMapper.countPage(userId, keyword, relation);
        List<ContactSummaryResponse> list = contactMapper.selectPage(userId, keyword, relation, (pageNo - 1) * pageSize, pageSize);
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactDetailResponse create(CreateContactRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        Contact contact = new Contact();
        contact.setId(idGenerator.nextId());
        contact.setUserId(userId);
        contact.setName(normalizeRequiredName(request.getName()));
        contact.setRelation(normalizeNullable(request.getRelation()));
        contact.setPhone(normalizePhone(request.getPhone()));
        contact.setNote(normalizeNullable(request.getNote()));
        contact.setCreatedBy(userId);
        contact.setUpdatedBy(userId);
        contactMapper.insert(contact);
        return toDetailResponse(contactMapper.selectById(userId, contact.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactDetailResponse update(UpdateContactRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        Contact existed = requireContact(userId, request.getContactId());
        existed.setName(normalizeRequiredName(request.getName()));
        existed.setRelation(normalizeNullable(request.getRelation()));
        existed.setPhone(normalizePhone(request.getPhone()));
        existed.setNote(normalizeNullable(request.getNote()));
        existed.setUpdatedBy(userId);
        contactMapper.update(existed);
        return toDetailResponse(contactMapper.selectById(userId, existed.getId()));
    }

    public ContactDetailResponse detail(ContactDetailRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        return toDetailResponse(requireContact(userId, request.getContactId()));
    }

    public Contact requireContact(Long userId, Long contactId) {
        Contact contact = contactMapper.selectById(userId, contactId);
        if (contact == null) {
            throw new BusinessException(ErrorCode.CONTACT_NOT_FOUND, "联系人不存在");
        }
        return contact;
    }

    public void refreshLastInteractionOn(Long userId, Long contactId, java.time.LocalDate lastInteractionOn) {
        contactMapper.updateLastInteractionOn(userId, contactId, lastInteractionOn);
    }

    private ContactDetailResponse toDetailResponse(Contact contact) {
        ContactDetailResponse response = new ContactDetailResponse();
        response.setId(contact.getId());
        response.setName(contact.getName());
        response.setRelation(contact.getRelation());
        response.setPhone(contact.getPhone());
        response.setNote(contact.getNote());
        response.setLastInteractionOn(contact.getLastInteractionOn());
        return response;
    }

    private String normalizeRequiredName(String name) {
        String normalizedName = StrUtil.trim(name);
        if (StrUtil.isBlank(normalizedName)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "name不能为空");
        }
        if (normalizedName.length() > 64) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "name长度不能超过64");
        }
        return normalizedName;
    }

    private String normalizePhone(String phone) {
        String normalizedPhone = normalizeNullable(phone);
        if (StrUtil.isBlank(normalizedPhone)) {
            return null;
        }
        if (!PhoneUtil.isMobile(normalizedPhone)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "联系人手机号格式不正确");
        }
        return normalizedPhone;
    }

    private String normalizeNullable(String value) {
        String normalizedValue = StrUtil.trim(value);
        return StrUtil.isBlank(normalizedValue) ? null : normalizedValue;
    }

    private int safePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
