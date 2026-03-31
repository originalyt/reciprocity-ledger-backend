import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_provider.dart';

class LedgerBottomNavScaffold extends ConsumerStatefulWidget {
  const LedgerBottomNavScaffold({
    super.key,
    required this.location,
    required this.child,
  });

  final String location;
  final Widget child;

  @override
  ConsumerState<LedgerBottomNavScaffold> createState() => _LedgerBottomNavScaffoldState();
}

class _LedgerBottomNavScaffoldState extends ConsumerState<LedgerBottomNavScaffold> {
  int get _currentIndex {
    if (widget.location.startsWith('/timeline')) {
      return 1;
    }
    if (widget.location.startsWith('/reciprocity')) {
      return 2;
    }
    if (widget.location.startsWith('/profile')) {
      return 3;
    }
    return 0;
  }

  void _handleLogout() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.of(context).pop();
              ref.read(authProvider.notifier).logout();
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: widget.child,
      floatingActionButton: widget.location.startsWith('/home')
          ? FloatingActionButton.extended(
              onPressed: () => context.push('/record/editor'),
              icon: const Icon(Icons.add_rounded),
              label: const Text('记一笔'),
            )
          : null,
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) {
          switch (index) {
            case 0:
              context.go('/home');
            case 1:
              context.go('/timeline');
            case 2:
              context.go('/reciprocity');
            case 3:
              context.go('/profile');
          }
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home_rounded),
            label: '首页',
          ),
          NavigationDestination(
            icon: Icon(Icons.schedule_outlined),
            selectedIcon: Icon(Icons.schedule_rounded),
            label: '时间线',
          ),
          NavigationDestination(
            icon: Icon(Icons.assignment_outlined),
            selectedIcon: Icon(Icons.assignment_rounded),
            label: '回礼',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person_rounded),
            label: '我的',
          ),
        ],
      ),
    );
  }
}
