import 'package:shared_preferences/shared_preferences.dart';
import '../../shared/models/user_models.dart';

class TokenStorageService {
  static final TokenStorageService _instance = TokenStorageService._internal();
  factory TokenStorageService() => _instance;
  TokenStorageService._internal();

  static const String _tokenKey = 'auth_token';
  static const String _userIdKey = 'auth_user_id';
  static const String _userEmailKey = 'auth_user_email';
  static const String _userNicknameKey = 'auth_user_nickname';
  static const String _userAvatarUrlKey = 'auth_user_avatar_url';

  Future<SharedPreferences> get _prefs async =>
      await SharedPreferences.getInstance();

  Future<String?> getToken() async {
    final prefs = await _prefs;
    return prefs.getString(_tokenKey);
  }

  Future<void> saveToken(String token) async {
    final prefs = await _prefs;
    await prefs.setString(_tokenKey, token);
  }

  Future<void> removeToken() async {
    final prefs = await _prefs;
    await prefs.remove(_tokenKey);
  }

  Future<User?> getUser() async {
    final prefs = await _prefs;
    final userId = prefs.getString(_userIdKey);
    final email = prefs.getString(_userEmailKey);
    final nickname = prefs.getString(_userNicknameKey);
    final avatarUrl = prefs.getString(_userAvatarUrlKey);

    if (userId == null || email == null) {
      return null;
    }

    return User(
      id: userId!,
      email: email!,
      nickname: nickname,
      avatarUrl: avatarUrl,
    );
  }

  Future<void> saveUser(User user) async {
    final prefs = await _prefs;
    await Future.wait([
      prefs.setString(_userIdKey, user.id),
      prefs.setString(_userEmailKey, user.email),
      if (user.nickname != null)
        prefs.setString(_userNicknameKey, user.nickname!),
      if (user.avatarUrl != null)
        prefs.setString(_userAvatarUrlKey, user.avatarUrl!),
    ]);
  }

  Future<void> removeUser() async {
    final prefs = await _prefs;
    await Future.wait([
      prefs.remove(_userIdKey),
      prefs.remove(_userEmailKey),
      prefs.remove(_userNicknameKey),
      prefs.remove(_userAvatarUrlKey),
    ]);
  }

  Future<void> clear() async {
    final prefs = await _prefs;
    await prefs.clear();
  }

  Future<bool> isLoggedIn() async {
    final token = await getToken();
    final user = await getUser();
    return token != null && token!.isNotEmpty && user != null;
  }
}
