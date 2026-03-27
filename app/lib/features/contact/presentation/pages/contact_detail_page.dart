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

class ContactDetailPage extends ConsumerWidget {
  const ContactDetailPage({super.key, required this.contactId});

  final String contactId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(contactDetailProvider(contactId));

    return Scaffold(
      appBar: AppBar(title: const Text('联系人详情')),
      body: detailAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) {
          final message = error is ApiException ? error.message : '联系人详情加载失败';
          return LedgerEmptyStateView(
            title: '加载失败',
            message: message,
            actionText: '重试',
            onAction: () => ref.invalidate(contactDetailProvider(contactId)),
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
                    Text(detail.name, style: Theme.of(context).textTheme.headlineMedium),
                    const SizedBox(height: 8),
                    Text('${detail.relation} / ${detail.mobile.isEmpty ? '未填写手机号' : detail.mobile}', style: Theme.of(context).textTheme.bodyMedium),
                    const SizedBox(height: 8),
                    Text(detail.remark.isEmpty ? '暂无备注' : detail.remark, style: Theme.of(context).textTheme.bodyMedium),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              LedgerSectionCard(
                title: '往来汇总',
                subtitle: detail.lastRecordDate == null ? '暂无最近记录' : '最近往来：${LedgerFormatters.fullDate(detail.lastRecordDate!)}',
                child: Row(
                  children: [
                    Expanded(child: LedgerMetricTile(label: '总随礼', value: LedgerFormatters.amount(detail.totalGive))),
                    Expanded(child: LedgerMetricTile(label: '总收礼', value: LedgerFormatters.amount(detail.totalReceive))),
                    Expanded(child: LedgerMetricTile(label: '净额', value: LedgerFormatters.amount(detail.netAmount), emphasize: true)),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              Text('待处理闭环 ${detail.unclosedReciprocityCount} 项', style: Theme.of(context).textTheme.bodyMedium),
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
                ],
              ),
              const SizedBox(height: 16),
              LedgerSectionCard(
                title: '往来时间线',
                subtitle: '接口：contact/detail + record/contact-timeline',
                child: detail.timeline.isEmpty
                    ? const LedgerEmptyStateView(
                        title: '暂无往来记录',
                        message: '先新增一笔与该联系人的记录。',
                      )
                    : Column(
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
                                      Text('${item.eventTypeName} / ${item.eventName}', style: Theme.of(context).textTheme.titleMedium),
                                      const SizedBox(height: 4),
                                      Text('状态：${item.reciprocityStatus.label}', style: Theme.of(context).textTheme.bodyMedium),
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
