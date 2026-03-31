import 'user_models.dart';

class LoginRequest {
  const LoginRequest({
    required this.email,
    required this.password,
  });

  final String email;
  final String password;

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
    };
  }
}

class RegisterRequest {
  const RegisterRequest({
    required this.email,
    required this.password,
    this.nickname,
  });

  final String email;
  final String password;
  final String? nickname;

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{
      'email': email,
      'password': password,
    };
    if (nickname != null) {
      json['nickname'] = nickname!;
    }
    return json;
  }
}

class LoginResponse {
  const LoginResponse({
    required this.userId,
    required this.email,
    required this.token,
    this.nickname,
    this.avatarUrl,
  });

  final String userId;
  final String email;
  final String token;
  final String? nickname;
  final String? avatarUrl;

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      userId: json['userId'] as String,
      email: json['email'] as String,
      token: json['token'] as String,
      nickname: json['nickname'] as String?,
      avatarUrl: json['avatarUrl'] as String?,
    );
  }

  User toUser() {
    return User(
      id: userId,
      email: email,
      nickname: nickname,
      avatarUrl: avatarUrl,
    );
  }
}
