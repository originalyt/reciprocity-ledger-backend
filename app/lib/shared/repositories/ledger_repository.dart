import '../models/ledger_models.dart';

abstract class LedgerRepository {
  Future<HomeOverview> fetchHomeOverview();

  Future<List<ContactOption>> fetchContacts({String? keyword, int pageNo = 1, int pageSize = 50});

  Future<ContactDetail> fetchContactDetail(String contactId);

  Future<TimelineBundle> fetchSelfTimeline({RecordKind? kind, int pageNo = 1, int pageSize = 50});

  Future<List<ReciprocityEventSummary>> fetchReciprocityList(ReciprocityStatus status, {int pageNo = 1, int pageSize = 50});

  Future<ReciprocityDetail> fetchReciprocityDetail(String recordId);

  Future<List<EventTypeOption>> fetchEventTypes();

  Future<List<RelationTypeOption>> fetchRelationTypes({String? keyword});

  Future<List<EventOption>> fetchEventOptions({required RecordKind kind, String? contactId, int pageNo = 1, int pageSize = 50});

  Future<String> saveRecord(RecordSaveDraft draft);

  Future<String> saveContact(ContactSaveDraft draft);
}
