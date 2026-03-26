import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class ReciprocityDetailPage extends ConsumerWidget {
  const ReciprocityDetailPage({super.key, required this.eventId});

  final String eventId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detail = ref.watch(reciprocityDetailProvider(eventId));

    if (detail == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('事件详情')),
        body: const LedgerEmptyStateView(
          title: '事件不存在',
          message: '当前没有找到对应的回礼事件。',
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(title: const Text('事件详情')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          LedgerSectionCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(detail.contactName, style: Theme.of(context).textTheme.headlineMedium),
                const SizedBox(height: 8),
                Text('${detail.eventType} / ${detail.eventNote}', style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 8),
                Text('最近日期：${LedgerFormatters.fullDate(detail.latestDate)}', style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
          ),
          const SizedBox(height: 16),
          LedgerSectionCard(
            title: '当前状态',
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                LedgerStatusChip.status(status: detail.status),
                const SizedBox(height: 12),
                Text('净额：${LedgerFormatters.amount(detail.netAmount)}', style: Theme.of(context).textTheme.titleLarge),
                if (detail.exemptReason != null) ...[
                  const SizedBox(height: 8),
                  Text('说明：${detail.exemptReason}', style: Theme.of(context).textTheme.bodyMedium),
                ],
              ],
            ),
          ),
          const SizedBox(height: 16),
          _RecordBlock(record: detail.receiveRecord),
          const SizedBox(height: 12),
          _RecordBlock(record: detail.giveRecord),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: () => context.push('/record/editor?kind=receive&contactId=${detail.contactId}'),
                  child: const Text('新增收礼'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton(
                  onPressed: () => context.push('/record/editor?kind=give&contactId=${detail.contactId}'),
                  child: const Text('新增随礼'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          FilledButton.tonal(
            onPressed: () {
              final message = detail.status == ReciprocityStatus.noNeed ? '这里后续接取消无需回礼接口。' : '这里后续接设为无需回礼接口。';
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
            },
            child: Text(detail.status == ReciprocityStatus.noNeed ? '取消无需回礼' : '设为无需回礼'),
          ),
        ],
      ),
    );
  }
}

class _RecordBlock extends StatelessWidget {
  const _RecordBlock({required this.record});

  final ReciprocityRecord? record;

  @override
  Widget build(BuildContext context) {
    if (record == null) {
      return const SizedBox.shrink();
    }

    return LedgerSectionCard(
      title: record!.label,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(LedgerFormatters.amount(record!.amount), style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(
            record!.occurredOn == null ? '暂无记录' : LedgerFormatters.fullDate(record!.occurredOn!),
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 8),
          Text(record!.remark, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}
