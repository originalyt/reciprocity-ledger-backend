import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_metric_tile.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/providers/mock_providers.dart';

class ContactDetailPage extends ConsumerWidget {
  const ContactDetailPage({super.key, required this.contactId});

  final String contactId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detail = ref.watch(contactDetailProvider(contactId));

    if (detail == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('联系人详情')),
        body: const LedgerEmptyStateView(
          title: '联系人不存在',
          message: '当前没有找到对应联系人。',
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(title: const Text('联系人详情')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          LedgerSectionCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(detail.name, style: Theme.of(context).textTheme.headlineMedium),
                const SizedBox(height: 8),
                Text('${detail.relation} / ${detail.phone}', style: Theme.of(context).textTheme.bodyMedium),
                const SizedBox(height: 8),
                Text(detail.note, style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
          ),
          const SizedBox(height: 16),
          LedgerSectionCard(
            title: '往来汇总',
            subtitle: '最近往来：${LedgerFormatters.fullDate(detail.lastInteractionOn)}',
            child: Row(
              children: [
                Expanded(
                  child: LedgerMetricTile(
                    label: '总随礼',
                    value: LedgerFormatters.amount(detail.totalGive),
                  ),
                ),
                Expanded(
                  child: LedgerMetricTile(
                    label: '总收礼',
                    value: LedgerFormatters.amount(detail.totalReceive),
                  ),
                ),
                Expanded(
                  child: LedgerMetricTile(
                    label: '净额',
                    value: LedgerFormatters.amount(detail.netAmount),
                    emphasize: true,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: () => context.push('/record/editor?kind=give&contactId=${detail.id}'),
                  child: const Text('新增随礼'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton(
                  onPressed: () => context.push('/record/editor?kind=receive&contactId=${detail.id}'),
                  child: const Text('新增收礼'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton(
                  onPressed: () => context.go('/reciprocity'),
                  child: const Text('查看回礼'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          LedgerSectionCard(
            title: '往来时间线',
            child: Column(
              children: detail.timeline.map((item) {
                return Padding(
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
                            Text('${item.eventType} / ${item.eventNote}', style: Theme.of(context).textTheme.titleMedium),
                            const SizedBox(height: 4),
                            Text(item.remark, style: Theme.of(context).textTheme.bodyMedium),
                          ],
                        ),
                      ),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text(LedgerFormatters.amount(item.amount), style: Theme.of(context).textTheme.titleMedium),
                          const SizedBox(height: 4),
                          Text(LedgerFormatters.monthDay(item.occurredOn), style: Theme.of(context).textTheme.bodyMedium),
                        ],
                      ),
                    ],
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
