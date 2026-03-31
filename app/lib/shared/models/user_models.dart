import 'package:flutter/material.dart';

class User {
  const User({
    required this.id,
    required this.email,
    this.nickname,
    this.avatarUrl,
  });

  final String id;
  final String email;
  final String? nickname;
  final String? avatarUrl;

  String get displayName => nickname ?? email;

  User copyWith({
    String? id,
    String? email,
    String? nickname,
    String? avatarUrl,
  }) {
    return User(
      id: id ?? this.id,
      email: email ?? this.email,
      nickname: nickname ?? this.nickname,
      avatarUrl: avatarUrl ?? this.avatarUrl,
    );
  }
}
