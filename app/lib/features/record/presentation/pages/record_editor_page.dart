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
  final _amountController = TextEditingController();
  final _remarkController = TextEditingController();

  late RecordKind _selectedKind;
  late DateTime _selectedDate;
  String? _selectedContactId;
  bool _submitting = false;

  // Data
  List<ContactOption>? _contacts;
  Object? _contactsError;
  bool _loadingContacts = true;

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
                      setState(() {
                        _selectedContactId = contact.id;
                      });
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

    final amount = double.tryParse(_amountController.text.trim());
    if (amount == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('金额格式不正确')));
      return;
    }

    setState(() {
      _submitting = true;
    });

    try {
      await saveRecord(
        ref,
        RecordSaveDraft(
          contactId: _selectedContactId!,
          kind: _selectedKind,
          recordDate: _selectedDate,
          amount: amount,
          recordRemark: _remarkController.text.trim(),
          // 不关联事件
          existingEventId: null,
          newEventName: null,
          newEventType: null,
        ),
      );

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
