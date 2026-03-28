import 'package:dio/dio.dart';

import 'api_config.dart';
import 'api_exception.dart';
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

  Future<T> post<T>(
    String path,
    Map<String, dynamic> body,
    T Function(Object? json) fromJsonT,
  ) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(path, data: body);
      final payload = response.data ?? const <String, dynamic>{};
      final apiResponse = ApiResponse.fromJson(payload, fromJsonT);
      if (apiResponse.code != 0) {
        throw ApiException(code: apiResponse.code, message: apiResponse.message);
      }
      return apiResponse.data;
    } on DioException catch (error) {
      final message =
          error.message ??
          '连接后端服务失败，请确认接口地址 ${ApiConfig.baseUrl} 可访问，且后端服务已启动';
      throw ApiException(code: -1, message: message);
    }
  }
}
