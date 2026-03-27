import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class ReciprocityPage extends ConsumerWidget {
  const ReciprocityPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selected = ref.watch(reciprocityFilterProvider);
    final listAsync = ref.watch(reciprocityListProvider);

    return SafeArea(
      child: listAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) {
          final message = error is ApiException ? error.message : '闭环列表加载失败';
          return LedgerEmptyStateView(
            title: '加载失败',
            message: message,
            actionText: '重试',
            onAction: () => ref.invalidate(reciprocityListProvider),
          );
        },
        data: (items) {
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
            children: [
              Text('闭环列表', style: Theme.of(context).textTheme.headlineMedium),
              const SizedBox(height: 8),
              Text('当前页面按后端真实闭环状态展示记录。', style: Theme.of(context).textTheme.bodyMedium),
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
                  ref.read(reciprocityFilterProvider.notifier).state = selection.first;
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
                          return InkWell(
                            onTap: () => context.push('/reciprocity/${item.recordId}'),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(vertical: 10),
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  LedgerStatusChip.status(status: item.status),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(item.contactName, style: Theme.of(context).textTheme.titleMedium),
                                        const SizedBox(height: 4),
                                        Text('${item.eventTypeName} / ${item.eventName}', style: Theme.of(context).textTheme.bodyMedium),
                                        const SizedBox(height: 4),
                                        Text('方向：${item.kind.label}', style: Theme.of(context).textTheme.bodyMedium),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(width: 12),
                                  Column(
                                    crossAxisAlignment: CrossAxisAlignment.end,
                                    children: [
                                      Text(LedgerFormatters.amount(item.amount), style: Theme.of(context).textTheme.titleMedium),
                                      const SizedBox(height: 4),
                                      Text(LedgerFormatters.monthDay(item.recordDate), style: Theme.of(context).textTheme.bodyMedium),
                                      if (item.matchedAmount != null) ...[
                                        const SizedBox(height: 4),
                                        Text('匹配 ${LedgerFormatters.amount(item.matchedAmount!)}', style: Theme.of(context).textTheme.bodyMedium),
                                      ],
                                    ],
                                  ),
                                ],
                              ),
                            ),
                          );
                        }).toList(),
                      ),
              ),
            ],
          );
        },
      ),
    );
  }
}
