import 'package:dio/dio.dart';

import 'api_config.dart';
import 'api_response.dart';

class ApiClient {
  ApiClient({Dio? dio}) : _dio = dio ?? Dio(_baseOptions);

  final Dio _dio;

  static final BaseOptions _baseOptions = BaseOptions(
    baseUrl: ApiConfig.baseUrl,
    connectTimeout: const Duration(seconds: 8),
    receiveTimeout: const Duration(seconds: 8),
    sendTimeout: const Duration(seconds: 8),
    contentType: Headers.jsonContentType,
  );

  Future<ApiResponse<T>> post<T>(
    String path,
    Map<String, dynamic> body,
    T Function(Object? json) fromJsonT,
  ) async {
    final response = await _dio.post<Map<String, dynamic>>(path, data: body);
    final payload = response.data ?? const <String, dynamic>{};
    return ApiResponse.fromJson(payload, fromJsonT);
  }
}
