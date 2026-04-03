import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/app_providers.dart';

class ReciprocityPage extends ConsumerStatefulWidget {
  const ReciprocityPage({super.key});

  @override
  ConsumerState<ReciprocityPage> createState() => _ReciprocityPageState();
}

class _ReciprocityPageState extends ConsumerState<ReciprocityPage> {
  List<ReciprocityEventSummary>? _items;
  Object? _error;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    final status = ref.read(reciprocityFilterProvider);
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final items = await fetchReciprocityList(ref, status);
      if (mounted) {
        setState(() {
          _items = items;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e;
          _isLoading = false;
        });
      }
    }
  }

  void _onStatusChanged(ReciprocityStatus newStatus) {
    ref.read(reciprocityFilterProvider.notifier).setStatus(newStatus);
    _loadData();
  }

  @override
  Widget build(BuildContext context) {
    final selected = ref.watch(reciprocityFilterProvider);
    final theme = Theme.of(context);

    if (_isLoading) {
      return const SafeArea(
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      final message = _error is ApiException ? (_error as ApiException).message : '闭环列表加载失败';
      return SafeArea(
        child: LedgerEmptyStateView(
          title: '加载失败',
          message: message,
          actionText: '重试',
          onAction: _loadData,
        ),
      );
    }

    final items = _items!;

    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
        children: [
          Text('闭环列表', style: theme.textTheme.headlineMedium),
          const SizedBox(height: 8),
          Text('当前页面按后端真实闭环状态展示记录。', style: theme.textTheme.bodyMedium),
          const SizedBox(height: 20),
          SegmentedButton<ReciprocityStatus>(
            multiSelectionEnabled: false,
            showSelectedIcon: false,
            segments: ReciprocityStatus.values.map((status) {
              return ButtonSegment<ReciprocityStatus>(
                value: status,
                label: Text(status.label),
              );
            }).toList(),
            selected: {selected},
            onSelectionChanged: (selection) {
              _onStatusChanged(selection.first);
            },
          ),
          const SizedBox(height: 16),
          LedgerSectionCard(
            title: '记录列表',
            subtitle: '接口：/app/reciprocity/page',
            child: items.isEmpty
                ? const LedgerEmptyStateView(
                    title: '当前状态没有记录',
                    message: '切换其它状态看看，或者先新增人情记录。',
                  )
                : Column(
                    children: items.map((item) {
                      final selfRecord = item.selfRecord;
                      final contactRecord = item.contactRecord;
                      return InkWell(
                        onTap: () => context.push('/reciprocity/${item.matchId}'),
                        child: Container(
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          decoration: BoxDecoration(
                            border: Border(
                              bottom: BorderSide(color: theme.dividerColor.withValues(alpha: 0.3)),
                            ),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // 标题行：事件类型 + 状态
                              Row(
                                children: [
                                  Text(item.eventTypeName, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                                  const SizedBox(width: 8),
                                  LedgerStatusChip.status(status: item.status),
                                ],
                              ),
                              const SizedBox(height: 8),
                              // 联系人
                              Padding(
                                padding: const EdgeInsets.only(left: 8),
                                child: Text('联系人：${item.contactName}', style: theme.textTheme.bodyMedium),
                              ),
                              const SizedBox(height: 8),
                              // 联系人事件（我随礼）
                              if (contactRecord != null)
                                Padding(
                                  padding: const EdgeInsets.only(left: 16),
                                  child: Row(
                                    children: [
                                      const Text('├─ ', style: TextStyle(fontFamily: 'monospace')),
                                      Expanded(
                                        child: Text(
                                          '${contactRecord.eventName}：我随礼 ${LedgerFormatters.amount(contactRecord.amount)} (${LedgerFormatters.monthDay(contactRecord.recordDate)})',
                                          style: theme.textTheme.bodyMedium,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              // 我方事件（对方回礼）
                              if (selfRecord != null)
                                Padding(
                                  padding: const EdgeInsets.only(left: 16),
                                  child: Row(
                                    children: [
                                      const Text('└─ ', style: TextStyle(fontFamily: 'monospace')),
                                      Expanded(
                                        child: Text(
                                          '${selfRecord.eventName}：${item.contactName}回礼 ${LedgerFormatters.amount(selfRecord.amount)} (${LedgerFormatters.monthDay(selfRecord.recordDate)})',
                                          style: theme.textTheme.bodyMedium,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                            ],
                          ),
                        ),
                      );
                    }).toList(),
                  ),
          ),
        ],
      ),
    );
  }
}
