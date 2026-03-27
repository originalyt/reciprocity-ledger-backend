import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_primary_button.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

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

  @override
  void initState() {
    super.initState();
    _selectedKind = widget.initialKind ?? RecordKind.give;
    _selectedDate = DateTime.now();
    _selectedContactId = widget.initialContactId;
  }

  @override
  void dispose() {
    _eventNameController.dispose();
    _amountController.dispose();
    _remarkController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final contactsAsync = ref.watch(contactsProvider);
    final eventTypesAsync = ref.watch(eventTypesProvider);
    final eventOptionsAsync = ref.watch(eventOptionsProvider((kind: _selectedKind, contactId: _selectedContactId)));

    return Scaffold(
      appBar: AppBar(title: const Text('新增记录')),
      body: contactsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) {
          final message = error is ApiException ? error.message : '联系人加载失败';
          return LedgerEmptyStateView(
            title: '加载失败',
            message: message,
            actionText: '重试',
            onAction: () => ref.invalidate(contactsProvider),
          );
        },
        data: (contacts) {
          if (_selectedContactId == null && contacts.isNotEmpty) {
            _selectedContactId = contacts.first.id;
          }

          return Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
              children: [
                LedgerSectionCard(
                  title: '联系人',
                  subtitle: '接口：/app/contact/page',
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
                            _selectedExistingEventId = null;
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
                        eventOptionsAsync.when(
                          loading: () => const Padding(
                            padding: EdgeInsets.symmetric(vertical: 12),
                            child: CircularProgressIndicator(),
                          ),
                          error: (error, stack) {
                            final message = error is ApiException ? error.message : '事件加载失败';
                            return Text(message, style: Theme.of(context).textTheme.bodyMedium);
                          },
                          data: (events) {
                            if (events.isEmpty) {
                              return const Text('当前没有可选事件，请打开“新建事件后保存记录”。');
                            }
                            return Wrap(
                              spacing: 8,
                              runSpacing: 8,
                              children: events.map((event) {
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
                            );
                          },
                        ),
                      if (_createNewEvent) ...[
                        eventTypesAsync.when(
                          loading: () => const Padding(
                            padding: EdgeInsets.symmetric(vertical: 12),
                            child: CircularProgressIndicator(),
                          ),
                          error: (error, stack) {
                            final message = error is ApiException ? error.message : '事件类型加载失败';
                            return Text(message, style: Theme.of(context).textTheme.bodyMedium);
                          },
                          data: (eventTypes) {
                            if (_selectedEventType == null && eventTypes.isNotEmpty) {
                              _selectedEventType = eventTypes.first;
                            }
                            return Wrap(
                              spacing: 8,
                              runSpacing: 8,
                              children: eventTypes.map((item) {
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
                            );
                          },
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
                        decoration: const InputDecoration(labelText: '记录备注'),
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
          );
        },
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
      final repository = ref.read(ledgerRepositoryProvider);
      await repository.saveRecord(
        RecordSaveDraft(
          contactId: _selectedContactId!,
          kind: _selectedKind,
          recordDate: _selectedDate,
          amount: amount,
          recordRemark: _remarkController.text.trim(),
          existingEventId: _createNewEvent ? null : _selectedExistingEventId,
          newEventName: _createNewEvent ? _eventNameController.text.trim() : null,
          newEventType: _createNewEvent ? _selectedEventType : null,
        ),
      );

      if (!mounted) {
        return;
      }

      ref.invalidate(homeOverviewProvider);
      ref.invalidate(selfTimelineProvider);
      ref.invalidate(reciprocityListProvider);
      if (_selectedContactId != null) {
        ref.invalidate(contactDetailProvider(_selectedContactId!));
      }

      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('记录已保存')));
      Navigator.of(context).pop();
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
