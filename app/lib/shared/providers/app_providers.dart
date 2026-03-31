import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/auth/auth_api.dart';
import '../../core/network/api_client.dart';
import '../../shared/models/ledger_models.dart';
import '../repositories/api_ledger_repository.dart';
import '../repositories/ledger_repository.dart';

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient();
});

final authApiProvider = Provider<AuthApi>((ref) {
  return const AuthApi();
});

final ledgerRepositoryProvider = Provider<LedgerRepository>((ref) {
  return ApiLedgerRepository(ref.read(apiClientProvider));
});

final homeOverviewProvider = FutureProvider<HomeOverview>((ref) {
  return ref.read(ledgerRepositoryProvider).fetchHomeOverview();
});

final contactsProvider = FutureProvider.autoDispose<List<ContactOption>>((ref) async {
  final filter = ref.watch(contactsFilterProvider);
  return ref.read(ledgerRepositoryProvider).fetchContacts(keyword: filter.keyword);
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

final selfTimelineProvider = FutureProvider<TimelineBundle>((ref) {
  final filter = ref.watch(timelineFilterProvider);
  return ref.read(ledgerRepositoryProvider).fetchSelfTimeline(kind: filter);
});

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

final reciprocityListProvider = FutureProvider<List<ReciprocityEventSummary>>((ref) {
  final filter = ref.watch(reciprocityFilterProvider);
  return ref.read(ledgerRepositoryProvider).fetchReciprocityList(filter);
});

final contactDetailProvider = FutureProvider.family<ContactDetail, String>((ref, id) {
  return ref.read(ledgerRepositoryProvider).fetchContactDetail(id);
});

final reciprocityDetailProvider = FutureProvider.family<ReciprocityDetail, String>((ref, id) {
  return ref.read(ledgerRepositoryProvider).fetchReciprocityDetail(id);
});

final eventTypesProvider = FutureProvider<List<EventTypeOption>>((ref) {
  return ref.read(ledgerRepositoryProvider).fetchEventTypes();
});

final relationTypesProvider = FutureProvider<List<RelationTypeOption>>((ref) {
  final filter = ref.watch(relationTypesFilterProvider);
  return ref.read(ledgerRepositoryProvider).fetchRelationTypes(keyword: filter.keyword);
});

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

final eventOptionsProvider = FutureProvider.family<List<EventOption>, ({RecordKind kind, String? contactId})>((ref, args) {
  return ref.read(ledgerRepositoryProvider).fetchEventOptions(kind: args.kind, contactId: args.contactId);
});

final eventsByContactProvider = FutureProvider.family<EventsByContact, String>((ref, id) {
  return ref.read(ledgerRepositoryProvider).eventsByContact(id);
});
