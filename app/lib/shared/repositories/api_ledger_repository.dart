import '../../core/network/api_client.dart';
import '../../core/network/api_exception.dart';
import '../models/ledger_models.dart';
import 'ledger_repository.dart';

class ApiLedgerRepository implements LedgerRepository {
  ApiLedgerRepository(this._client);

  final ApiClient _client;

  @override
  Future<HomeOverview> fetchHomeOverview() async {
    return _client.post('/app/home/overview', {}, (json) {
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
    return _client.post('/app/contact/page', {
      'keyword': keyword,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, (json) {
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
    final detail = await _client.post('/app/contact/detail', {'contactId': contactId}, (json) => _asMap(json));
    final timeline = await _client.post('/app/record/contact-timeline', {
      'contactId': contactId,
      'pageNo': 1,
      'pageSize': 20,
    }, (json) => _asMap(json));

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
    return _client.post('/app/record/self-timeline', {
      'direction': kind?.backendDirection,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, (json) {
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
    return _client.post('/app/reciprocity/page', {
      'reciprocityStatus': status.backendValue,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, (json) {
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
    return _client.post('/app/reciprocity/detail', {'recordId': recordId}, (json) {
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
    return _client.post('/app/dict/event-type/list', {'enabledFlag': true}, (json) {
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
    return _client.post('/app/dict/relation-type/list', {'keyword': keyword}, (json) {
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
    return _client.post('/app/event/page', {
      'eventOwnerType': kind.eventOwnerType,
      'ownerContactId': kind == RecordKind.give ? contactId : null,
      'pageNo': pageNo,
      'pageSize': pageSize,
    }, (json) {
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
    final eventId = draft.existingEventId ?? await _createEvent(draft);
    final result = await _client.post('/app/record/save', {
      'contactId': draft.contactId,
      'eventId': eventId,
      'direction': draft.kind.backendDirection,
      'amount': draft.amount.toStringAsFixed(2),
      'recordDate': _dateString(draft.recordDate),
      'remark': draft.recordRemark,
    }, (json) => _asMap(json));
    return _stringValue(result['id']);
  }

  @override
  Future<String> saveContact(ContactSaveDraft draft) async {
    final result = await _client.post('/app/contact/save', {
      'contactName': draft.name,
      'aliasName': draft.aliasName,
      'salutation': draft.salutation,
      'mobile': draft.mobile,
      'relationType': draft.relationTypeCode,
      'remark': draft.remark,
    }, (json) => _asMap(json));
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
    final result = await _client.post('/app/event/save', {
      'eventName': eventName,
      'eventTypeId': eventType.id,
      'eventOwnerType': draft.kind.eventOwnerType,
      'ownerContactId': draft.kind == RecordKind.give ? draft.contactId : null,
      'eventDate': _dateString(draft.recordDate),
      'remark': '',
    }, (json) => _asMap(json));
    return _stringValue(result['id']);
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

  static RecordKind _recordKindFromDirection(String value) {
    return value == 'RECEIVE' ? RecordKind.receive : RecordKind.give;
  }

  static String _dateString(DateTime value) {
    final year = value.year.toString().padLeft(4, '0');
    final month = value.month.toString().padLeft(2, '0');
    final day = value.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }
}
