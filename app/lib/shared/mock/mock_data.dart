import '../models/ledger_models.dart';

class MockLedgerData {
  MockLedgerData._();

  static const recentContacts = [
    ContactOption(id: 'c1', name: '张三', relation: '表弟'),
    ContactOption(id: 'c2', name: '李阿姨', relation: '邻里'),
    ContactOption(id: 'c3', name: '王老师', relation: '师长'),
    ContactOption(id: 'c4', name: '周同学', relation: '同学'),
  ];

  static const commonEventTypes = ['婚礼', '白事', '生日', '乔迁', '满月'];

  static final homeOverview = HomeOverview(
    totalGive: 12800,
    totalReceive: 17600,
    netAmount: 4800,
    pending: const PendingSummary(
      waitMeCount: 3,
      waitOtherCount: 5,
      noNeedCount: 2,
    ),
    recentRecords: [
      RecentRecord(
        id: 'r1',
        contactId: 'c1',
        contactName: '张三',
        eventType: '婚礼',
        eventNote: '表弟结婚',
        kind: RecordKind.give,
        amount: 800,
        occurredOn: DateTime(2026, 3, 18),
      ),
      RecentRecord(
        id: 'r2',
        contactId: 'c2',
        contactName: '李阿姨',
        eventType: '白事',
        eventNote: '邻里白事',
        kind: RecordKind.receive,
        amount: 500,
        occurredOn: DateTime(2026, 3, 16),
      ),
      RecentRecord(
        id: 'r3',
        contactId: 'c3',
        contactName: '王老师',
        eventType: '生日',
        eventNote: '老师寿宴',
        kind: RecordKind.give,
        amount: 300,
        occurredOn: DateTime(2026, 3, 10),
      ),
      RecentRecord(
        id: 'r4',
        contactId: 'c4',
        contactName: '周同学',
        eventType: '乔迁',
        eventNote: '新房入伙',
        kind: RecordKind.receive,
        amount: 600,
        occurredOn: DateTime(2026, 3, 6),
      ),
    ],
  );

  static final timelineEntries = [
    TimelineEntry(
      id: 't1',
      contactId: 'c1',
      contactName: '张三',
      relation: '表弟',
      eventType: '婚礼',
      eventNote: '表弟结婚',
      kind: RecordKind.give,
      amount: 800,
      occurredOn: DateTime(2026, 3, 18),
      remark: '现场随礼',
    ),
    TimelineEntry(
      id: 't2',
      contactId: 'c2',
      contactName: '李阿姨',
      relation: '邻里',
      eventType: '白事',
      eventNote: '邻里白事',
      kind: RecordKind.receive,
      amount: 500,
      occurredOn: DateTime(2026, 3, 16),
      remark: '家中往来',
    ),
    TimelineEntry(
      id: 't3',
      contactId: 'c3',
      contactName: '王老师',
      relation: '师长',
      eventType: '生日',
      eventNote: '老师寿宴',
      kind: RecordKind.give,
      amount: 300,
      occurredOn: DateTime(2026, 3, 10),
      remark: '感谢老师照顾',
    ),
    TimelineEntry(
      id: 't4',
      contactId: 'c4',
      contactName: '周同学',
      relation: '同学',
      eventType: '乔迁',
      eventNote: '新房入伙',
      kind: RecordKind.receive,
      amount: 600,
      occurredOn: DateTime(2026, 3, 6),
      remark: '同学聚会见面',
    ),
    TimelineEntry(
      id: 't5',
      contactId: 'c1',
      contactName: '张三',
      relation: '表弟',
      eventType: '满月',
      eventNote: '孩子满月',
      kind: RecordKind.receive,
      amount: 1000,
      occurredOn: DateTime(2026, 2, 24),
      remark: '对方先送来',
    ),
  ];

  static final contactDetails = [
    ContactDetail(
      id: 'c1',
      name: '张三',
      relation: '表弟',
      phone: '138****1024',
      note: '老家亲属，往来比较频繁。',
      totalGive: 2600,
      totalReceive: 1800,
      netAmount: -800,
      lastInteractionOn: DateTime(2026, 3, 18),
      timeline: timelineEntries.where((item) => item.contactId == 'c1').toList(),
    ),
    ContactDetail(
      id: 'c2',
      name: '李阿姨',
      relation: '邻里',
      phone: '139****6608',
      note: '老邻居，逢事都会往来。',
      totalGive: 900,
      totalReceive: 1400,
      netAmount: 500,
      lastInteractionOn: DateTime(2026, 3, 16),
      timeline: timelineEntries.where((item) => item.contactId == 'c2').toList(),
    ),
    ContactDetail(
      id: 'c3',
      name: '王老师',
      relation: '师长',
      phone: '暂无',
      note: '多年恩师，保持联系。',
      totalGive: 300,
      totalReceive: 0,
      netAmount: -300,
      lastInteractionOn: DateTime(2026, 3, 10),
      timeline: timelineEntries.where((item) => item.contactId == 'c3').toList(),
    ),
    ContactDetail(
      id: 'c4',
      name: '周同学',
      relation: '同学',
      phone: '137****8891',
      note: '大学同学，近两年联系恢复。',
      totalGive: 0,
      totalReceive: 600,
      netAmount: 600,
      lastInteractionOn: DateTime(2026, 3, 6),
      timeline: timelineEntries.where((item) => item.contactId == 'c4').toList(),
    ),
  ];

