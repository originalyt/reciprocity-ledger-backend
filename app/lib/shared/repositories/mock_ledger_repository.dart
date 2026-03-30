import '../models/ledger_models.dart';
import 'ledger_repository.dart';

class MockLedgerRepository implements LedgerRepository {
  static final _contacts = <ContactOption>[
    const ContactOption(id: 'c1', name: '张三', relation: 'RELATIVE'),
    const ContactOption(id: 'c2', name: '李阿姨', relation: 'NEIGHBOR'),
    const ContactOption(id: 'c3', name: '王老师', relation: 'OTHER'),
  ];

  static const _relationTypes = <RelationTypeOption>[
    RelationTypeOption(id: 'RELATIVE', code: 'RELATIVE', name: '亲戚'),
    RelationTypeOption(id: 'FRIEND', code: 'FRIEND', name: '朋友'),
    RelationTypeOption(id: 'COLLEAGUE', code: 'COLLEAGUE', name: '同事'),
    RelationTypeOption(id: 'CLASSMATE', code: 'CLASSMATE', name: '同学'),
    RelationTypeOption(id: 'NEIGHBOR', code: 'NEIGHBOR', name: '邻居'),
    RelationTypeOption(id: 'OTHER', code: 'OTHER', name: '其他'),
  ];

  static const _eventTypes = <EventTypeOption>[
    EventTypeOption(id: '1001', code: 'WEDDING', name: '结婚'),
    EventTypeOption(id: '1005', code: 'HOUSEWARMING', name: '乔迁'),
    EventTypeOption(id: '1006', code: 'BIRTHDAY', name: '生日'),
    EventTypeOption(id: '1007', code: 'FUNERAL', name: '白事'),
    EventTypeOption(id: '1010', code: 'OTHER', name: '其他'),
  ];

  static final _recentRecords = <RecentRecord>[
    RecentRecord(
      id: 'r1',
      contactId: 'c1',
      contactName: '张三',
      eventId: 'e1',
      eventName: '表弟结婚',
      eventTypeCode: 'WEDDING',
      eventTypeName: '结婚',
      kind: RecordKind.give,
      amount: 800,
      recordDate: DateTime(2026, 3, 18),
      reciprocityStatus: ReciprocityStatus.unmatched,
    ),
    RecentRecord(
      id: 'r2',
      contactId: 'c2',
      contactName: '李阿姨',
      eventId: 'e2',
      eventName: '我家乔迁',
      eventTypeCode: 'HOUSEWARMING',
      eventTypeName: '乔迁',
      kind: RecordKind.receive,
      amount: 500,
      recordDate: DateTime(2026, 3, 16),
      reciprocityStatus: ReciprocityStatus.matched,
    ),
  ];

  static final _timeline = <TimelineEntry>[
    TimelineEntry(
      id: 'r1',
      contactId: 'c1',
      contactName: '张三',
      eventId: 'e1',
      eventName: '表弟结婚',
      eventTypeId: '1001',
      eventTypeCode: 'WEDDING',
      eventTypeName: '结婚',
      kind: RecordKind.give,
      amount: 800,
      recordDate: DateTime(2026, 3, 18),
      reciprocityStatus: ReciprocityStatus.unmatched,
      remark: '现场随礼',
      relation: 'RELATIVE',
    ),
    TimelineEntry(
      id: 'r2',
      contactId: 'c2',
      contactName: '李阿姨',
      eventId: 'e2',
      eventName: '我家乔迁',
      eventTypeId: '1005',
      eventTypeCode: 'HOUSEWARMING',
      eventTypeName: '乔迁',
      kind: RecordKind.receive,
      amount: 500,
      recordDate: DateTime(2026, 3, 16),
      reciprocityStatus: ReciprocityStatus.matched,
      remark: '邻里往来',
      relation: 'NEIGHBOR',
    ),
  ];

  @override
  Future<HomeOverview> fetchHomeOverview() async {
    return HomeOverview(
      totalGive: 12800,
      totalReceive: 17600,
      pendingReciprocityCount: 3,
      recentRecords: _recentRecords,
    );
  }

  @override
  Future<List<ContactOption>> fetchContacts({String? keyword, int pageNo = 1, int pageSize = 50}) async {
    if (keyword == null || keyword.trim().isEmpty) {
      return _contacts;
    }
    final normalized = keyword.trim();
    return _contacts.where((item) => item.name.contains(normalized)).toList();
  }

  @override
  Future<ContactDetail> fetchContactDetail(String contactId) async {
    final entries = _timeline.where((item) => item.contactId == contactId).toList();
    final contact = _contacts.firstWhere((item) => item.id == contactId, orElse: () => _contacts.first);
    return ContactDetail(
      id: contact.id,
      name: contact.name,
      mobile: '138****1024',
      relation: contact.relation,
      remark: 'Mock 数据联系人',
      totalGive: 800,
      totalReceive: 300,
      netAmount: -500,
      lastRecordDate: entries.isEmpty ? null : entries.first.recordDate,
      unclosedReciprocityCount: 1,
      timeline: entries,
    );
  }

