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
  EventTypeOption? _selectedEventType;
  bool _submitting = false;

  // Data
  List<ContactOption>? _contacts;
  Object? _contactsError;
  bool _loadingContacts = true;

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
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _contactsError = e;
          _loadingContacts = false;
        });
      }
    }
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
    });
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
                        });
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
                  });
                },
              ),
            ),
            const SizedBox(height: 16),
            LedgerSectionCard(
              title: '事由',
              subtitle: '选择事件类型',
              child: _buildEventTypeSelector(),
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

  Widget _buildEventTypeSelector() {
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
          decoration: const InputDecoration(
            labelText: '事件名称（可选）',
            hintText: '不填则使用事件类型名称',
          ),
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
    if (_selectedEventType == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('请选择事件类型')));
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
      // 使用简化的保存方式：自动处理事件
      await saveRecordSimple(
        ref,
        RecordSaveSimpleDraft(
          contactId: _selectedContactId!,
          kind: _selectedKind,
          recordDate: _selectedDate,
          amount: amount,
          eventTypeId: _selectedEventType!.id,
          eventName: _eventNameController.text.trim().isEmpty
              ? null
              : _eventNameController.text.trim(),
          recordRemark: _remarkController.text.trim(),
        ),
      );

      if (!mounted) {
        return;
      }

      // 触发首页数据刷新
      triggerDataRefresh(ref);

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