  static final reciprocityEvents = [
    ReciprocityEventSummary(
      id: 'e1',
      contactId: 'c1',
      contactName: '张三',
      relation: '表弟',
      eventType: '婚礼',
      eventNote: '表弟结婚',
      status: ReciprocityStatus.waitMe,
      netAmount: -800,
      latestDate: DateTime(2026, 3, 18),
    ),
    ReciprocityEventSummary(
      id: 'e2',
      contactId: 'c2',
      contactName: '李阿姨',
      relation: '邻里',
      eventType: '白事',
      eventNote: '邻里白事',
      status: ReciprocityStatus.waitOther,
      netAmount: 500,
      latestDate: DateTime(2026, 3, 16),
    ),
    ReciprocityEventSummary(
      id: 'e3',
      contactId: 'c3',
      contactName: '王老师',
      relation: '师长',
      eventType: '生日',
      eventNote: '老师寿宴',
      status: ReciprocityStatus.mutual,
      netAmount: 0,
      latestDate: DateTime(2026, 3, 10),
    ),
    ReciprocityEventSummary(
      id: 'e4',
      contactId: 'c4',
      contactName: '周同学',
      relation: '同学',
      eventType: '乔迁',
      eventNote: '新房入伙',
      status: ReciprocityStatus.noNeed,
      netAmount: 600,
      latestDate: DateTime(2026, 3, 6),
    ),
  ];

  static final reciprocityDetails = [
    ReciprocityDetail(
      id: 'e1',
      contactId: 'c1',
      contactName: '张三',
      relation: '表弟',
      eventType: '婚礼',
      eventNote: '表弟结婚',
      status: ReciprocityStatus.waitMe,
      netAmount: -800,
      latestDate: DateTime(2026, 3, 18),
      receiveRecord: const ReciprocityRecord(
        label: '收礼记录',
        amount: 0,
        occurredOn: null,
        remark: '暂无记录',
      ),
      giveRecord: ReciprocityRecord(
        label: '随礼记录',
        amount: 800,
        occurredOn: DateTime(2026, 3, 18),
        remark: '婚礼当天到场随礼',
      ),
      exemptReason: null,
    ),
    ReciprocityDetail(
      id: 'e2',
      contactId: 'c2',
      contactName: '李阿姨',
      relation: '邻里',
      eventType: '白事',
      eventNote: '邻里白事',
      status: ReciprocityStatus.waitOther,
      netAmount: 500,
      latestDate: DateTime(2026, 3, 16),
      receiveRecord: ReciprocityRecord(
        label: '收礼记录',
        amount: 500,
        occurredOn: DateTime(2026, 3, 16),
        remark: '对方来家中吊唁',
      ),
      giveRecord: const ReciprocityRecord(
        label: '随礼记录',
        amount: 0,
        occurredOn: null,
        remark: '暂无记录',
      ),
      exemptReason: null,
    ),
    ReciprocityDetail(
      id: 'e3',
      contactId: 'c3',
      contactName: '王老师',
      relation: '师长',
      eventType: '生日',
      eventNote: '老师寿宴',
      status: ReciprocityStatus.mutual,
      netAmount: 0,
      latestDate: DateTime(2026, 3, 10),
      receiveRecord: ReciprocityRecord(
        label: '收礼记录',
        amount: 300,
        occurredOn: DateTime(2025, 10, 12),
        remark: '之前老师给过祝贺礼',
      ),
      giveRecord: ReciprocityRecord(
        label: '随礼记录',
        amount: 300,
        occurredOn: DateTime(2026, 3, 10),
        remark: '寿宴当天回礼',
      ),
      exemptReason: null,
    ),
    ReciprocityDetail(
      id: 'e4',
      contactId: 'c4',
      contactName: '周同学',
      relation: '同学',
      eventType: '乔迁',
      eventNote: '新房入伙',
      status: ReciprocityStatus.noNeed,
      netAmount: 600,
      latestDate: DateTime(2026, 3, 6),
      receiveRecord: ReciprocityRecord(
        label: '收礼记录',
        amount: 600,
        occurredOn: DateTime(2026, 3, 6),
        remark: '同学聚会顺手带来礼金',
      ),
      giveRecord: const ReciprocityRecord(
        label: '随礼记录',
        amount: 0,
        occurredOn: null,
        remark: '暂无记录',
      ),
      exemptReason: '同学之间礼节性往来，不单独回礼。',
    ),
  ];

  static ContactDetail? findContactDetail(String id) {
    for (final item in contactDetails) {
      if (item.id == id) {
        return item;
      }
    }
    return null;
  }

  static ReciprocityDetail? findReciprocityDetail(String id) {
    for (final item in reciprocityDetails) {
      if (item.id == id) {
        return item;
      }
    }
    return null;
  }
}
