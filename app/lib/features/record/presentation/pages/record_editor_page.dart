import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_primary_button.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/app_providers.dart';
import '../widgets/quick_add_contact_dialog.dart';

class RecordEditorPage extends ConsumerStatefulWidget {
  const RecordEditorPage({super.key, this.initialKind, this.initialContactId});

  final RecordKind? initialKind;
  final String? initialContactId;

  @override
  ConsumerState<RecordEditorPage> createState() => _RecordEditorPageState();
}

class _RecordEditorPageState extends ConsumerState<RecordEditorPage> {
  final _formKey = GlobalKey<FormState>();
  final _eventNameController = TextEditingController();
  final _amountController = TextEditingController();
  final _remarkController = TextEditingController();

  late RecordKind _selectedKind;
  late DateTime _selectedDate;
  String? _selectedContactId;
  String? _selectedExistingEventId;
  EventTypeOption? _selectedEventType;
  bool _createNewEvent = false;
  bool _submitting = false;

  // Data
  List<ContactOption>? _contacts;
  Object? _contactsError;
  bool _loadingContacts = true;

  // Events
  EventsByContact? _eventsByContact;
  Object? _eventsError;
  bool _loadingEvents = false;

  // Event types
  List<EventTypeOption>? _eventTypes;
  Object? _eventTypesError;
  bool _loadingEventTypes = false;

  @override
  void initState() {
    super.initState();
    _selectedKind = widget.initialKind ?? RecordKind.give;
    _selectedDate = DateTime.now();
    _selectedContactId = widget.initialContactId;
    _loadContacts();
  }

  @override
  void dispose() {
    _eventNameController.dispose();
    _amountController.dispose();
    _remarkController.dispose();
    super.dispose();
  }

