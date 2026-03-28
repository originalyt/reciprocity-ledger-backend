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

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final overviewAsync = ref.watch(homeOverviewProvider);
    final theme = Theme.of(context);

    return SafeArea(
      child: overviewAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) {
          final message = error is ApiException ? error.message : '首页加载失败';
          return LedgerEmptyStateView(
            title: '加载失败',
            message: message,
            actionText: '重试',
            onAction: () => ref.invalidate(homeOverviewProvider),
          );
        },
        data: (overview) {
          final hasRecentRecord = overview.recentRecords.isNotEmpty;
          final recentContactId = hasRecentRecord ? overview.recentRecords.first.contactId : null;

          return CustomScrollView(
            slivers: [
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    Text('礼账', style: theme.textTheme.headlineMedium),
                    const SizedBox(height: 8),
                    Text('当前页面已经接入真实首页接口。', style: theme.textTheme.bodyMedium),
                    const SizedBox(height: 20),
                    LedgerSectionCard(
                      title: '首页概览',
                      subtitle: '数据来自 /app/home/overview',
                      child: Row(
                        children: [
                          Expanded(
                            child: LedgerMetricTile(
                              label: '总随礼',
                              value: LedgerFormatters.amount(overview.totalGive),
                            ),
                          ),
                          Expanded(
                            child: LedgerMetricTile(
                              label: '总收礼',
                              value: LedgerFormatters.amount(overview.totalReceive),
                            ),
                          ),
                          Expanded(
                            child: LedgerMetricTile(
                              label: '净额',
                              value: LedgerFormatters.amount(overview.netAmount),
                              emphasize: true,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                    LedgerSectionCard(
                      title: '待处理闭环',
                      subtitle: '当前后端首页只返回待处理总数。',
                      child: InkWell(
                        borderRadius: BorderRadius.circular(18),
                        onTap: () {
                          ref.read(reciprocityFilterProvider.notifier).state = ReciprocityStatus.unmatched;
                          context.go('/reciprocity');
                        },
                        child: Ink(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: Theme.of(context).cardColor,
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(color: Theme.of(context).dividerColor),
                          ),
                          child: Row(
                            children: [
                              const LedgerStatusChip.status(status: ReciprocityStatus.unmatched),
                              const Spacer(),
                              Text(
                                '${overview.pendingReciprocityCount} 项',
                                style: theme.textTheme.headlineMedium,
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    LedgerSectionCard(
                      title: '快捷入口',
                      child: GridView.count(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        crossAxisCount: 2,
                        crossAxisSpacing: 12,
                        mainAxisSpacing: 12,
                        childAspectRatio: 1.45,
                        children: [
                          _QuickActionTile(
                            icon: Icons.arrow_upward_rounded,
                            title: '新增随礼',
                            subtitle: '保存到真实接口',
                            onTap: () => context.push('/record/editor?kind=give'),
                          ),
                          _QuickActionTile(
                            icon: Icons.arrow_downward_rounded,
                            title: '新增收礼',
                            subtitle: '保存到真实接口',
                            onTap: () => context.push('/record/editor?kind=receive'),
                          ),
                          _QuickActionTile(
                            icon: Icons.people_alt_outlined,
                            title: '联系人详情',
                            subtitle: hasRecentRecord ? '查看联系人往来' : '暂无最近联系人',
                            onTap: recentContactId == null ? null : () => context.push('/contacts/$recentContactId'),
                          ),
                          _QuickActionTile(
                            icon: Icons.assignment_outlined,
                            title: '闭环记录',
                            subtitle: '查看闭环状态列表',
                            onTap: () => context.go('/reciprocity'),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                    LedgerSectionCard(
                      title: '最近记录',
                      subtitle: '数据来自首页返回的 recentRecordList',
                      child: overview.recentRecords.isEmpty
                          ? const LedgerEmptyStateView(
                              title: '还没有记录',
                              message: '先新增一笔记录，首页就会展示最近往来。',
                            )
                          : Column(
                              children: overview.recentRecords.map((record) {
                                return InkWell(
                                  onTap: () => context.push('/contacts/${record.contactId}'),
                                  borderRadius: BorderRadius.circular(16),
                                  child: Padding(
                                    padding: const EdgeInsets.symmetric(vertical: 10),
                                    child: Row(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        LedgerStatusChip.recordKind(kind: record.kind),
                                        const SizedBox(width: 12),
                                        Expanded(
                                          child: Column(
                                            crossAxisAlignment: CrossAxisAlignment.start,
                                            children: [
                                              Text(record.contactName, style: theme.textTheme.titleMedium),
                                              const SizedBox(height: 4),
                                              Text('${record.eventTypeName} / ${record.eventName}', style: theme.textTheme.bodyMedium),
                                            ],
                                          ),
                                        ),
                                        const SizedBox(width: 12),
                                        Column(
                                          crossAxisAlignment: CrossAxisAlignment.end,
                                          children: [
                                            Text(LedgerFormatters.amount(record.amount), style: theme.textTheme.titleMedium),
                                            const SizedBox(height: 4),
                                            Text(LedgerFormatters.monthDay(record.recordDate), style: theme.textTheme.bodyMedium),
                                          ],
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              }).toList(),
                            ),
                    ),
                  ]),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _QuickActionTile extends StatelessWidget {
  const _QuickActionTile({required this.icon, required this.title, required this.subtitle, required this.onTap});

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final disabled = onTap == null;

    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Ink(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: theme.cardColor,
          borderRadius: BorderRadius.circular(18),
        ),
        child: Opacity(
          opacity: disabled ? 0.55 : 1,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(icon, color: theme.colorScheme.primary),
              const Spacer(),
              Text(title, style: theme.textTheme.titleMedium),
              const SizedBox(height: 6),
              Text(subtitle, style: theme.textTheme.bodyMedium),
            ],
          ),
        ),
      ),
    );
  }
}
