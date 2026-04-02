import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_provider.dart';
import '../../core/widgets/ledger_bottom_nav_scaffold.dart';
import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/auth/presentation/pages/register_page.dart';
import '../../features/contact/presentation/pages/contact_detail_page.dart';
import '../../features/event/presentation/pages/event_relation_page.dart';
import '../../features/event/presentation/pages/unlinked_events_page.dart';
import '../../features/home/presentation/pages/home_page.dart';
import '../../features/profile/presentation/pages/profile_page.dart';
import '../../features/record/presentation/pages/record_editor_page.dart';
import '../../features/reciprocity/presentation/pages/reciprocity_detail_page.dart';
import '../../features/reciprocity/presentation/pages/reciprocity_page.dart';
import '../../features/timeline/presentation/pages/timeline_page.dart';
import '../../shared/models/ledger_models.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/login',
    refreshListenable: ValueNotifier(ref.read(isAuthenticatedProvider)),
    redirect: (context, state) {
      final isAuthenticated = ref.read(isAuthenticatedProvider);
      final isOnAuthPage = state.uri.path == '/login' || state.uri.path == '/register';

      if (!isAuthenticated && !isOnAuthPage) {
        return '/login';
      }

      if (isAuthenticated && isOnAuthPage) {
        return '/home';
      }

      return null;
    },
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
          GoRoute(
            path: '/profile',
            builder: (context, state) => const ProfilePage(),
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
      GoRoute(
        path: '/event/relation/:eventId',
        builder: (context, state) {
          return EventRelationPage(
            selfEventId: state.pathParameters['eventId']!,
            selfEventName: state.uri.queryParameters['name'],
          );
        },
      ),
      GoRoute(
        path: '/event/unlinked',
        builder: (context, state) => const UnlinkedEventsPage(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterPage(),
      ),
    ],
  );
});