  Future<void> _loadContacts() async {
    setState(() {
      _loadingContacts = true;
      _contactsError = null;
    });
    try {
      final contacts = await fetchContacts(ref);
      if (mounted) {
        setState(() {
          _contacts = contacts;
          _loadingContacts = false;
          if (_selectedContactId == null && contacts.isNotEmpty) {
            _selectedContactId = contacts.first.id;
          }
        });
        // Load events for the selected contact
        if (_selectedContactId != null) {
          _loadEvents(_selectedContactId!);
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _contactsError = e;
          _loadingContacts = false;
        });
      }
    }
    // Load event types
    _loadEventTypes();
  }

  Future<void> _loadEventTypes() async {
    setState(() {
      _loadingEventTypes = true;
      _eventTypesError = null;
    });
    try {
      final eventTypes = await fetchEventTypes(ref);
      if (mounted) {
        setState(() {
          _eventTypes = eventTypes;
          _loadingEventTypes = false;
          if (_selectedEventType == null && eventTypes.isNotEmpty) {
            _selectedEventType = eventTypes.first;
          }
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _eventTypesError = e;
          _loadingEventTypes = false;
        });
      }
    }
  }

  Future<void> _loadEvents(String contactId) async {
    setState(() {
      _loadingEvents = true;
      _eventsError = null;
    });
    try {
      final events = await fetchEventsByContact(ref, contactId);
      if (mounted) {
        setState(() {
          _eventsByContact = events;
          _loadingEvents = false;
          // Reset selected event when contact changes
          _selectedExistingEventId = null;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _eventsError = e;
          _loadingEvents = false;
        });
      }
    }
  }

  /// 将新联系人添加到列表开头并选中
  void _addNewContact(ContactQuickSaveResult result, String relationTypeName) {
    final newContact = ContactOption(
      id: result.contactId,
      name: result.contactName,
      relation: result.relationType ?? relationTypeName,
    );
    setState(() {
      _contacts = [newContact, ...?_contacts];
      _selectedContactId = result.contactId;
      _selectedExistingEventId = null;
    });
    _loadEvents(result.contactId);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (_loadingContacts) {
      return Scaffold(
        appBar: AppBar(title: const Text('新增记录')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_contactsError != null) {
      final message = _contactsError is ApiException ? (_contactsError as ApiException).message : '联系人加载失败';
      return Scaffold(
        appBar: AppBar(title: const Text('新增记录')),
        body: LedgerEmptyStateView(
          title: '加载失败',
          message: message,
          actionText: '重试',
          onAction: _loadContacts,
        ),
      );
    }

    final contacts = _contacts!;

    return Scaffold(
      appBar: AppBar(title: const Text('新增记录')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: [
            LedgerSectionCard(
              title: '联系人',
              trailing: TextButton.icon(
                onPressed: _showQuickAddContact,
                icon: const Icon(Icons.add, size: 18),
                label: const Text('新建'),
              ),
              child: Wrap(
                spacing: 8,
                runSpacing: 8,
                children: contacts.map((contact) {
                  return ChoiceChip(
                    label: Text('${contact.name} · ${contact.relation}'),
                    selected: _selectedContactId == contact.id,
                    onSelected: (_) {
                      if (_selectedContactId != contact.id) {
                        setState(() {
                          _selectedContactId = contact.id;
                          _selectedExistingEventId = null;
                        });
                        _loadEvents(contact.id);
                      }
                    },
                  );
                }).toList(),
              ),
            ),
            const SizedBox(height: 16),
            LedgerSectionCard(
              title: '记录类型',
              child: SegmentedButton<RecordKind>(
                showSelectedIcon: false,
                segments: const [
                  ButtonSegment(value: RecordKind.give, label: Text('随礼（我给别人）')),
                  ButtonSegment(value: RecordKind.receive, label: Text('收礼（别人给我）')),
                ],
                selected: {_selectedKind},
                onSelectionChanged: (selection) {
                  setState(() {
                    _selectedKind = selection.first;
                    // Reset selected event when kind changes
                    _selectedExistingEventId = null;
                  });
                },
              ),
            ),
            const SizedBox(height: 16),
            LedgerSectionCard(
              title: '事件',
              subtitle: _selectedKind == RecordKind.give
                  ? '随礼默认读取该联系人的联系人事件'
                  : '收礼默认读取本人的自有事件',
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SwitchListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('新建事件后保存记录'),
                    value: _createNewEvent,
                    onChanged: (value) {
                      setState(() {
                        _createNewEvent = value;
                        if (value) {
                          _selectedExistingEventId = null;
                        }
                      });
                    },
                  ),
                  const SizedBox(height: 8),
                  if (!_createNewEvent)
                    _buildEventSelector()
                  else
                    _buildNewEventForm(),
                ],
              ),
            ),
            const SizedBox(height: 16),
            LedgerSectionCard(
              title: '时间与金额',
              child: Column(
                children: [
                  InkWell(
                    onTap: _pickDate,
                    borderRadius: BorderRadius.circular(12),
                    child: InputDecorator(
                      decoration: const InputDecoration(labelText: '记录日期'),
                      child: Row(
                        children: [
                          Expanded(child: Text(LedgerFormatters.fullDate(_selectedDate))),
                          const Icon(Icons.calendar_today_outlined, size: 18),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _amountController,
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(labelText: '金额'),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return '请输入金额';
                      }
                      final amount = double.tryParse(value);
                      if (amount == null || amount <= 0) {
                        return '金额必须大于 0';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _remarkController,
                    minLines: 2,
                    maxLines: 4,
                    decoration: const InputDecoration(labelText: '备注（可选）'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            LedgerPrimaryButton(
              label: '保存记录',
              icon: Icons.check_rounded,
              isLoading: _submitting,
              onPressed: _submit,
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _pickDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
    );
    if (picked != null) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  Widget _buildEventSelector() {
    if (_selectedContactId == null) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 12),
        child: Text('请先选择联系人'),
      );
    }

    if (_loadingEvents) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 12),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_eventsError != null) {
      final message = _eventsError is ApiException ? (_eventsError as ApiException).message : '事件加载失败';
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 12),
        child: Row(
          children: [
            Expanded(child: Text(message)),
            TextButton(
              onPressed: () => _loadEvents(_selectedContactId!),
              child: const Text('重试'),
            ),
          ],
        ),
      );
    }

    if (_eventsByContact == null) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 12),
        child: Text('暂无事件数据'),
      );
    }

    final showSelfEvents = _selectedKind == RecordKind.receive;
    final eventsToShow = showSelfEvents ? _eventsByContact!.selfEventList : _eventsByContact!.contactEventList;

    if (eventsToShow.isEmpty) {
      return const Text('当前没有可选事件，请打开"新建事件后保存记录"。');
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (_eventsByContact!.selfEventList.isNotEmpty && _eventsByContact!.contactEventList.isNotEmpty) ...[
          Text(
            showSelfEvents ? '本人事件' : '联系人事件',
            style: Theme.of(context).textTheme.titleSmall,
          ),
          const SizedBox(height: 8),
        ],
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: eventsToShow.map((event) {
            return ChoiceChip(
              label: Text('${event.eventTypeName} · ${event.name}'),
              selected: _selectedExistingEventId == event.id,
              onSelected: (_) {
                setState(() {
                  _selectedExistingEventId = event.id;
                });
              },
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildNewEventForm() {
    if (_loadingEventTypes) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 12),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_eventTypesError != null) {
      final message = _eventTypesError is ApiException ? (_eventTypesError as ApiException).message : '事件类型加载失败';
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 12),
        child: Row(
          children: [
            Expanded(child: Text(message)),
            TextButton(
              onPressed: _loadEventTypes,
              child: const Text('重试'),
            ),
          ],
        ),
      );
    }

    if (_eventTypes == null || _eventTypes!.isEmpty) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 12),
        child: Text('暂无事件类型数据'),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: _eventTypes!.map((item) {
            return ChoiceChip(
              label: Text(item.name),
              selected: _selectedEventType?.id == item.id,
              onSelected: (_) {
                setState(() {
                  _selectedEventType = item;
                });
              },
            );
          }).toList(),
        ),
        const SizedBox(height: 16),
        TextFormField(
          controller: _eventNameController,
          decoration: const InputDecoration(labelText: '事件名称'),
          validator: (value) {
            if (_createNewEvent && (value == null || value.trim().isEmpty)) {
              return '请输入事件名称';
            }
            return null;
          },
        ),
      ],
    );
  }

  Future<void> _showQuickAddContact() async {
    final result = await showDialog<ContactQuickSaveResult>(
      context: context,
      builder: (context) => const QuickAddContactDialog(),
    );
    if (result != null) {
      _addNewContact(result, result.relationType ?? '');
    }
  }

  Future<void> _submit() async {
    if (_selectedContactId == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('请先选择联系人')));
      return;
    }
    if (!_formKey.currentState!.validate()) {
      return;
    }
    if (!_createNewEvent && _selectedExistingEventId == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('请选择一个已有事件，或开启新建事件')));
      return;
    }

    final amount = double.tryParse(_amountController.text.trim());
    if (amount == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('金额格式不正确')));
      return;
    }

    setState(() {
      _submitting = true;
    });

    try {
      if (_createNewEvent) {
        // 新建事件并保存记录
        await saveRecordWithEvent(
          ref,
          RecordSaveWithEventDraft(
            contactId: _selectedContactId!,
            kind: _selectedKind,
            recordDate: _selectedDate,
            amount: amount,
            recordRemark: _remarkController.text.trim(),
            eventName: _eventNameController.text.trim(),
            eventTypeId: _selectedEventType!.id,
            eventOwnerType: _selectedKind.eventOwnerType,
            ownerContactId: _selectedKind == RecordKind.give ? _selectedContactId : null,
            eventRemark: '',
          ),
        );
      } else {
        // 选择已有事件保存记录
        await saveRecord(
          ref,
          RecordSaveDraft(
            contactId: _selectedContactId!,
            kind: _selectedKind,
            recordDate: _selectedDate,
            amount: amount,
            recordRemark: _remarkController.text.trim(),
            existingEventId: _selectedExistingEventId,
            newEventName: null,
            newEventType: null,
          ),
        );
      }

      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('记录已保存')));
      context.pop();
    } on ApiException catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error.message)));
    } finally {
      if (mounted) {
        setState(() {
          _submitting = false;
        });
      }
    }
  }
}
