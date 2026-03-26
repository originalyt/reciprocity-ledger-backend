import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_primary_button.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class RecordEditorPage extends ConsumerStatefulWidget {
  const RecordEditorPage({
    super.key,
    this.initialKind,
    this.initialContactId,
  });

  final RecordKind? initialKind;
  final String? initialContactId;

  @override
  ConsumerState<RecordEditorPage> createState() => _RecordEditorPageState();
}

class _RecordEditorPageState extends ConsumerState<RecordEditorPage> {
  final _formKey = GlobalKey<FormState>();
  final _eventNoteController = TextEditingController();
  final _amountController = TextEditingController();
  final _remarkController = TextEditingController();

  late RecordKind _selectedKind;
  late DateTime _selectedDate;
  String? _selectedContactId;
  String? _selectedEventType;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _selectedKind = widget.initialKind ?? RecordKind.give;
    _selectedDate = DateTime.now();
    _selectedContactId = widget.initialContactId;
    _selectedEventType = '婚礼';
  }

  @override
  void dispose() {
    _eventNoteController.dispose();
    _amountController.dispose();
    _remarkController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final contacts = ref.watch(recentContactsProvider);
    final eventTypes = ref.watch(commonEventTypesProvider);
    final selectedContact = contacts.cast<ContactOption?>().firstWhere(
          (item) => item?.id == _selectedContactId,
          orElse: () => null,
        );

    return Scaffold(
      appBar: AppBar(title: const Text('新增记录')),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
            children: [
              LedgerSectionCard(
                title: '联系人',
                subtitle: '最近联系人直接点选，后续再接完整联系人选择页。',
                child: Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: contacts.map((contact) {
                    final selected = contact.id == _selectedContactId;
                    return ChoiceChip(
                      label: Text('${contact.name} · ${contact.relation}'),
                      selected: selected,
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
                    ButtonSegment(value: RecordKind.give, label: Text('随礼')),
                    ButtonSegment(value: RecordKind.receive, label: Text('收礼')),
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
                title: '事件信息',
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: eventTypes.map((eventType) {
                        return ChoiceChip(
                          label: Text(eventType),
                          selected: _selectedEventType == eventType,
                          onSelected: (_) {
                            setState(() {
                              _selectedEventType = eventType;
                            });
                          },
                        );
                      }).toList(),
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _eventNoteController,
                      decoration: const InputDecoration(labelText: '事件说明'),
                    ),
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
                        decoration: const InputDecoration(labelText: '发生日期'),
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
                      decoration: const InputDecoration(labelText: '备注'),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              if (selectedContact != null)
                Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Text(
                    '当前联系人：${selectedContact.name} · ${selectedContact.relation}',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ),
              LedgerPrimaryButton(
                label: '保存记录',
                icon: Icons.check_rounded,
                isLoading: _submitting,
                onPressed: _submit,
              ),
            ],
          ),
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

  Future<void> _submit() async {
    if (_selectedContactId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请先选择联系人')),
      );
      return;
    }

    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _submitting = true;
    });

    await Future<void>.delayed(const Duration(milliseconds: 600));

    if (!mounted) {
      return;
    }

    setState(() {
      _submitting = false;
    });

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('${_selectedKind.label}记录已保存，后续这里接后端 /app/record/save'),
      ),
    );

    Navigator.of(context).pop();
  }
}
