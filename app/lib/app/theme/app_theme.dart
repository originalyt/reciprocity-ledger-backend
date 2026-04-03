import 'package:flutter/material.dart';

import 'ledger_theme_extension.dart';

class AppTheme {
  AppTheme._();

  static const Color background = Color(0xFFF3EFE6);
  static const Color surface = Color(0xFFFFFDF8);
  static const Color surfaceMuted = Color(0xFFECE5DA);
  static const Color textPrimary = Color(0xFF1F1A17);
  static const Color textSecondary = Color(0xFF6B625C);
  static const Color textTertiary = Color(0xFF9B9188);
  static const Color border = Color(0xFFDED6CC);
  static const Color primary = Color(0xFFA24B36);
  static const Color primarySoft = Color(0xFFF2E0DA);
  static const Color primaryText = Color(0xFF6C2F21);
  static const Color accent = Color(0xFF2F5D62);
  static const Color accentSoft = Color(0xFFDDEBED);

  static ThemeData light() {
    const colorScheme = ColorScheme.light(
      primary: primary,
      onPrimary: Colors.white,
      primaryContainer: primarySoft,
      onPrimaryContainer: primaryText,
      secondary: accent,
      onSecondary: Colors.white,
      secondaryContainer: accentSoft,
      onSecondaryContainer: accent,
      surface: surface,
      onSurface: textPrimary,
      error: Color(0xFFB64A3B),
      onError: Colors.white,
      errorContainer: Color(0xFFFCE8E6),
      onErrorContainer: Color(0xFFB64A3B),
      outline: border,
    );

    final base = ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: background,
      canvasColor: background,
      fontFamilyFallback: const [
        'Noto Sans SC',
        'PingFang SC',
        'Microsoft YaHei',
        'sans-serif',
      ],
    );

    final textTheme = base.textTheme.copyWith(
      headlineMedium: base.textTheme.headlineMedium?.copyWith(
        fontSize: 24,
        fontWeight: FontWeight.w600,
        color: textPrimary,
      ),
      titleLarge: base.textTheme.titleLarge?.copyWith(
        fontSize: 20,
        fontWeight: FontWeight.w600,
        color: textPrimary,
      ),
      titleMedium: base.textTheme.titleMedium?.copyWith(
        fontSize: 16,
        fontWeight: FontWeight.w500,
        color: textPrimary,
      ),
      bodyLarge: base.textTheme.bodyLarge?.copyWith(
        fontSize: 16,
        color: textPrimary,
      ),
      bodyMedium: base.textTheme.bodyMedium?.copyWith(
        fontSize: 14,
        color: textSecondary,
      ),
      labelLarge: base.textTheme.labelLarge?.copyWith(
        fontSize: 16,
        fontWeight: FontWeight.w600,
      ),
      labelMedium: base.textTheme.labelMedium?.copyWith(
        fontSize: 12,
        fontWeight: FontWeight.w600,
        color: textSecondary,
      ),
    );

    return base.copyWith(
      textTheme: textTheme,
      appBarTheme: const AppBarTheme(
        backgroundColor: background,
        foregroundColor: textPrimary,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
      ),
      cardTheme: CardThemeData(
        color: surface,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: surface,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: primary, width: 1.4),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFFB64A3B)),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: primary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(50),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: textPrimary,
          side: const BorderSide(color: border),
          minimumSize: const Size.fromHeight(46),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
      ),
      chipTheme: base.chipTheme.copyWith(
        side: const BorderSide(color: border),
        backgroundColor: surface,
        selectedColor: primarySoft,
        labelStyle: const TextStyle(
          color: textPrimary,
          fontWeight: FontWeight.w500,
        ),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(999),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: surface,
        indicatorColor: primarySoft,
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return const TextStyle(fontWeight: FontWeight.w600, color: primaryText);
          }
          return const TextStyle(fontWeight: FontWeight.w500, color: textSecondary);
        }),
      ),
      dividerColor: border,
      extensions: const [
        LedgerStatusTheme(
          waitMe: Color(0xFFC46B2D),
          waitMeSoft: Color(0xFFF8E8D8),
          waitOther: Color(0xFF2F6A78),
          waitOtherSoft: Color(0xFFDFEEF1),
          mutual: Color(0xFF547A4F),
          mutualSoft: Color(0xFFE4EDE2),
          noNeed: Color(0xFF8C847C),
          noNeedSoft: Color(0xFFECE8E4),
          invalid: Color(0xFFA9A29A),
          invalidSoft: Color(0xFFF1EFED),
        ),
      ],
    );
  }
}
