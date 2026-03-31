import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/api_client.dart';
import '../../shared/models/auth_models.dart';

class AuthApi {
  const AuthApi();

  Future<LoginResponse> login(
    WidgetRef ref,
    String email,
    String password,
  ) async {
    final client = ApiClient(ref: ref);
    return client.post(
      '/app/user/login',
      {'email': email, 'password': password},
      requireAuth: false,
      fromJsonT: (json) => LoginResponse.fromJson(json as Map<String, dynamic>),
    );
  }

  Future<LoginResponse> register(
    WidgetRef ref,
    String email,
    String password, {
    String? nickname,
  }) async {
    final client = ApiClient(ref: ref);
    final body = <String, dynamic>{
      'email': email,
      'password': password,
    };
    if (nickname != null) {
      body['nickname'] = nickname;
    }
    return client.post(
      '/app/user/register',
      body,
      requireAuth: false,
      fromJsonT: (json) => LoginResponse.fromJson(json as Map<String, dynamic>),
    );
  }
}
