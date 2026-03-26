import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/widgets/ledger_bottom_nav_scaffold.dart';
import '../../features/contact/presentation/pages/contact_detail_page.dart';
import '../../features/home/presentation/pages/home_page.dart';
import '../../features/record/presentation/pages/record_editor_page.dart';
import '../../features/reciprocity/presentation/pages/reciprocity_detail_page.dart';
import '../../features/reciprocity/presentation/pages/reciprocity_page.dart';
import '../../features/timeline/presentation/pages/timeline_page.dart';
import '../../shared/models/ledger_models.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/home',
    routes: [
      ShellRoute(
        builder: (context, state, child) {
          return LedgerBottomNavScaffold(
            location: state.uri.toString(),
            child: child,
          );
        },
        routes: [
          GoRoute(
            path: '/home',
            builder: (context, state) => const HomePage(),
          ),
          GoRoute(
            path: '/timeline',
            builder: (context, state) => const TimelinePage(),
          ),
          GoRoute(
            path: '/reciprocity',
            builder: (context, state) => const ReciprocityPage(),
          ),
        ],
      ),
      GoRoute(
        path: '/record/editor',
        builder: (context, state) {
          return RecordEditorPage(
            initialKind: parseRecordKind(state.uri.queryParameters['kind']),
            initialContactId: state.uri.queryParameters['contactId'],
          );
        },
      ),
      GoRoute(
        path: '/contacts/:contactId',
        builder: (context, state) {
          return ContactDetailPage(
            contactId: state.pathParameters['contactId']!,
          );
        },
      ),
      GoRoute(
        path: '/reciprocity/:eventId',
        builder: (context, state) {
          return ReciprocityDetailPage(
            eventId: state.pathParameters['eventId']!,
          );
        },
      ),
    ],
  );
});
