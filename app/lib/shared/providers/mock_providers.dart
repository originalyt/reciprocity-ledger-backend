import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../mock/mock_data.dart';
import '../models/ledger_models.dart';

final homeOverviewProvider = Provider<HomeOverview>((ref) {
  return MockLedgerData.homeOverview;
});

final recentContactsProvider = Provider<List<ContactOption>>((ref) {
  return MockLedgerData.recentContacts;
});

final commonEventTypesProvider = Provider<List<String>>((ref) {
  return MockLedgerData.commonEventTypes;
});

final timelineFilterProvider = StateProvider<RecordKind?>((ref) {
  return null;
});

final timelineEntriesProvider = Provider<List<TimelineEntry>>((ref) {
  return MockLedgerData.timelineEntries;
});

final filteredTimelineEntriesProvider = Provider<List<TimelineEntry>>((ref) {
  final filter = ref.watch(timelineFilterProvider);
  final all = ref.watch(timelineEntriesProvider);
  if (filter == null) {
    return all;
  }
  return all.where((item) => item.kind == filter).toList();
});

final reciprocityFilterProvider = StateProvider<ReciprocityStatus>((ref) {
  return ReciprocityStatus.waitMe;
});

final reciprocityEventsProvider = Provider<List<ReciprocityEventSummary>>((ref) {
  return MockLedgerData.reciprocityEvents;
});

final filteredReciprocityEventsProvider = Provider<List<ReciprocityEventSummary>>((ref) {
  final status = ref.watch(reciprocityFilterProvider);
  final all = ref.watch(reciprocityEventsProvider);
  return all.where((item) => item.status == status).toList();
});

final contactDetailProvider = Provider.family<ContactDetail?, String>((ref, id) {
  return MockLedgerData.findContactDetail(id);
});

final reciprocityDetailProvider = Provider.family<ReciprocityDetail?, String>((ref, id) {
  return MockLedgerData.findReciprocityDetail(id);
});
