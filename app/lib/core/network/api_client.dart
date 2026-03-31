import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'api_config.dart';
import 'api_exception.dart';
import 'api_response.dart';
import '../auth/auth_provider.dart';

class ApiClient {
  ApiClient({Dio? dio, WidgetRef? ref}) : _dio = dio ?? Dio(_baseOptions), _ref = ref {
    _dio.interceptors.add(_authInterceptor);
  }

  final Dio _dio;
  final WidgetRef? _ref;

  static final BaseOptions _baseOptions = BaseOptions(
    baseUrl: ApiConfig.baseUrl,
    connectTimeout: const Duration(seconds: 8),
    receiveTimeout: const Duration(seconds: 8),
    sendTimeout: const Duration(seconds: 8),
    contentType: Headers.jsonContentType,
  );

  InterceptorsWrapper get _authInterceptor => InterceptorsWrapper(
    onRequest: (options, handler) {
      return handler.next(options);
    },
    onResponse: (response, handler) {
      return handler.next(response);
    },
    onError: (DioException error, handler) async {
      if (error.response?.statusCode == 401) {
        return handler.reject(
          DioException(
            requestOptions: error.requestOptions,
            response: error.response,
            type: error.type,
            error: error.error,
            message: '未登录或token已过期',
          ),
        );
      }
      return handler.next(error);
    },
  );

  Future<T> post<T>(
    String path,
    Map<String, dynamic> body, {
    WidgetRef? ref,
    bool requireAuth = true,
    T Function(Object? json)? fromJsonT,
  }) async {
    final effectiveRef = ref ?? _ref;
    final options = Options(
      headers: effectiveRef != null && requireAuth
          ? {'Authorization': 'Bearer ${effectiveRef.read(tokenProvider)}'}
          : {},
    );

    try {
      final response = await _dio.post<Map<String, dynamic>>(path, data: body, options: options);
      final payload = response.data ?? const <String, dynamic>{};
      final apiResponse = ApiResponse.fromJson(payload, fromJsonT!);
      if (apiResponse.code != 0) {
        throw ApiException(code: apiResponse.code, message: apiResponse.message);
      }
      return apiResponse.data;
    } on DioException catch (error) {
      final message = error.message ??
          '连接后端服务失败，请确认接口地址 ${ApiConfig.baseUrl} 可访问，且后端服务已启动';
      throw ApiException(code: error.response?.statusCode ?? -1, message: message);
    }
  }
}
