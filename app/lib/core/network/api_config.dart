import 'package:flutter/foundation.dart';

class ApiConfig {
  ApiConfig._();

  static const int _defaultPort = 10010;

  static String get baseUrl {
    const configured = String.fromEnvironment('API_BASE_URL', defaultValue: '');
    if (configured.isNotEmpty) {
      return configured;
    }
    if (kIsWeb) {
      return '${Uri.base.scheme}://${Uri.base.host}:$_defaultPort';
    }
    if (defaultTargetPlatform == TargetPlatform.android) {
      return 'http://10.0.2.2:$_defaultPort';
    }
    return 'http://127.0.0.1:$_defaultPort';
  }
}
