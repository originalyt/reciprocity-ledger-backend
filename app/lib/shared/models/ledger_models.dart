import 'package:flutter/material.dart';

enum RecordKind { give, receive }

enum ReciprocityStatus { unmatched, matched, manualConfirmed, manualCanceled }

RecordKind? parseRecordKind(String? value) {
  switch (value) {
    case 'give':
      return RecordKind.give;
    case 'receive':
      return RecordKind.receive;
    default:
      return null;
  }
}

extension RecordKindX on RecordKind {
  String get label {
    switch (this) {
      case RecordKind.give:
        return '随礼';
      case RecordKind.receive:
        return '收礼';
    }
  }

  IconData get icon {
    switch (this) {
      case RecordKind.give:
        return Icons.north_east_rounded;
      case RecordKind.receive:
        return Icons.south_west_rounded;
    }
  }

  String get routeValue {
    switch (this) {
      case RecordKind.give:
        return 'give';
      case RecordKind.receive:
        return 'receive';
    }
  }

  String get backendDirection {
    switch (this) {
      case RecordKind.give:
        return 'SEND';
      case RecordKind.receive:
        return 'RECEIVE';
    }
  }

  String get eventOwnerType {
    switch (this) {
      case RecordKind.give:
        return 'CONTACT';
      case RecordKind.receive:
        return 'SELF';
    }
  }
}

extension ReciprocityStatusX on ReciprocityStatus {
  String get label {
    switch (this) {
      case ReciprocityStatus.unmatched:
        return '未闭环';
      case ReciprocityStatus.matched:
        return '已闭环';
      case ReciprocityStatus.manualConfirmed:
        return '人工确认';
      case ReciprocityStatus.manualCanceled:
        return '已取消';
    }
  }

  String get backendValue {
    switch (this) {
      case ReciprocityStatus.unmatched:
        return 'UNMATCHED';
      case ReciprocityStatus.matched:
        return 'MATCHED';
      case ReciprocityStatus.manualConfirmed:
        return 'MANUAL_CONFIRMED';
      case ReciprocityStatus.manualCanceled:
        return 'MANUAL_CANCELED';
    }
  }
}

ReciprocityStatus parseReciprocityStatus(String? value) {
  switch (value) {
    case 'MATCHED':
      return ReciprocityStatus.matched;
    case 'MANUAL_CONFIRMED':
      return ReciprocityStatus.manualConfirmed;
    case 'MANUAL_CANCELED':
      return ReciprocityStatus.manualCanceled;
    case 'UNMATCHED':
    default:
      return ReciprocityStatus.unmatched;
  }
}

class ContactOption {
  const ContactOption({required this.id, required this.name, required this.relation});

  final String id;
  final String name;
  final String relation;
}

class RelationTypeOption {
  const RelationTypeOption({required this.id, required this.code, required this.name});

  final String id;
  final String code;
  final String name;
}

class EventTypeOption {
  const EventTypeOption({required this.id, required this.code, required this.name});

  final String id;
  final String code;
  final String name;
}

class EventOption {
  const EventOption({
    required this.id,
    required this.name,
    required this.eventTypeId,
    required this.eventTypeCode,
    required this.eventTypeName,
    required this.ownerType,
    required this.ownerContactId,
    required this.eventDate,
  });

  final String id;
  final String name;
  final String eventTypeId;
  final String eventTypeCode;
  final String eventTypeName;
  final String ownerType;
  final String? ownerContactId;
  final DateTime eventDate;
}

class RecentRecord {
  const RecentRecord({
    required this.id,
    required this.contactId,
    required this.contactName,
    required this.eventId,
    required this.eventName,
    required this.eventTypeCode,
    required this.eventTypeName,
    required this.kind,
    required this.amount,
    required this.recordDate,
    required this.reciprocityStatus,
  });

  final String id;
  final String contactId;
  final String contactName;
  final String eventId;
  final String eventName;
  final String eventTypeCode;
  final String eventTypeName;
  final RecordKind kind;
  final double amount;
  final DateTime recordDate;
  final ReciprocityStatus reciprocityStatus;
}

class HomeOverview {
  const HomeOverview({
    required this.totalGive,
    required this.totalReceive,
    required this.pendingReciprocityCount,
    required this.recentRecords,
  });

  final double totalGive;
  final double totalReceive;
  final int pendingReciprocityCount;
  final List<RecentRecord> recentRecords;

  double get netAmount => totalReceive - totalGive;
}

class TimelineSummary {
  const TimelineSummary({
    required this.totalGive,
    required this.totalReceive,
    required this.netAmount,
    required this.recordCount,
  });

  final double totalGive;
  final double totalReceive;
  final double netAmount;
  final int recordCount;
}

