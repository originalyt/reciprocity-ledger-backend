import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/app_providers.dart';

/// 待还提醒页面 - 显示未关联的CONTACT事件
class UnlinkedEventsPage extends ConsumerStatefulWidget {
  const UnlinkedEventsPage({super.key});

  @override
  ConsumerState<UnlinkedEventsPage> createState() => _UnlinkedEventsPageState();
}

class _UnlinkedEventsPageState extends ConsumerState<UnlinkedEventsPage> {
  List<UnlinkedEventVO> _events = [];
  List<EventTypeOption> _eventTypes = [];
  String? _selectedEventTypeId;
  bool _isLoading = true;
  bool _isLoadingMore = false;
  Object? _error;
  int _pageNo = 1;
  bool _hasMore = true;
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
      _pageNo = 1;
      _hasMore = true;
    });
    try {
      final results = await Future.wait([
        fetchUnlinkedEvents(ref, eventTypeId: _selectedEventTypeId),
        fetchEventTypes(ref),
      ]);
      if (mounted) {
        setState(() {
          _events = results[0] as List<UnlinkedEventVO>;
          _eventTypes = results[1] as List<EventTypeOption>;
          _isLoading = false;
          _hasMore = _events.length >= 20;
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

  Future<void> _loadMore() async {
    if (_isLoadingMore || !_hasMore) return;
    setState(() {
      _isLoadingMore = true;
    });
    try {
      final newEvents = await fetchUnlinkedEvents(ref, eventTypeId: _selectedEventTypeId);
      if (mounted) {
        setState(() {
          _events.addAll(newEvents);
          _pageNo++;
          _isLoadingMore = false;
          _hasMore = newEvents.length >= 20;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoadingMore = false;
        });
      }
    }
  }

  void _onEventTypeChanged(String? eventTypeId) {
    if (_selectedEventTypeId != eventTypeId) {
      setState(() {
        _selectedEventTypeId = eventTypeId;
      });
      _loadData();
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
        appBar: AppBar(title: const Text('待还提醒')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      final message = _error is ApiException ? (_error as ApiException).message : '加载失败';
      return Scaffold(
        appBar: AppBar(title: const Text('待还提醒')),
        body: LedgerEmptyStateView(
          title: '加载失败',
          message: message,
          actionText: '重试',
          onAction: _loadData,
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('待还提醒'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: _loadData,
            tooltip: '刷新',
          ),
        ],
      ),
      body: Column(
        children: [
          // 筛选栏
          if (_eventTypes.isNotEmpty)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: theme.colorScheme.surface,
                border: Border(
                  bottom: BorderSide(color: theme.dividerColor),
                ),
              ),
              child: Row(
                children: [
                  Text('事件类型:', style: theme.textTheme.bodyMedium),
                  const SizedBox(width: 12),
                  Expanded(
                    child: DropdownButtonFormField<String>(
                      value: _selectedEventTypeId,
                      decoration: InputDecoration(
                        contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        isDense: true,
                      ),
                      hint: const Text('全部类型'),
                      items: [
                        const DropdownMenuItem(value: null, child: Text('全部类型')),
                        ..._eventTypes.map((type) => DropdownMenuItem(
                          value: type.id,
                          child: Text(type.name),
                        )),
                      ],
                      onChanged: _onEventTypeChanged,
                    ),
                  ),
                ],
              ),
            ),
          // 统计信息
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Text('共 ${_events.length} 项待还', style: theme.textTheme.titleMedium),
              ],
            ),
          ),
          // 列表
          Expanded(
            child: _events.isEmpty
                ? LedgerEmptyStateView(
                    title: '暂无待还提醒',
                    message: '所有送礼事件都已关联收礼事件',
                  )
                : ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    itemCount: _events.length + (_hasMore ? 1 : 0),
                    itemBuilder: (context, index) {
                      if (index == _events.length) {
                        if (_isLoadingMore) {
                          return const Padding(
                            padding: EdgeInsets.all(16),
                            child: Center(child: CircularProgressIndicator()),
                          );
                        }
                        return const SizedBox.shrink();
                      }
                      final event = _events[index];
                      return _UnlinkedEventTile(event: event);
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

class _UnlinkedEventTile extends StatelessWidget {
  const _UnlinkedEventTile({required this.event});

  final UnlinkedEventVO event;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Card(
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          onTap: () => context.push('/contacts/${event.contactId}'),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primaryContainer,
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        event.eventTypeName,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onPrimaryContainer,
                        ),
                      ),
                    ),
                    const Spacer(),
                    Text(
                      LedgerFormatters.fullDate(event.eventDate),
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: theme.colorScheme.outline,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  event.eventName,
                  style: theme.textTheme.titleMedium,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Icon(Icons.person_outline, size: 16, color: theme.colorScheme.outline),
                    const SizedBox(width: 4),
                    Text(
                      event.contactName,
                      style: theme.textTheme.bodyMedium,
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Icon(Icons.card_giftcard_outlined, size: 16, color: theme.colorScheme.outline),
                    const SizedBox(width: 4),
                    Text(
                      '已送: ${LedgerFormatters.amount(event.sendAmount)}',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: theme.colorScheme.error,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
