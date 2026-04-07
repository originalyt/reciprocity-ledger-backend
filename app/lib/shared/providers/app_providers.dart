import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/auth/auth_api.dart';
import '../../core/network/api_client.dart';
import '../../shared/models/ledger_models.dart';
import '../repositories/api_ledger_repository.dart';
import '../repositories/ledger_repository.dart';

/// Provider that creates ApiClient with WidgetRef for authenticated requests
ApiClient createApiClient(WidgetRef ref) => ApiClient(ref: ref);

/// Provider that creates LedgerRepository with WidgetRef for authenticated requests
LedgerRepository createLedgerRepository(WidgetRef ref) =>
    ApiLedgerRepository(createApiClient(ref));

// ============================================
// Data refresh notification - trigger refresh when data changes
// ============================================

/// 数据刷新通知 Provider
/// 当数据变更时（如新增记录），调用 ref.read(dataRefreshProvider.notifier).refresh() 触发刷新
final dataRefreshProvider = NotifierProvider<DataRefreshNotifier, int>(() {
  return DataRefreshNotifier();
});

/// 数据刷新通知器
class DataRefreshNotifier extends Notifier<int> {
  @override
  int build() => 0;

  /// 触发刷新，增加版本号
  void refresh() => state++;
}

/// 触发数据刷新
void triggerDataRefresh(WidgetRef ref) {
  ref.read(dataRefreshProvider.notifier).refresh();
}

// ============================================
// Data fetch functions - call these from widgets with WidgetRef
// ============================================

Future<HomeOverview> fetchHomeOverview(WidgetRef ref) =>
    createLedgerRepository(ref).fetchHomeOverview();

Future<List<ContactOption>> fetchContacts(WidgetRef ref, {String? keyword}) =>
    createLedgerRepository(ref).fetchContacts(keyword: keyword);

Future<ContactDetail> fetchContactDetail(WidgetRef ref, String contactId) =>
    createLedgerRepository(ref).fetchContactDetail(contactId);

Future<TimelineBundle> fetchSelfTimeline(WidgetRef ref, {RecordKind? kind}) =>
    createLedgerRepository(ref).fetchSelfTimeline(kind: kind);

Future<List<ReciprocityEventSummary>> fetchReciprocityList(
        WidgetRef ref, ReciprocityStatus status) =>
    createLedgerRepository(ref).fetchReciprocityList(status);

Future<ReciprocityDetail> fetchReciprocityDetail(WidgetRef ref, String recordId) =>
    createLedgerRepository(ref).fetchReciprocityDetail(recordId);

Future<List<EventTypeOption>> fetchEventTypes(WidgetRef ref) =>
    createLedgerRepository(ref).fetchEventTypes();

Future<List<RelationTypeOption>> fetchRelationTypes(WidgetRef ref, {String? keyword}) =>
    createLedgerRepository(ref).fetchRelationTypes(keyword: keyword);

Future<List<EventOption>> fetchEventOptions(
        WidgetRef ref, {required RecordKind kind, String? contactId}) =>
    createLedgerRepository(ref).fetchEventOptions(kind: kind, contactId: contactId);

Future<EventsByContact> fetchEventsByContact(WidgetRef ref, String contactId) =>
    createLedgerRepository(ref).eventsByContact(contactId);

Future<String> saveRecord(WidgetRef ref, RecordSaveDraft draft) =>
    createLedgerRepository(ref).saveRecord(draft);

Future<String> saveContact(WidgetRef ref, ContactSaveDraft draft) =>
    createLedgerRepository(ref).saveContact(draft);

Future<ContactQuickSaveResult> quickSaveContact(WidgetRef ref, ContactSaveDraft draft) =>
    createLedgerRepository(ref).quickSaveContact(draft);

Future<String> saveRecordWithEvent(WidgetRef ref, RecordSaveWithEventDraft draft) =>
    createLedgerRepository(ref).saveRecordWithEvent(draft);

/// 简化的记录保存：自动创建或复用事件
Future<String> saveRecordSimple(WidgetRef ref, RecordSaveSimpleDraft draft) =>
    createLedgerRepository(ref).saveRecordSimple(draft);

// 事件关联相关
Future<String> saveEventRelation(WidgetRef ref, String selfEventId, String contactEventId) =>
    createLedgerRepository(ref).saveEventRelation(selfEventId, contactEventId);

Future<void> deleteEventRelation(WidgetRef ref, String id) =>
    createLedgerRepository(ref).deleteEventRelation(id);

Future<List<EventRelationVO>> fetchEventRelationList(WidgetRef ref, String selfEventId) =>
    createLedgerRepository(ref).fetchEventRelationList(selfEventId);

Future<List<SuggestRelationVO>> fetchSuggestRelations(WidgetRef ref, String selfEventId) =>
    createLedgerRepository(ref).fetchSuggestRelations(selfEventId);

Future<List<UnlinkedEventVO>> fetchUnlinkedEvents(WidgetRef ref, {String? eventTypeId}) =>
    createLedgerRepository(ref).fetchUnlinkedEvents(eventTypeId: eventTypeId);

/// 标记往来记录为无需往来
Future<String> markReciprocityNoNeed(WidgetRef ref, String recordId, {String? reason}) =>
    createLedgerRepository(ref).markReciprocityNoNeed(recordId, reason: reason);

// ============================================
// State providers for filters (no auth needed)
// ============================================

final authApiProvider = Provider<AuthApi>((ref) {
  return const AuthApi();
});

final contactsFilterProvider = NotifierProvider<ContactsFilterNotifier, ContactsFilter>(() {
  return ContactsFilterNotifier();
});

class ContactsFilterNotifier extends Notifier<ContactsFilter> {
  @override
  ContactsFilter build() => const ContactsFilter();

  void setKeyword(String? keyword) {
    state = state.copyWith(keyword: keyword);
  }
}

class ContactsFilter {
  const ContactsFilter({this.keyword});

  final String? keyword;

  ContactsFilter copyWith({String? keyword}) {
    return ContactsFilter(keyword: keyword);
  }
}

final timelineFilterProvider = NotifierProvider<TimelineFilterNotifier, RecordKind?>(() {
  return TimelineFilterNotifier();
});

class TimelineFilterNotifier extends Notifier<RecordKind?> {
  @override
  RecordKind? build() => null;

  void setFilter(RecordKind? filter) {
    state = filter;
  }
}

final reciprocityFilterProvider = NotifierProvider<ReciprocityFilterNotifier, ReciprocityStatus>(() {
  return ReciprocityFilterNotifier();
});

class ReciprocityFilterNotifier extends Notifier<ReciprocityStatus> {
  @override
  ReciprocityStatus build() => ReciprocityStatus.unmatched;

  void setStatus(ReciprocityStatus status) {
    state = status;
  }
}

final relationTypesFilterProvider = NotifierProvider<RelationTypesFilterNotifier, RelationTypesFilter>(() {
  return RelationTypesFilterNotifier();
});

class RelationTypesFilterNotifier extends Notifier<RelationTypesFilter> {
  @override
  RelationTypesFilter build() => const RelationTypesFilter();

  void setKeyword(String? keyword) {
    state = state.copyWith(keyword: keyword);
  }
}

class RelationTypesFilter {
  const RelationTypesFilter({this.keyword});

  final String? keyword;

  RelationTypesFilter copyWith({String? keyword}) {
    return RelationTypesFilter(keyword: keyword);
  }
}
