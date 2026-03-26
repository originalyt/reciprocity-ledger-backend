class ApiResponse<T> {
  const ApiResponse({
    required this.code,
    required this.message,
    required this.data,
  });

  final int code;
  final String message;
  final T data;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Object? json) fromJsonT,
  ) {
    return ApiResponse<T>(
      code: json['code'] as int? ?? -1,
      message: json['message'] as String? ?? '未知错误',
      data: fromJsonT(json['data']),
    );
  }
}
