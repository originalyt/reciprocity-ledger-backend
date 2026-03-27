import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../../core/network/api_client.dart';
import '../models/ledger_models.dart';
import '../repositories/api_ledger_repository.dart';
import '../repositories/ledger_repository.dart';
import '../repositories/mock_ledger_repository.dart';

final useMockDataProvider = Provider<bool>((ref) {
  return const bool.fromEnvironment('USE_MOCK_DATA', defaultValue: false);
});

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient();
});

final ledgerRepositoryProvider = Provider<LedgerRepository>((ref) {
  if (ref.watch(useMockDataProvider)) {
    return MockLedgerRepository();
  }
  return ApiLedgerRepository(ref.watch(apiClientProvider));
});

final homeOverviewProvider = FutureProvider<HomeOverview>((ref) {
  return ref.watch(ledgerRepositoryProvider).fetchHomeOverview();
});

final contactsProvider = FutureProvider<List<ContactOption>>((ref) {
  return ref.watch(ledgerRepositoryProvider).fetchContacts();
});

final timelineFilterProvider = StateProvider<RecordKind?>((ref) => null);

final selfTimelineProvider = FutureProvider<TimelineBundle>((ref) {
  final filter = ref.watch(timelineFilterProvider);
  return ref.watch(ledgerRepositoryProvider).fetchSelfTimeline(kind: filter);
});

final reciprocityFilterProvider = StateProvider<ReciprocityStatus>((ref) => ReciprocityStatus.unmatched);

final reciprocityListProvider = FutureProvider<List<ReciprocityEventSummary>>((ref) {
  final status = ref.watch(reciprocityFilterProvider);
  return ref.watch(ledgerRepositoryProvider).fetchReciprocityList(status);
});

final contactDetailProvider = FutureProvider.family<ContactDetail, String>((ref, id) {
  return ref.watch(ledgerRepositoryProvider).fetchContactDetail(id);
});

final reciprocityDetailProvider = FutureProvider.family<ReciprocityDetail, String>((ref, id) {
  return ref.watch(ledgerRepositoryProvider).fetchReciprocityDetail(id);
});

final eventTypesProvider = FutureProvider<List<EventTypeOption>>((ref) {
  return ref.watch(ledgerRepositoryProvider).fetchEventTypes();
});

final eventOptionsProvider = FutureProvider.family<List<EventOption>, ({RecordKind kind, String? contactId})>((ref, args) {
  return ref.watch(ledgerRepositoryProvider).fetchEventOptions(kind: args.kind, contactId: args.contactId);
});
