import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class ReciprocityPage extends ConsumerWidget {
  const ReciprocityPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selected = ref.watch(reciprocityFilterProvider);
    final events = ref.watch(filteredReciprocityEventsProvider);

    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
        children: [
          Text('回礼清单', style: Theme.of(context).textTheme.headlineMedium),
          const SizedBox(height: 8),
          Text('先把真实待办和无需处理的事项分开看。', style: Theme.of(context).textTheme.bodyMedium),
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
          Row(
            children: [
              ChoiceChip(label: const Text('今年'), selected: true, onSelected: (_) {}),
              const SizedBox(width: 8),
              ChoiceChip(label: const Text('全部关系'), selected: false, onSelected: (_) {}),
              const SizedBox(width: 8),
              ChoiceChip(label: const Text('按最近日期'), selected: true, onSelected: (_) {}),
            ],
          ),
          const SizedBox(height: 16),
          LedgerSectionCard(
            title: '当前分组',
            subtitle: '点击某条记录进入事件详情。',
            child: events.isEmpty
                ? const LedgerEmptyStateView(
                    title: '当前没有记录',
                    message: '这个状态下暂时没有要展示的事项。',
                  )
                : Column(
                    children: events.map((item) {
                      return InkWell(
                        onTap: () => context.push('/reciprocity/${item.id}'),
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 10),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(item.contactName, style: Theme.of(context).textTheme.titleMedium),
                                    const SizedBox(height: 4),
                                    Text(
                                      '${item.eventType} / ${item.eventNote}',
                                      style: Theme.of(context).textTheme.bodyMedium,
                                    ),
                                    const SizedBox(height: 8),
                                    LedgerStatusChip.status(status: item.status),
                                  ],
                                ),
                              ),
                              const SizedBox(width: 12),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.end,
                                children: [
                                  Text(
                                    LedgerFormatters.amount(item.netAmount),
                                    style: Theme.of(context).textTheme.titleMedium,
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    LedgerFormatters.monthDay(item.latestDate),
                                    style: Theme.of(context).textTheme.bodyMedium,
                                  ),
                                  const SizedBox(height: 4),
                                  Text(item.relation, style: Theme.of(context).textTheme.bodyMedium),
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
      ),
    );
  }
}