class TimelineEntry {
  const TimelineEntry({
    required this.id,
    required this.contactId,
    required this.contactName,
    required this.eventId,
    required this.eventName,
    required this.eventTypeId,
    required this.eventTypeCode,
    required this.eventTypeName,
    required this.kind,
    required this.amount,
    required this.recordDate,
    required this.reciprocityStatus,
    this.remark = '',
    this.relation = '',
  });

  final String id;
  final String contactId;
  final String contactName;
  final String eventId;
  final String eventName;
  final String eventTypeId;
  final String eventTypeCode;
  final String eventTypeName;
  final RecordKind kind;
  final double amount;
  final DateTime recordDate;
  final ReciprocityStatus reciprocityStatus;
  final String remark;
  final String relation;
}

class TimelineBundle {
  const TimelineBundle({required this.summary, required this.entries});

  final TimelineSummary summary;
  final List<TimelineEntry> entries;
}

class ContactDetail {
  const ContactDetail({
    required this.id,
    required this.name,
    required this.mobile,
    required this.relation,
    required this.remark,
    required this.totalGive,
    required this.totalReceive,
    required this.netAmount,
    required this.lastRecordDate,
    required this.unclosedReciprocityCount,
    required this.timeline,
  });

  final String id;
  final String name;
  final String mobile;
  final String relation;
  final String remark;
  final double totalGive;
  final double totalReceive;
  final double netAmount;
  final DateTime? lastRecordDate;
  final int unclosedReciprocityCount;
  final List<TimelineEntry> timeline;
}

class ReciprocityRecordItem {
  const ReciprocityRecordItem({
    required this.eventId,
    required this.eventName,
    required this.eventOwnerType,
    required this.amount,
    required this.recordDate,
  });

  final String eventId;
  final String eventName;
  final String eventOwnerType;
  final double amount;
  final DateTime recordDate;
}

class ReciprocityEventSummary {
  const ReciprocityEventSummary({
    required this.matchId,
    required this.contactId,
    required this.contactName,
    required this.eventTypeId,
    required this.eventTypeName,
    required this.matchType,
    required this.status,
    required this.records,
  });

  final String matchId;
  final String contactId;
  final String contactName;
  final String eventTypeId;
  final String eventTypeName;
  final String matchType;
  final ReciprocityStatus status;
  final List<ReciprocityRecordItem> records;

  /// 获取SELF事件（我办的事件，对方随礼）
  ReciprocityRecordItem? get selfRecord {
    for (final record in records) {
      if (record.eventOwnerType == 'SELF') {
        return record;
      }
    }
    return null;
  }

  /// 获取CONTACT事件（联系人办的事件，我随礼）
  ReciprocityRecordItem? get contactRecord {
    for (final record in records) {
      if (record.eventOwnerType == 'CONTACT') {
        return record;
      }
    }
    return null;
  }
}

class RecordInfo {
  const RecordInfo({
    required this.recordId,
    required this.contactId,
    required this.contactName,
    required this.eventId,
    required this.eventName,
    required this.eventTypeId,
    required this.eventTypeCode,
    required this.eventTypeName,
    required this.kind,
    required this.amount,
    required this.recordDate,
    required this.remark,
    required this.reciprocityStatus,
  });

  final String recordId;
  final String contactId;
  final String contactName;
  final String eventId;
  final String eventName;
  final String eventTypeId;
  final String eventTypeCode;
  final String eventTypeName;
  final RecordKind kind;
  final double amount;
  final DateTime recordDate;
  final String remark;
  final ReciprocityStatus reciprocityStatus;
}

class ReciprocityHistoryReference {
  const ReciprocityHistoryReference({
    required this.sameTypeReceiveAmount,
    required this.sameTypeSendAmount,
    required this.unclosedRecordCount,
    required this.lastSameTypeRecord,
  });

  final double sameTypeReceiveAmount;
  final double sameTypeSendAmount;
  final int unclosedRecordCount;
  final TimelineEntry? lastSameTypeRecord;
}

class ReciprocityManualInfo {
  const ReciprocityManualInfo({
    required this.matchId,
    required this.matchType,
    required this.matchStatus,
    required this.cancelReason,
    required this.remark,
  });

  final String matchId;
  final String matchType;
  final String matchStatus;
  final String? cancelReason;
  final String? remark;
}

class ReciprocityDetail {
  const ReciprocityDetail({
    required this.record,
    required this.matchedRecord,
    required this.historyReference,
    required this.manualInfo,
  });

  final RecordInfo record;
  final RecordInfo? matchedRecord;
  final ReciprocityHistoryReference historyReference;
  final ReciprocityManualInfo? manualInfo;
}

class RecordSaveSimpleDraft {
  const RecordSaveSimpleDraft({
    required this.contactId,
    required this.kind,
    required this.recordDate,
    required this.amount,
    required this.eventTypeId,
    this.eventName,
    this.recordRemark,
  });

