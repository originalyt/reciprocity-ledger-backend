import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/ledger_empty_state_view.dart';
import '../../../../core/widgets/ledger_primary_button.dart';
import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/app_providers.dart';

/// 事件关联管理页面
class EventRelationPage extends ConsumerStatefulWidget {
  const EventRelationPage({
    super.key,
    required this.selfEventId,
    this.selfEventName,
  });

  final String selfEventId;
  final String? selfEventName;

  @override
  ConsumerState<EventRelationPage> createState() => _EventRelationPageState();
}

class _EventRelationPageState extends ConsumerState<EventRelationPage> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<EventRelationVO> _relations = [];
  List<SuggestRelationVO> _suggestions = [];
  bool _isLoading = true;
  Object? _error;
  int _lastRefreshVersion = -1;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final results = await Future.wait([
        fetchEventRelationList(ref, widget.selfEventId),
        fetchSuggestRelations(ref, widget.selfEventId),
      ]);
      if (mounted) {
        setState(() {
          _relations = results[0] as List<EventRelationVO>;
          _suggestions = results[1] as List<SuggestRelationVO>;
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

  Future<void> _addRelation(SuggestRelationVO suggestion) async {
    try {
      await saveEventRelation(ref, widget.selfEventId, suggestion.eventId);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('关联成功')),
        );
        _loadData();
      }
    } catch (e) {
      if (mounted) {
        final message = e is ApiException ? e.message : '关联失败';
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(message)),
        );
      }
    }
  }

  Future<void> _deleteRelation(EventRelationVO relation) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认删除'),
        content: Text('确定要删除与 "${relation.eventName}" 的关联吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('删除'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await deleteEventRelation(ref, relation.id);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('删除成功')),
          );
          _loadData();
        }
      } catch (e) {
        if (mounted) {
          final message = e is ApiException ? e.message : '删除失败';
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(message)),
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

    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(title: const Text('事件关联')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      final message = _error is ApiException ? (_error as ApiException).message : '加载失败';
      return Scaffold(
        appBar: AppBar(title: const Text('事件关联')),
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
        title: const Text('事件关联'),
        bottom: TabBar(
          controller: _tabController,
          tabs: [
            Tab(text: '已关联 (${_relations.length})'),
            Tab(text: '可关联 (${_suggestions.length})'),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: _loadData,
            tooltip: '刷新',
          ),
        ],
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildRelationsList(),
          _buildSuggestionsList(),
        ],
      ),
    );
  }

  Widget _buildRelationsList() {
    if (_relations.isEmpty) {
      return const LedgerEmptyStateView(
        title: '暂无关联',
        message: '还没有关联任何联系人事件',
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _relations.length,
      itemBuilder: (context, index) {
        final relation = _relations[index];
        return _RelationTile(
          relation: relation,
          onDelete: () => _deleteRelation(relation),
        );
      },
    );
  }

  Widget _buildSuggestionsList() {
    if (_suggestions.isEmpty) {
      return const LedgerEmptyStateView(
        title: '暂无可关联事件',
        message: '没有可关联的联系人事件，或所有同类型事件已关联',
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _suggestions.length,
      itemBuilder: (context, index) {
        final suggestion = _suggestions[index];
        return _SuggestionTile(
          suggestion: suggestion,
          onAdd: () => _addRelation(suggestion),
        );
      },
    );
  }
}

class _RelationTile extends StatelessWidget {
  const _RelationTile({
    required this.relation,
    required this.onDelete,
  });

  final EventRelationVO relation;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          relation.eventName,
                          style: theme.textTheme.titleMedium,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          LedgerFormatters.fullDate(relation.eventDate),
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.outline,
                          ),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: Icon(Icons.delete_outline, color: theme.colorScheme.error),
                    onPressed: onDelete,
                    tooltip: '删除关联',
                  ),
                ],
              ),
              const Divider(height: 24),
              Row(
                children: [
                  Icon(Icons.person_outline, size: 16, color: theme.colorScheme.outline),
                  const SizedBox(width: 4),
                  Text(relation.contactName, style: theme.textTheme.bodyMedium),
                  const Spacer(),
                  Icon(Icons.card_giftcard_outlined, size: 16, color: theme.colorScheme.outline),
                  const SizedBox(width: 4),
                  Text(
                    '已送: ${LedgerFormatters.amount(relation.sendAmount)}',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SuggestionTile extends StatelessWidget {
  const _SuggestionTile({
    required this.suggestion,
    required this.onAdd,
  });

  final SuggestRelationVO suggestion;
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          suggestion.eventName,
                          style: theme.textTheme.titleMedium,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          LedgerFormatters.fullDate(suggestion.eventDate),
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.outline,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Icon(Icons.person_outline, size: 16, color: theme.colorScheme.outline),
                  const SizedBox(width: 4),
                  Text(suggestion.contactName, style: theme.textTheme.bodyMedium),
                  const Spacer(),
                  Icon(Icons.card_giftcard_outlined, size: 16, color: theme.colorScheme.outline),
                  const SizedBox(width: 4),
                  Text(
                    '已送: ${LedgerFormatters.amount(suggestion.sendAmount)}',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: LedgerPrimaryButton(
                  label: '添加关联',
                  onPressed: onAdd,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
