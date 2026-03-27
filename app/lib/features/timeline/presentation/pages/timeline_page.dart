import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_metric_tile.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class TimelinePage extends ConsumerWidget {
  const TimelinePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final filter = ref.watch(timelineFilterProvider);
    final timelineAsync = ref.watch(selfTimelineProvider);

    return SafeArea(
      child: timelineAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) {
          final message = error is ApiException ? error.message : '时间线加载失败';
          return LedgerEmptyStateView(
            title: '加载失败',
            message: message,
            actionText: '重试',
            onAction: () => ref.invalidate(selfTimelineProvider),
          );
        },
        data: (timeline) {
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
            children: [
              Text('时间线', style: Theme.of(context).textTheme.headlineMedium),
              const SizedBox(height: 8),
              Text('数据来自 /app/record/self-timeline', style: Theme.of(context).textTheme.bodyMedium),
              const SizedBox(height: 20),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    ChoiceChip(
                      label: const Text('全部'),
                      selected: filter == null,
                      onSelected: (_) => ref.read(timelineFilterProvider.notifier).state = null,
                    ),
                    const SizedBox(width: 8),
                    ChoiceChip(
                      label: const Text('随礼'),
                      selected: filter == RecordKind.give,
                      onSelected: (_) => ref.read(timelineFilterProvider.notifier).state = RecordKind.give,
                    ),
                    const SizedBox(width: 8),
                    ChoiceChip(
                      label: const Text('收礼'),
                      selected: filter == RecordKind.receive,
                      onSelected: (_) => ref.read(timelineFilterProvider.notifier).state = RecordKind.receive,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              LedgerSectionCard(
                title: '当前筛选摘要',
                child: Row(
                  children: [
                    Expanded(
                      child: LedgerMetricTile(
                        label: '总随礼',
                        value: LedgerFormatters.amount(timeline.summary.totalGive),
                      ),
                    ),
                    Expanded(
                      child: LedgerMetricTile(
                        label: '总收礼',
                        value: LedgerFormatters.amount(timeline.summary.totalReceive),
                      ),
                    ),
                    Expanded(
                      child: LedgerMetricTile(
                        label: '净额',
                        value: LedgerFormatters.amount(timeline.summary.netAmount),
                        emphasize: true,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              LedgerSectionCard(
                title: '记录列表',
                subtitle: '当前共 ${timeline.summary.recordCount} 条',
                child: timeline.entries.isEmpty
                    ? const LedgerEmptyStateView(
                        title: '暂无记录',
                        message: '当前筛选下还没有数据。',
                      )
                    : Column(
                        children: timeline.entries.map((item) {
                          return InkWell(
                            onTap: () => context.push('/contacts/${item.contactId}'),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(vertical: 10),
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  LedgerStatusChip.recordKind(kind: item.kind),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(item.contactName, style: Theme.of(context).textTheme.titleMedium),
                                        const SizedBox(height: 4),
                                        Text('${item.eventTypeName} / ${item.eventName}', style: Theme.of(context).textTheme.bodyMedium),
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
