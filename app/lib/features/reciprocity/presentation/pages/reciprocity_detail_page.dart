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

class ReciprocityDetailPage extends ConsumerWidget {
  const ReciprocityDetailPage({super.key, required this.eventId});

  final String eventId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(reciprocityDetailProvider(eventId));

    return Scaffold(
      appBar: AppBar(title: const Text('闭环详情')),
      body: detailAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) {
          final message = error is ApiException ? error.message : '闭环详情加载失败';
          return LedgerEmptyStateView(
            title: '加载失败',
            message: message,
            actionText: '重试',
            onAction: () => ref.invalidate(reciprocityDetailProvider(eventId)),
          );
        },
        data: (detail) {
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
            children: [
              LedgerSectionCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(detail.record.contactName, style: Theme.of(context).textTheme.headlineMedium),
                    const SizedBox(height: 8),
                    Text('${detail.record.eventTypeName} / ${detail.record.eventName}', style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 8),
                    LedgerStatusChip.status(status: detail.record.reciprocityStatus),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              _RecordBlock(title: '当前记录', record: detail.record),
              const SizedBox(height: 12),
              if (detail.matchedRecord != null) _RecordBlock(title: '匹配记录', record: detail.matchedRecord!),
              const SizedBox(height: 16),
              LedgerSectionCard(
                title: '历史参考',
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('同类型总收礼：${LedgerFormatters.amount(detail.historyReference.sameTypeReceiveAmount)}', style: Theme.of(context).textTheme.bodyLarge),
                    const SizedBox(height: 8),
                    Text('同类型总随礼：${LedgerFormatters.amount(detail.historyReference.sameTypeSendAmount)}', style: Theme.of(context).textTheme.bodyLarge),
                    const SizedBox(height: 8),
                    Text('未闭环记录：${detail.historyReference.unclosedRecordCount} 条', style: Theme.of(context).textTheme.bodyLarge),
                    if (detail.historyReference.lastSameTypeRecord != null) ...[
                      const SizedBox(height: 12),
                      Text('最近同类型记录：${detail.historyReference.lastSameTypeRecord!.eventName}', style: Theme.of(context).textTheme.bodyMedium),
                    ],
                  ],
                ),
              ),
              if (detail.manualInfo != null) ...[
                const SizedBox(height: 16),
                LedgerSectionCard(
                  title: '人工闭环信息',
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('匹配类型：${detail.manualInfo!.matchType}', style: Theme.of(context).textTheme.bodyLarge),
                      const SizedBox(height: 8),
                      Text('匹配状态：${detail.manualInfo!.matchStatus}', style: Theme.of(context).textTheme.bodyLarge),
                      if ((detail.manualInfo!.remark ?? '').isNotEmpty) ...[
                        const SizedBox(height: 8),
                        Text('备注：${detail.manualInfo!.remark}', style: Theme.of(context).textTheme.bodyMedium),
                      ],
                    ],
                  ),
                ),
              ],
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => context.push('/record/editor?kind=receive&contactId=${detail.record.contactId}'),
                      child: const Text('新增收礼'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => context.push('/record/editor?kind=give&contactId=${detail.record.contactId}'),
                      child: const Text('新增随礼'),
                    ),
                  ),
                ],
              ),
            ],
          );
        },
      ),
    );
  }
}

class _RecordBlock extends StatelessWidget {
  const _RecordBlock({required this.title, required this.record});

  final String title;
  final RecordInfo record;

  @override
  Widget build(BuildContext context) {
    return LedgerSectionCard(
      title: title,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          LedgerStatusChip.recordKind(kind: record.kind),
          const SizedBox(height: 12),
          Text(LedgerFormatters.amount(record.amount), style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(LedgerFormatters.fullDate(record.recordDate), style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 8),
          Text(record.remark.isEmpty ? '暂无备注' : record.remark, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}
