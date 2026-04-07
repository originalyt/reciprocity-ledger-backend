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
import '../../../../shared/providers/app_providers.dart';

class TimelinePage extends ConsumerStatefulWidget {
  const TimelinePage({super.key});

  @override
  ConsumerState<TimelinePage> createState() => _TimelinePageState();
}

class _TimelinePageState extends ConsumerState<TimelinePage> {
  TimelineBundle? _timeline;
  Object? _error;
  bool _isLoading = true;
  int _lastRefreshVersion = -1;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    final filter = ref.read(timelineFilterProvider);
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final timeline = await fetchSelfTimeline(ref, kind: filter);
      if (mounted) {
        setState(() {
          _timeline = timeline;
          _isLoading = false;
          _lastRefreshVersion = ref.read(dataRefreshProvider);
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

  void _onFilterChanged(RecordKind? newFilter) {
    ref.read(timelineFilterProvider.notifier).setFilter(newFilter);
    _loadData();
  }

  @override
  Widget build(BuildContext context) {
    // 监听数据刷新通知
    final refreshVersion = ref.watch(dataRefreshProvider);
    if (refreshVersion != _lastRefreshVersion) {
      _lastRefreshVersion = refreshVersion;
      Future.microtask(() {
        if (mounted && !_isLoading) {
          _loadData();
        }
      });
    }

    final filter = ref.watch(timelineFilterProvider);
    final theme = Theme.of(context);

    if (_isLoading) {
      return const SafeArea(
        child: Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      final message = _error is ApiException ? (_error as ApiException).message : '时间线加载失败';
      return SafeArea(
        child: LedgerEmptyStateView(
          title: '加载失败',
          message: message,
          actionText: '重试',
          onAction: _loadData,
        ),
      );
    }

    final timeline = _timeline!;

    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 120),
        children: [
          Text('时间线', style: theme.textTheme.headlineMedium),
          const SizedBox(height: 8),
          Text('数据来自 /app/record/self-timeline', style: theme.textTheme.bodyMedium),
          const SizedBox(height: 20),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                ChoiceChip(
                  label: const Text('全部'),
                  selected: filter == null,
                  onSelected: (_) => _onFilterChanged(null),
                ),
                const SizedBox(width: 8),
                ChoiceChip(
                  label: const Text('随礼'),
                  selected: filter == RecordKind.give,
                  onSelected: (_) => _onFilterChanged(RecordKind.give),
                ),
                const SizedBox(width: 8),
                ChoiceChip(
                  label: const Text('收礼'),
                  selected: filter == RecordKind.receive,
                  onSelected: (_) => _onFilterChanged(RecordKind.receive),
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
                                    Text(item.contactName, style: theme.textTheme.titleMedium),
                                    const SizedBox(height: 4),
                                    Text('${item.eventTypeName} / ${item.eventName}', style: theme.textTheme.bodyMedium),
                                  ],
                                ),
                              ),
                              const SizedBox(width: 12),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.end,
                                children: [
                                  Text(LedgerFormatters.amount(item.amount), style: theme.textTheme.titleMedium),
                                  const SizedBox(height: 4),
                                  Text(LedgerFormatters.monthDay(item.recordDate), style: theme.textTheme.bodyMedium),
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
