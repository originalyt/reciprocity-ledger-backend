import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_metric_tile.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final overview = ref.watch(homeOverviewProvider);
    final theme = Theme.of(context);

    return SafeArea(
      child: CustomScrollView(
        slivers: [
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
            sliver: SliverList(
              delegate: SliverChildListDelegate([
                Text('礼账', style: theme.textTheme.headlineMedium),
                const SizedBox(height: 8),
                Text(
                  '今天适合把往来关系记清楚。',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 20),
                LedgerSectionCard(
                  title: '今年概览',
                  subtitle: '先看总数，再决定今天要处理什么。',
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
                  title: '待办提醒',
                  subtitle: '优先处理真正待回礼的事项。',
                  child: Wrap(
                    spacing: 12,
                    runSpacing: 12,
                    children: [
                      _PendingTile(
                        title: '我待回礼',
                        count: overview.pending.waitMeCount,
                        status: ReciprocityStatus.waitMe,
                        onTap: () {
                          ref.read(reciprocityFilterProvider.notifier).state = ReciprocityStatus.waitMe;
                          context.go('/reciprocity');
                        },
                      ),
                      _PendingTile(
                        title: '待对方回礼',
                        count: overview.pending.waitOtherCount,
                        status: ReciprocityStatus.waitOther,
                        onTap: () {
                          ref.read(reciprocityFilterProvider.notifier).state = ReciprocityStatus.waitOther;
                          context.go('/reciprocity');
                        },
                      ),
                      _PendingTile(
                        title: '无需回礼',
                        count: overview.pending.noNeedCount,
                        status: ReciprocityStatus.noNeed,
                        onTap: () {
                          ref.read(reciprocityFilterProvider.notifier).state = ReciprocityStatus.noNeed;
                          context.go('/reciprocity');
                        },
                      ),
                    ],
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
                        subtitle: '直接记一笔随礼',
                        onTap: () => context.push('/record/editor?kind=give'),
                      ),
                      _QuickActionTile(
                        icon: Icons.arrow_downward_rounded,
                        title: '新增收礼',
                        subtitle: '登记收到的礼金',
                        onTap: () => context.push('/record/editor?kind=receive'),
                      ),
                      _QuickActionTile(
                        icon: Icons.people_alt_outlined,
                        title: '联系人',
                        subtitle: '看某个人的往来',
                        onTap: () => context.push('/contacts/c1'),
                      ),
                      _QuickActionTile(
                        icon: Icons.assignment_outlined,
                        title: '回礼清单',
                        subtitle: '查看待处理事项',
                        onTap: () => context.go('/reciprocity'),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                LedgerSectionCard(
                  title: '最近往来',
                  subtitle: '只放最近几条，完整记录到时间线看。',
                  child: Column(
                    children: overview.recentRecords.map((record) {
                      return _RecentRecordTile(record: record);
                    }).toList(),
                  ),
                ),
              ]),
            ),
          ),
        ],
      ),
    );
  }
}

class _PendingTile extends StatelessWidget {
  const _PendingTile({
    required this.title,
    required this.count,
    required this.status,
    required this.onTap,
  });

  final String title;
  final int count;
  final ReciprocityStatus status;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Ink(
        width: 150,
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: Theme.of(context).cardColor,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: Theme.of(context).dividerColor),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            LedgerStatusChip.status(status: status),
            const SizedBox(height: 12),
            Text(title, style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 6),
            Text('$count 项', style: Theme.of(context).textTheme.headlineMedium),
          ],
        ),
      ),
    );
  }
}

class _QuickActionTile extends StatelessWidget {
  const _QuickActionTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Ink(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: theme.cardColor,
          borderRadius: BorderRadius.circular(18),
        ),
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
    );
  }
}

class _RecentRecordTile extends StatelessWidget {
  const _RecentRecordTile({required this.record});

  final RecentRecord record;

  @override
  Widget build(BuildContext context) {
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
                  Text(record.contactName, style: Theme.of(context).textTheme.titleMedium),
                  const SizedBox(height: 4),
                  Text(
                    '${record.eventType} / ${record.eventNote}',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  LedgerFormatters.amount(record.amount),
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 4),
                Text(
                  LedgerFormatters.monthDay(record.occurredOn),
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
