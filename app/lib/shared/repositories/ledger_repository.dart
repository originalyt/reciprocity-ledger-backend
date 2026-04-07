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
  Future<ContactQuickSaveResult> quickSaveContact(ContactSaveDraft draft);
  Future<EventsByContact> eventsByContact(String contactId);
  Future<String> saveRecordWithEvent(RecordSaveWithEventDraft draft);

  /// 简化的记录保存：自动创建或复用事件
  Future<String> saveRecordSimple(RecordSaveSimpleDraft draft);

  // 事件关联相关
  Future<String> saveEventRelation(String selfEventId, String contactEventId);
  Future<void> deleteEventRelation(String id);
  Future<List<EventRelationVO>> fetchEventRelationList(String selfEventId);
  Future<List<SuggestRelationVO>> fetchSuggestRelations(String selfEventId);
  Future<List<UnlinkedEventVO>> fetchUnlinkedEvents({String? eventTypeId, int pageNo = 1, int pageSize = 20});

  /// 标记往来记录为无需往来
  Future<String> markReciprocityNoNeed(String recordId, {String? reason});
}
