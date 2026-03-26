import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class LedgerBottomNavScaffold extends StatelessWidget {
  const LedgerBottomNavScaffold({
    super.key,
    required this.location,
    required this.child,
  });

  final String location;
  final Widget child;

  int get _currentIndex {
    if (location.startsWith('/timeline')) {
      return 1;
    }
    if (location.startsWith('/reciprocity')) {
      return 2;
    }
    return 0;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: child,
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/record/editor'),
        icon: const Icon(Icons.add_rounded),
        label: const Text('记一笔'),
      ),
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
        ],
      ),
    );
  }
}
