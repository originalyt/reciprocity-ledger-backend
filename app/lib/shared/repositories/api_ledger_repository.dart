import '../../core/network/api_client.dart';
import '../../core/network/api_exception.dart';
import '../models/ledger_models.dart';
import 'ledger_repository.dart';

class ApiLedgerRepository implements LedgerRepository {
  ApiLedgerRepository(this._apiClient);

  final ApiClient _apiClient;

  @override
  Future<HomeOverview> fetchHomeOverview() async {
    return _apiClient.post('/app/home/overview', {}, fromJsonT: (json) {
      final map = _asMap(json);
      return HomeOverview(
        totalGive: _doubleValue(map['sendTotalAmount']),
        totalReceive: _doubleValue(map['receiveTotalAmount']),
        pendingReciprocityCount: _intValue(map['pendingReciprocityCount']),
        recentRecords: _asList(map['recentRecordList']).map((item) => _recentRecordFromJson(_asMap(item))).toList(),
      );
    });
  }

  @override
  Future<List<ContactOption>> fetchContacts({String? keyword, int pageNo = 1, int pageSize = 50}) async {
    return _apiClient.post('/app/contact/page', {
      'keyword': keyword,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, fromJsonT: (json) {
      final page = _asMap(json);
      return _asList(page['list']).map((item) {
        final map = _asMap(item);
        return ContactOption(
          id: _stringValue(map['contactId']),
          name: _stringValue(map['contactName']),
          relation: _stringValue(map['relationType']),
        );
      }).toList();
    });
  }

  @override
  Future<ContactDetail> fetchContactDetail(String contactId) async {
    final detail = await _apiClient.post('/app/contact/detail', {'contactId': contactId}, fromJsonT: (json) => _asMap(json));
    final timeline = await _apiClient.post('/app/record/contact-timeline', {
      'contactId': contactId,
      'pageNo': 1,
      'pageSize': 20,
    }, fromJsonT: (json) => _asMap(json));

    final timelineItems = _asList(_asMap(timeline['pageResult'])['list']).map((item) {
      final map = _asMap(item);
      return _timelineEntryFromRecordPage(map, relation: _stringValue(detail['relationType']));
    }).toList();

    return ContactDetail(
      id: _stringValue(detail['contactId']),
      name: _stringValue(detail['contactName']),
      mobile: _stringValue(detail['mobile']),
      relation: _stringValue(detail['relationType']),
      remark: _stringValue(detail['remark']),
      totalGive: _doubleValue(detail['sendTotalAmount']),
      totalReceive: _doubleValue(detail['receiveTotalAmount']),
      netAmount: _doubleValue(detail['netAmount']),
      lastRecordDate: _nullableDate(detail['lastRecordDate']),
      unclosedReciprocityCount: _intValue(detail['unclosedReciprocityCount']),
      timeline: timelineItems,
    );
  }

  @override
  Future<TimelineBundle> fetchSelfTimeline({RecordKind? kind, int pageNo = 1, int pageSize = 50}) async {
    return _apiClient.post('/app/record/self-timeline', {
      'direction': kind?.backendDirection,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, fromJsonT: (json) {
      final map = _asMap(json);
      final summary = _asMap(map['summaryInfo']);
      final page = _asMap(map['pageResult']);
      return TimelineBundle(
        summary: TimelineSummary(
          totalGive: _doubleValue(summary['sendTotalAmount']),
          totalReceive: _doubleValue(summary['receiveTotalAmount']),
          netAmount: _doubleValue(summary['netAmount']),
          recordCount: _intValue(summary['recordCount']),
        ),
        entries: _asList(page['list']).map((item) => _timelineEntryFromRecordPage(_asMap(item))).toList(),
      );
    });
  }

  @override
  Future<List<ReciprocityEventSummary>> fetchReciprocityList(ReciprocityStatus status, {int pageNo = 1, int pageSize = 50}) async {
    return _apiClient.post('/app/reciprocity/page', {
      'reciprocityStatus': status.backendValue,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, fromJsonT: (json) {
      final page = _asMap(json);
      return _asList(page['list']).map((item) {
        final map = _asMap(item);
        return ReciprocityEventSummary(
          recordId: _stringValue(map['recordId']),
          contactId: _stringValue(map['contactId']),
          contactName: _stringValue(map['contactName']),
          eventId: _stringValue(map['eventId']),
          eventName: _stringValue(map['eventName']),
          eventTypeCode: _stringValue(map['eventTypeCode']),
          eventTypeName: _stringValue(map['eventTypeName']),
          kind: _recordKindFromDirection(_stringValue(map['direction'])),
          amount: _doubleValue(map['amount']),
          recordDate: _dateValue(map['recordDate']),
          status: parseReciprocityStatus(map['reciprocityStatus']?.toString()),
          matchedRecordId: _nullableString(map['matchedRecordId']),
          matchedAmount: _nullableDouble(map['matchedAmount']),
          matchedRecordDate: _nullableDate(map['matchedRecordDate']),
        );
      }).toList();
    });
  }

  @override
  Future<ReciprocityDetail> fetchReciprocityDetail(String recordId) async {
    return _apiClient.post('/app/reciprocity/detail', {'recordId': recordId}, fromJsonT: (json) {
      final map = _asMap(json);
      final history = _asMap(map['historyReference']);
      return ReciprocityDetail(
        record: _recordInfoFromDetail(_asMap(map['recordInfo'])),
        matchedRecord: map['matchedRecordInfo'] == null ? null : _recordInfoFromDetail(_asMap(map['matchedRecordInfo'])),
        historyReference: ReciprocityHistoryReference(
          sameTypeReceiveAmount: _doubleValue(history['sameTypeReceiveAmount']),
          sameTypeSendAmount: _doubleValue(history['sameTypeSendAmount']),
          unclosedRecordCount: _intValue(history['unclosedRecordCount']),
          lastSameTypeRecord: history['lastSameTypeRecord'] == null ? null : _timelineEntryFromRecordPage(_asMap(history['lastSameTypeRecord'])),
        ),
        manualInfo: map['manualFlagInfo'] == null
            ? null
            : ReciprocityManualInfo(
                matchId: _stringValue(_asMap(map['manualFlagInfo'])['reciprocityMatchId']),
                matchType: _stringValue(_asMap(map['manualFlagInfo'])['matchType']),
                matchStatus: _stringValue(_asMap(map['manualFlagInfo'])['matchStatus']),
                cancelReason: _nullableString(_asMap(map['manualFlagInfo'])['cancelReason']),
                remark: _nullableString(_asMap(map['manualFlagInfo'])['remark']),
              ),
      );
    });
  }

  @override
  Future<List<EventTypeOption>> fetchEventTypes() async {
    return _apiClient.post('/app/dict/event-type/list', {'enabledFlag': true}, fromJsonT: (json) {
      final map = _asMap(json);
      return _asList(map['list']).map((item) {
        final raw = _asMap(item);
        return EventTypeOption(
          id: _stringValue(raw['id']),
          code: _stringValue(raw['code']),
          name: _stringValue(raw['name']),
        );
      }).toList();
    });
  }

  @override
  Future<List<RelationTypeOption>> fetchRelationTypes({String? keyword}) async {
    return _apiClient.post('/app/dict/relation-type/list', {'keyword': keyword}, fromJsonT: (json) {
      final map = _asMap(json);
      return _asList(map['list']).map((item) {
        final raw = _asMap(item);
        return RelationTypeOption(
          id: _stringValue(raw['id']),
          code: _stringValue(raw['code']),
          name: _stringValue(raw['name']),
        );
      }).toList();
    });
  }

  @override
  Future<List<EventOption>> fetchEventOptions({required RecordKind kind, String? contactId, int pageNo = 1, int pageSize = 50}) async {
    return _apiClient.post('/app/event/page', {
      'eventOwnerType': kind.eventOwnerType,
      'ownerContactId': kind == RecordKind.give ? contactId : null,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, fromJsonT: (json) {
      final page = _asMap(json);
      return _asList(page['list']).map((item) {
        final map = _asMap(item);
        return EventOption(
          id: _stringValue(map['eventId']),
          name: _stringValue(map['eventName']),
          eventTypeId: _stringValue(map['eventTypeId']),
          eventTypeCode: _stringValue(map['eventTypeCode']),
          eventTypeName: _stringValue(map['eventTypeName']),
          ownerType: _stringValue(map['eventOwnerType']),
          ownerContactId: _nullableString(map['ownerContactId']),
          eventDate: _dateValue(map['eventDate']),
        );
      }).toList();
    });
  }

  @override
  Future<String> saveRecord(RecordSaveDraft draft) async {
    // 事件是可选的，如果已有事件ID则使用，否则尝试创建新事件
    String? eventId = draft.existingEventId;
    if (eventId == null && draft.newEventType != null && draft.newEventName != null && draft.newEventName!.isNotEmpty) {
      eventId = await _createEvent(draft);
    }
    final result = await _apiClient.post('/app/record/save', {
      'contactId': draft.contactId,
      'eventId': eventId,
      'direction': draft.kind.backendDirection,
      'amount': draft.amount.toStringAsFixed(2),
      'recordDate': _dateString(draft.recordDate),
      'remark': draft.recordRemark,
    }, fromJsonT: (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  @override
  Future<String> saveContact(ContactSaveDraft draft) async {
    final result = await _apiClient.post('/app/contact/save', {
      'contactName': draft.name,
      'aliasName': draft.aliasName,
      'salutation': draft.salutation,
      'mobile': draft.mobile,
      'relationType': draft.relationTypeCode,
      'remark': draft.remark,
    }, fromJsonT: (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  @override
  Future<ContactQuickSaveResult> quickSaveContact(ContactSaveDraft draft) async {
    final result = await _apiClient.post('/app/record/quick-save-contact', {
      'contactName': draft.name,
      'aliasName': draft.aliasName,
      'salutation': draft.salutation,
      'mobile': draft.mobile,
      'relationType': draft.relationTypeCode,
      'remark': draft.remark,
    }, fromJsonT: (json) => _asMap(json));
    return ContactQuickSaveResult(
      contactId: _stringValue(result['contactId']),
      contactName: _stringValue(result['contactName']),
      aliasName: _nullableString(result['aliasName']),
      relationType: _nullableString(result['relationType']),
    );
  }

  @override
  Future<EventsByContact> eventsByContact(String contactId) async {
    return _apiClient.post('/app/event/by-contact', {'contactId': contactId}, fromJsonT: (json) {
      final map = _asMap(json);
      final selfEventList = _asList(map['selfEventList']).map((item) => _eventOptionFromJson(_asMap(item))).toList();
      final contactEventList = _asList(map['contactEventList']).map((item) => _eventOptionFromJson(_asMap(item))).toList();
      return EventsByContact(
        contactId: _stringValue(map['contactId']),
        contactName: _stringValue(map['contactName']),
        selfEventList: selfEventList,
        contactEventList: contactEventList,
      );
    });
  }

  @override
  Future<String> saveRecordWithEvent(RecordSaveWithEventDraft draft) async {
    final result = await _apiClient.post('/app/record/save-with-event', {
      'contactId': draft.contactId,
      'eventName': draft.eventName,
      'eventTypeId': draft.eventTypeId,
      'eventOwnerType': draft.eventOwnerType,
      'ownerContactId': draft.ownerContactId,
      'eventDate': _dateString(draft.recordDate),
      'eventRemark': draft.eventRemark ?? '',
      'direction': draft.kind.backendDirection,
      'amount': draft.amount.toStringAsFixed(2),
      'recordDate': _dateString(draft.recordDate),
      'recordRemark': draft.recordRemark,
    }, fromJsonT: (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  @override
  Future<String> saveRecordSimple(RecordSaveSimpleDraft draft) async {
    final result = await _apiClient.post('/app/record/save-simple', {
      'contactId': draft.contactId,
      'direction': draft.kind.backendDirection,
      'amount': draft.amount.toStringAsFixed(2),
      'recordDate': _dateString(draft.recordDate),
      'eventTypeId': draft.eventTypeId,
      'eventName': draft.eventName,
      'remark': draft.recordRemark,
    }, fromJsonT: (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  Future<String> _createEvent(RecordSaveDraft draft) async {
    final eventType = draft.newEventType;
    if (eventType == null) {
      throw const ApiException(code: -1, message: '请选择事件类型');
    }
    final eventName = draft.newEventName?.trim() ?? '';
    if (eventName.isEmpty) {
      throw const ApiException(code: -1, message: '请输入事件名称');
    }
    final result = await _apiClient.post('/app/event/save', {
      'eventName': eventName,
      'eventTypeId': eventType.id,
      'eventOwnerType': draft.kind.eventOwnerType,
      'ownerContactId': draft.kind == RecordKind.give ? draft.contactId : null,
      'eventDate': _dateString(draft.recordDate),
      'remark': '',
    }, fromJsonT: (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  RecordKind _recordKindFromDirection(String direction) {
    switch (direction) {
      case 'SEND':
        return RecordKind.give;
      case 'RECEIVE':
        return RecordKind.receive;
      default:
        return RecordKind.give;
    }
  }

  RecentRecord _recentRecordFromJson(Map<String, dynamic> map) {
    return RecentRecord(
      id: _stringValue(map['recordId']),
      contactId: _stringValue(map['contactId']),
      contactName: _stringValue(map['contactName']),
      eventId: _stringValue(map['eventId']),
      eventName: _stringValue(map['eventName']),
      eventTypeCode: _stringValue(map['eventTypeCode']),
      eventTypeName: _stringValue(map['eventTypeName']),
      kind: _recordKindFromDirection(_stringValue(map['direction'])),
      amount: _doubleValue(map['amount']),
      recordDate: _dateValue(map['recordDate']),
      reciprocityStatus: parseReciprocityStatus(map['reciprocityStatus']?.toString()),
    );
  }

  EventOption _eventOptionFromJson(Map<String, dynamic> map) {
    return EventOption(
      id: _stringValue(map['eventId'] ?? map['id']),
      name: _stringValue(map['eventName']),
      eventTypeId: _stringValue(map['eventTypeId']),
      eventTypeCode: _stringValue(map['eventTypeCode']),
      eventTypeName: _stringValue(map['eventTypeName']),
      ownerType: _stringValue(map['eventOwnerType']),
      ownerContactId: _nullableString(map['ownerContactId']),
      eventDate: _dateValue(map['eventDate']),
    );
  }

  TimelineEntry _timelineEntryFromRecordPage(Map<String, dynamic> map, {String relation = ''}) {
    return TimelineEntry(
      id: _stringValue(map['recordId']),
      contactId: _stringValue(map['contactId']),
      contactName: _stringValue(map['contactName']),
      eventId: _stringValue(map['eventId']),
      eventName: _stringValue(map['eventName']),
      eventTypeId: _stringValue(map['eventTypeId']),
      eventTypeCode: _stringValue(map['eventTypeCode']),
      eventTypeName: _stringValue(map['eventTypeName']),
      kind: _recordKindFromDirection(_stringValue(map['direction'])),
      amount: _doubleValue(map['amount']),
      recordDate: _dateValue(map['recordDate']),
      reciprocityStatus: parseReciprocityStatus(map['reciprocityStatus']?.toString()),
      relation: relation,
    );
  }

  RecordInfo _recordInfoFromDetail(Map<String, dynamic> map) {
    return RecordInfo(
      recordId: _stringValue(map['recordId']),
      contactId: _stringValue(map['contactId']),
      contactName: _stringValue(map['contactName']),
      eventId: _stringValue(map['eventId']),
      eventName: _stringValue(map['eventName']),
      eventTypeId: _stringValue(map['eventTypeId']),
      eventTypeCode: _stringValue(map['eventTypeCode']),
      eventTypeName: _stringValue(map['eventTypeName']),
      kind: _recordKindFromDirection(_stringValue(map['direction'])),
      amount: _doubleValue(map['amount']),
      recordDate: _dateValue(map['recordDate']),
      remark: _stringValue(map['remark']),
      reciprocityStatus: parseReciprocityStatus(map['reciprocityStatus']?.toString()),
    );
  }

  static Map<String, dynamic> _asMap(Object? value) {
    return (value as Map).cast<String, dynamic>();
  }

  static List<Object?> _asList(Object? value) {
    return (value as List?) ?? const [];
  }

  static String _stringValue(Object? value) {
    return value?.toString() ?? '';
  }

  static String? _nullableString(Object? value) {
    final text = value?.toString().trim();
    if (text == null || text.isEmpty) {
      return null;
    }
    return text;
  }

  static double _doubleValue(Object? value) {
    if (value == null) {
      return 0;
    }
    if (value is num) {
      return value.toDouble();
    }
    return double.tryParse(value.toString()) ?? 0;
  }

  static double? _nullableDouble(Object? value) {
    if (value == null) {
      return null;
    }
    return _doubleValue(value);
  }

  static int _intValue(Object? value) {
    if (value == null) {
      return 0;
    }
    if (value is num) {
      return value.toInt();
    }
    return int.tryParse(value.toString()) ?? 0;
  }

  static DateTime _dateValue(Object? value) {
    return DateTime.parse(_stringValue(value));
  }

  static DateTime? _nullableDate(Object? value) {
    final text = _nullableString(value);
    if (text == null) {
      return null;
    }
    return DateTime.parse(text);
  }

  static String _dateString(DateTime value) {
    final year = value.year.toString().padLeft(4, '0');
    final month = value.month.toString().padLeft(2, '0');
    final day = value.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  // 事件关联相关接口
  @override
  Future<String> saveEventRelation(String selfEventId, String contactEventId) async {
    final result = await _apiClient.post('/app/event/relation/save', {
      'selfEventId': selfEventId,
      'contactEventId': contactEventId,
    }, fromJsonT: (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  @override
  Future<void> deleteEventRelation(String id) async {
    await _apiClient.post('/app/event/relation/delete', {'id': id}, fromJsonT: (_) => null);
  }

  @override
  Future<List<EventRelationVO>> fetchEventRelationList(String selfEventId) async {
    return _apiClient.post('/app/event/relation/list', {
      'selfEventId': selfEventId,
    }, fromJsonT: (json) {
      final map = _asMap(json);
      return _asList(map['list']).map((item) {
        return EventRelationVO.fromJson(_asMap(item));
      }).toList();
    });
  }

  @override
  Future<List<SuggestRelationVO>> fetchSuggestRelations(String selfEventId) async {
    return _apiClient.post('/app/event/relation/suggest', {
      'selfEventId': selfEventId,
    }, fromJsonT: (json) {
      final map = _asMap(json);
      return _asList(map['list']).map((item) {
        return SuggestRelationVO.fromJson(_asMap(item));
      }).toList();
    });
  }

  @override
  Future<List<UnlinkedEventVO>> fetchUnlinkedEvents({String? eventTypeId, int pageNo = 1, int pageSize = 20}) async {
    return _apiClient.post('/app/event/relation/unlinked', {
      'eventTypeId': eventTypeId,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, fromJsonT: (json) {
      final page = _asMap(json);
      return _asList(page['list']).map((item) {
        return UnlinkedEventVO.fromJson(_asMap(item));
      }).toList();
    });
  }
}