  final String contactId;
  final RecordKind kind;
  final DateTime recordDate;
  final double amount;
  final String eventTypeId;
  final String? eventName;
  final String? recordRemark;
}

class RecordSaveDraft {
  const RecordSaveDraft({
    required this.contactId,
    required this.kind,
    required this.recordDate,
    required this.amount,
    required this.recordRemark,
    this.existingEventId,
    this.newEventName,
    this.newEventType,
  });

  final String contactId;
  final RecordKind kind;
  final DateTime recordDate;
  final double amount;
  final String recordRemark;
  final String? existingEventId;
  final String? newEventName;
  final EventTypeOption? newEventType;
}

class ContactSaveDraft {
  const ContactSaveDraft({
    required this.name,
    required this.relationTypeCode,
    this.aliasName,
    this.salutation,
    this.mobile,
    this.remark,
  });

  final String name;
  final String relationTypeCode;
  final String? aliasName;
  final String? salutation;
  final String? mobile;
  final String? remark;
}

class ContactQuickSaveResult {
  const ContactQuickSaveResult({
    required this.contactId,
    required this.contactName,
    this.aliasName,
    this.relationType,
  });

  final String contactId;
  final String contactName;
  final String? aliasName;
  final String? relationType;
}

class EventsByContact {
  const EventsByContact({
    required this.contactId,
    required this.contactName,
    required this.selfEventList,
    required this.contactEventList,
  });

  final String contactId;
  final String contactName;
  final List<EventOption> selfEventList;
  final List<EventOption> contactEventList;
}

class RecordSaveWithEventDraft {
  const RecordSaveWithEventDraft({
    required this.contactId,
    required this.kind,
    required this.recordDate,
    required this.amount,
    required this.recordRemark,
    required this.eventName,
    required this.eventTypeId,
    required this.eventOwnerType,
    this.ownerContactId,
    this.eventRemark,
  });

  final String contactId;
  final RecordKind kind;
  final DateTime recordDate;
  final double amount;
  final String recordRemark;
  final String eventName;
  final String eventTypeId;
  final String eventOwnerType;
  final String? ownerContactId;
  final String? eventRemark;
}

/// 事件关联VO
class EventRelationVO {
  const EventRelationVO({
    required this.id,
    required this.contactEventId,
    required this.eventName,
    required this.eventDate,
    required this.contactName,
    required this.sendAmount,
  });

  final String id;
  final String contactEventId;
  final String eventName;
  final DateTime eventDate;
  final String contactName;
  final double sendAmount;

  factory EventRelationVO.fromJson(Map<String, dynamic> json) {
    return EventRelationVO(
      id: json['id'] as String,
      contactEventId: json['contactEventId'] as String,
      eventName: json['eventName'] as String,
      eventDate: DateTime.parse(json['eventDate'] as String),
      contactName: json['contactName'] as String,
      sendAmount: (json['sendAmount'] as num).toDouble(),
    );
  }
}

/// 可关联事件建议VO
class SuggestRelationVO {
  const SuggestRelationVO({
    required this.eventId,
    required this.eventName,
    required this.eventDate,
    required this.contactName,
    required this.sendAmount,
  });

  final String eventId;
  final String eventName;
  final DateTime eventDate;
  final String contactName;
  final double sendAmount;

  factory SuggestRelationVO.fromJson(Map<String, dynamic> json) {
    return SuggestRelationVO(
      eventId: json['eventId'] as String,
      eventName: json['eventName'] as String,
      eventDate: DateTime.parse(json['eventDate'] as String),
      contactName: json['contactName'] as String,
      sendAmount: (json['sendAmount'] as num).toDouble(),
    );
  }
}

/// 未关联事件VO（待还提醒）
class UnlinkedEventVO {
  const UnlinkedEventVO({
    required this.eventId,
    required this.eventName,
    required this.eventTypeId,
    required this.eventTypeName,
    required this.eventDate,
    required this.contactId,
    required this.contactName,
    required this.sendAmount,
  });

  final String eventId;
  final String eventName;
  final String eventTypeId;
  final String eventTypeName;
  final DateTime eventDate;
  final String contactId;
  final String contactName;
  final double sendAmount;

  factory UnlinkedEventVO.fromJson(Map<String, dynamic> json) {
    return UnlinkedEventVO(
      eventId: json['eventId'] as String,
      eventName: json['eventName'] as String,
      eventTypeId: json['eventTypeId'] as String,
      eventTypeName: json['eventTypeName'] as String,
      eventDate: DateTime.parse(json['eventDate'] as String),
      contactId: json['contactId'] as String,
      contactName: json['contactName'] as String,
      sendAmount: (json['sendAmount'] as num).toDouble(),
    );
  }
}