  @override
  Future<TimelineBundle> fetchSelfTimeline({RecordKind? kind, int pageNo = 1, int pageSize = 50}) async {
    final entries = kind == null ? _timeline : _timeline.where((item) => item.kind == kind).toList();
    double give = 0;
    double receive = 0;
    for (final item in entries) {
      if (item.kind == RecordKind.give) {
        give += item.amount;
      } else {
        receive += item.amount;
      }
    }
    return TimelineBundle(
      summary: TimelineSummary(
        totalGive: give,
        totalReceive: receive,
        netAmount: receive - give,
        recordCount: entries.length,
      ),
      entries: entries,
    );
  }

  @override
  Future<List<ReciprocityEventSummary>> fetchReciprocityList(ReciprocityStatus status, {int pageNo = 1, int pageSize = 50}) async {
    return [
      ReciprocityEventSummary(
        recordId: 'r1',
        contactId: 'c1',
        contactName: '张三',
        eventId: 'e1',
        eventName: '表弟结婚',
        eventTypeCode: 'WEDDING',
        eventTypeName: '结婚',
        kind: RecordKind.give,
        amount: 800,
        recordDate: DateTime(2026, 3, 18),
        status: status,
      ),
    ];
  }

  @override
  Future<ReciprocityDetail> fetchReciprocityDetail(String recordId) async {
    return ReciprocityDetail(
      record: RecordInfo(
        recordId: 'r1',
        contactId: 'c1',
        contactName: '张三',
        eventId: 'e1',
        eventName: '表弟结婚',
        eventTypeId: '1001',
        eventTypeCode: 'WEDDING',
        eventTypeName: '结婚',
        kind: RecordKind.give,
        amount: 800,
        recordDate: DateTime(2026, 3, 18),
        remark: '现场随礼',
        reciprocityStatus: ReciprocityStatus.unmatched,
      ),
      matchedRecord: null,
      historyReference: ReciprocityHistoryReference(
        sameTypeReceiveAmount: 1000,
        sameTypeSendAmount: 800,
        unclosedRecordCount: 1,
        lastSameTypeRecord: _timeline.first,
      ),
      manualInfo: null,
    );
  }

  @override
  Future<List<EventTypeOption>> fetchEventTypes() async {
    return _eventTypes;
  }

  @override
  Future<List<RelationTypeOption>> fetchRelationTypes({String? keyword}) async {
    if (keyword == null || keyword.trim().isEmpty) {
      return _relationTypes;
    }
    final normalized = keyword.trim();
    return _relationTypes.where((item) => item.name.contains(normalized) || item.code.contains(normalized)).toList();
  }

  @override
  Future<List<EventOption>> fetchEventOptions({required RecordKind kind, String? contactId, int pageNo = 1, int pageSize = 50}) async {
    return [
      EventOption(
        id: 'e1',
        name: kind == RecordKind.give ? '表弟结婚' : '我家乔迁',
        eventTypeId: kind == RecordKind.give ? '1001' : '1005',
        eventTypeCode: kind == RecordKind.give ? 'WEDDING' : 'HOUSEWARMING',
        eventTypeName: kind == RecordKind.give ? '结婚' : '乔迁',
        ownerType: kind.eventOwnerType,
        ownerContactId: kind == RecordKind.give ? contactId : null,
        eventDate: DateTime(2026, 3, 18),
      ),
    ];
  }

  @override
  Future<String> saveRecord(RecordSaveDraft draft) async {
    return 'mock-record-id';
  }

  @override
  Future<String> saveContact(ContactSaveDraft draft) async {
    final id = 'mock-contact-${_contacts.length + 1}';
    _contacts.add(ContactOption(id: id, name: draft.name, relation: draft.relationTypeCode));
    return id;
  }

  @override
  Future<ContactQuickSaveResult> quickSaveContact(ContactSaveDraft draft) async {
    final id = 'mock-contact-${_contacts.length + 1}';
    _contacts.add(ContactOption(id: id, name: draft.name, relation: draft.relationTypeCode));
    return ContactQuickSaveResult(
      contactId: id,
      contactName: draft.name,
      aliasName: draft.aliasName,
      relationType: draft.relationTypeCode,
    );
  }

  @override
  Future<EventsByContact> eventsByContact(String contactId) async {
    final contact = _contacts.firstWhere((item) => item.id == contactId, orElse: () => _contacts.first);
    return EventsByContact(
      contactId: contactId,
      contactName: contact.name,
      selfEventList: [
        EventOption(
          id: 'e2',
          name: '我家乔迁',
          eventTypeId: '1005',
          eventTypeCode: 'HOUSEWARMING',
          eventTypeName: '乔迁',
          ownerType: 'SELF',
          ownerContactId: null,
          eventDate: DateTime(2026, 3, 16),
        ),
      ],
      contactEventList: [
        EventOption(
          id: 'e1',
          name: '张三结婚',
          eventTypeId: '1001',
          eventTypeCode: 'WEDDING',
          eventTypeName: '结婚',
          ownerType: 'CONTACT',
          ownerContactId: contactId,
          eventDate: DateTime(2026, 3, 18),
        ),
      ],
    );
  }

  @override
  Future<String> saveRecordWithEvent(RecordSaveWithEventDraft draft) async {
    return 'mock-record-id';
  }
}
