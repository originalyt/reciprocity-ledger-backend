import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../storage/token_storage_service.dart';
import '../../shared/models/user_models.dart';

enum AuthStatus { initial, authenticated, unauthenticated }

class AuthState {
  const AuthState({
    required this.status,
    this.user,
    this.token,
  });

  final AuthStatus status;
  final User? user;
  final String? token;

  bool get isAuthenticated => status == AuthStatus.authenticated;

  AuthState copyWith({
    AuthStatus? status,
    User? user,
    String? token,
  }) {
    return AuthState(
      status: status ?? this.status,
      user: user ?? this.user,
      token: token ?? this.token,
    );
  }
}

class AuthNotifier extends Notifier<AuthState> {
  @override
  AuthState build() {
    _init();
    return const AuthState(status: AuthStatus.initial);
  }

  final TokenStorageService _tokenStorage = TokenStorageService();

  Future<void> _init() async {
    final isLoggedIn = await _tokenStorage.isLoggedIn();
    if (isLoggedIn) {
      final user = await _tokenStorage.getUser();
      final token = await _tokenStorage.getToken();
      if (user != null && token != null) {
        state = AuthState(status: AuthStatus.authenticated, user: user, token: token);
      } else {
        state = const AuthState(status: AuthStatus.unauthenticated);
      }
    } else {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  Future<void> login(User user, String token) async {
    await _tokenStorage.saveToken(token);
    await _tokenStorage.saveUser(user);
    state = AuthState(status: AuthStatus.authenticated, user: user, token: token);
  }

  Future<void> logout() async {
    await _tokenStorage.removeToken();
    await _tokenStorage.removeUser();
    state = const AuthState(status: AuthStatus.unauthenticated, token: null);
  }

  User? get currentUser => state.user;
  String? get currentToken => state.token;
}

final authProvider = NotifierProvider<AuthNotifier, AuthState>(() {
  return AuthNotifier();
});

final authStateProvider = Provider<AuthStatus>((ref) {
  return ref.watch(authProvider).status;
});

final currentUserProvider = Provider<User?>((ref) {
  return ref.watch(authProvider).user;
});

final tokenProvider = Provider<String?>((ref) {
  return ref.watch(authProvider).token;
});

final isAuthenticatedProvider = Provider<bool>((ref) {
  return ref.watch(authStateProvider) == AuthStatus.authenticated;
});
