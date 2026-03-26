import 'package:flutter/material.dart';

enum RecordKind { give, receive }

enum ReciprocityStatus { waitMe, waitOther, mutual, noNeed }

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
}

extension ReciprocityStatusX on ReciprocityStatus {
  String get label {
    switch (this) {
      case ReciprocityStatus.waitMe:
        return '我待回礼';
      case ReciprocityStatus.waitOther:
        return '待对方回礼';
      case ReciprocityStatus.mutual:
        return '已互回';
      case ReciprocityStatus.noNeed:
        return '无需回礼';
    }
  }
}

class ContactOption {
  const ContactOption({
    required this.id,
    required this.name,
    required this.relation,
  });

  final String id;
  final String name;
  final String relation;
}

class PendingSummary {
  const PendingSummary({
    required this.waitMeCount,
    required this.waitOtherCount,
    required this.noNeedCount,
  });

  final int waitMeCount;
  final int waitOtherCount;
  final int noNeedCount;
}

class RecentRecord {
  const RecentRecord({
    required this.id,
    required this.contactId,
    required this.contactName,
    required this.eventType,
    required this.eventNote,
    required this.kind,
    required this.amount,
    required this.occurredOn,
  });

  final String id;
  final String contactId;
  final String contactName;
  final String eventType;
  final String eventNote;
  final RecordKind kind;
  final double amount;
  final DateTime occurredOn;
}

class HomeOverview {
  const HomeOverview({
    required this.totalGive,
    required this.totalReceive,
    required this.netAmount,
    required this.pending,
    required this.recentRecords,
  });

  final double totalGive;
  final double totalReceive;
  final double netAmount;
  final PendingSummary pending;
  final List<RecentRecord> recentRecords;
}

class TimelineEntry {
  const TimelineEntry({
    required this.id,
    required this.contactId,
    required this.contactName,
    required this.relation,
    required this.eventType,
    required this.eventNote,
    required this.kind,
    required this.amount,
    required this.occurredOn,
    required this.remark,
  });

  final String id;
  final String contactId;
  final String contactName;
  final String relation;
  final String eventType;
  final String eventNote;
  final RecordKind kind;
  final double amount;
  final DateTime occurredOn;
  final String remark;
}

class ContactDetail {
  const ContactDetail({
    required this.id,
    required this.name,
    required this.relation,
    required this.phone,
    required this.note,
    required this.totalGive,
    required this.totalReceive,
    required this.netAmount,
    required this.lastInteractionOn,
    required this.timeline,
  });

  final String id;
  final String name;
  final String relation;
  final String phone;
  final String note;
  final double totalGive;
  final double totalReceive;
  final double netAmount;
  final DateTime lastInteractionOn;
  final List<TimelineEntry> timeline;
}

class ReciprocityEventSummary {
  const ReciprocityEventSummary({
    required this.id,
    required this.contactId,
    required this.contactName,
    required this.relation,
    required this.eventType,
    required this.eventNote,
    required this.status,
    required this.netAmount,
    required this.latestDate,
  });

  final String id;
  final String contactId;
  final String contactName;
  final String relation;
  final String eventType;
  final String eventNote;
  final ReciprocityStatus status;
  final double netAmount;
  final DateTime latestDate;
}

class ReciprocityRecord {
  const ReciprocityRecord({
    required this.label,
    required this.amount,
    required this.occurredOn,
    required this.remark,
  });

  final String label;
  final double amount;
  final DateTime? occurredOn;
  final String remark;
}

class ReciprocityDetail {
  const ReciprocityDetail({
    required this.id,
    required this.contactId,
    required this.contactName,
    required this.relation,
    required this.eventType,
    required this.eventNote,
    required this.status,
    required this.netAmount,
    required this.latestDate,
    required this.receiveRecord,
    required this.giveRecord,
    required this.exemptReason,
  });

  final String id;
  final String contactId;
  final String contactName;
  final String relation;
  final String eventType;
  final String eventNote;
  final ReciprocityStatus status;
  final double netAmount;
  final DateTime latestDate;
  final ReciprocityRecord? receiveRecord;
  final ReciprocityRecord? giveRecord;
  final String? exemptReason;
}
