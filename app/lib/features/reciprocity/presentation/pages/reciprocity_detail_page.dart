import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_section_card.dart';
import '../../../../core/widgets/ledger_status_chip.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/app_providers.dart';

class ReciprocityDetailPage extends ConsumerStatefulWidget {
  const ReciprocityDetailPage({super.key, required this.eventId});

  final String eventId;

  @override
  ConsumerState<ReciprocityDetailPage> createState() => _ReciprocityDetailPageState();
}

class _ReciprocityDetailPageState extends ConsumerState<ReciprocityDetailPage> {
  ReciprocityDetail? _detail;
  Object? _error;
  bool _isLoading = true;
  int _lastRefreshVersion = -1;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final detail = await fetchReciprocityDetail(ref, widget.eventId);
      if (mounted) {
        setState(() {
          _detail = detail;
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

  /// 显示标记为无需往来的确认对话框
  Future<void> _showMarkNoNeedDialog() async {
    final reasonController = TextEditingController();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('标记为无需往来'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('确认后将此记录标记为无需往来，后续不再提醒。'),
            const SizedBox(height: 16),
            TextField(
              controller: reasonController,
              maxLines: 2,
              decoration: const InputDecoration(
                labelText: '原因（可选）',
                hintText: '如：对方已去世、关系已断等',
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('确认'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      try {
        await markReciprocityNoNeed(ref, _detail!.record.recordId, reason: reasonController.text.trim());
        if (mounted) {
          triggerDataRefresh(ref);
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('已标记为无需往来')),
          );
          context.pop();
        }
      } on ApiException catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(e.message)),
          );
        }
      }
    }
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

    final theme = Theme.of(context);

    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(title: const Text('往来详情')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      final message = _error is ApiException ? (_error as ApiException).message : '往来详情加载失败';
      return Scaffold(
        appBar: AppBar(title: const Text('往来详情')),
        body: LedgerEmptyStateView(
          title: '加载失败',
          message: message,
          actionText: '重试',
          onAction: _loadData,
        ),
      );
    }

    final detail = _detail!;

    return Scaffold(
      appBar: AppBar(title: const Text('往来详情')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          LedgerSectionCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(detail.record.contactName, style: theme.textTheme.headlineMedium),
                const SizedBox(height: 8),
                Text('${detail.record.eventTypeName} / ${detail.record.eventName}', style: theme.textTheme.titleMedium),
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
                Text('同类型总收礼：${LedgerFormatters.amount(detail.historyReference.sameTypeReceiveAmount)}', style: theme.textTheme.bodyLarge),
                const SizedBox(height: 8),
                Text('同类型总随礼：${LedgerFormatters.amount(detail.historyReference.sameTypeSendAmount)}', style: theme.textTheme.bodyLarge),
                const SizedBox(height: 8),
                Text('待往来记录：${detail.historyReference.unclosedRecordCount} 条', style: theme.textTheme.bodyLarge),
                if (detail.historyReference.lastSameTypeRecord != null) ...[
                  const SizedBox(height: 12),
                  Text('最近同类型记录：${detail.historyReference.lastSameTypeRecord!.eventName}', style: theme.textTheme.bodyMedium),
                ],
              ],
            ),
          ),
          if (detail.manualInfo != null) ...[
            const SizedBox(height: 16),
            LedgerSectionCard(
              title: '手动往来信息',
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('匹配类型：${detail.manualInfo!.matchType}', style: theme.textTheme.bodyLarge),
                  const SizedBox(height: 8),
                  Text('匹配状态：${detail.manualInfo!.matchStatus}', style: theme.textTheme.bodyLarge),
                  if ((detail.manualInfo!.remark ?? '').isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Text('备注：${detail.manualInfo!.remark}', style: theme.textTheme.bodyMedium),
                  ],
                ],
              ),
            ),
          ],
          const SizedBox(height: 16),
          // 根据记录类型显示对应的操作按钮
          // 如果是随礼（我给别人），等待对方回礼，显示"新增收礼"
          // 如果是收礼（别人给我），我需要回礼，显示"新增随礼"
          if (detail.record.kind == RecordKind.give)
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: () => context.push('/record/editor?kind=receive&contactId=${detail.record.contactId}'),
                icon: const Icon(Icons.arrow_downward_rounded),
                label: const Text('新增收礼（对方回礼）'),
              ),
            )
          else
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: () => context.push('/record/editor?kind=give&contactId=${detail.record.contactId}'),
                icon: const Icon(Icons.arrow_upward_rounded),
                label: const Text('新增随礼（我回礼）'),
              ),
            ),
          // 只有待往来状态才显示"无需往来"按钮
          if (detail.record.reciprocityStatus == ReciprocityStatus.unmatched) ...[
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: TextButton.icon(
                onPressed: _showMarkNoNeedDialog,
                icon: const Icon(Icons.check_circle_outline),
                label: const Text('标记为无需往来'),
              ),
            ),
          ],
        ],
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
    final theme = Theme.of(context);
    return LedgerSectionCard(
      title: title,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          LedgerStatusChip.recordKind(kind: record.kind),
          const SizedBox(height: 12),
          Text(LedgerFormatters.amount(record.amount), style: theme.textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(LedgerFormatters.fullDate(record.recordDate), style: theme.textTheme.bodyMedium),
          const SizedBox(height: 8),
          Text(record.remark.isEmpty ? '暂无备注' : record.remark, style: theme.textTheme.bodyMedium),
        ],
      ),
    );
  }
}
