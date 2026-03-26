import 'package:flutter/material.dart';

@immutable
class LedgerStatusTheme extends ThemeExtension<LedgerStatusTheme> {
  const LedgerStatusTheme({
    required this.waitMe,
    required this.waitMeSoft,
    required this.waitOther,
    required this.waitOtherSoft,
    required this.mutual,
    required this.mutualSoft,
    required this.noNeed,
    required this.noNeedSoft,
    required this.invalid,
    required this.invalidSoft,
  });

  final Color waitMe;
  final Color waitMeSoft;
  final Color waitOther;
  final Color waitOtherSoft;
  final Color mutual;
  final Color mutualSoft;
  final Color noNeed;
  final Color noNeedSoft;
  final Color invalid;
  final Color invalidSoft;

  @override
  LedgerStatusTheme copyWith({
    Color? waitMe,
    Color? waitMeSoft,
    Color? waitOther,
    Color? waitOtherSoft,
    Color? mutual,
    Color? mutualSoft,
    Color? noNeed,
    Color? noNeedSoft,
    Color? invalid,
    Color? invalidSoft,
  }) {
    return LedgerStatusTheme(
      waitMe: waitMe ?? this.waitMe,
      waitMeSoft: waitMeSoft ?? this.waitMeSoft,
      waitOther: waitOther ?? this.waitOther,
      waitOtherSoft: waitOtherSoft ?? this.waitOtherSoft,
      mutual: mutual ?? this.mutual,
      mutualSoft: mutualSoft ?? this.mutualSoft,
      noNeed: noNeed ?? this.noNeed,
      noNeedSoft: noNeedSoft ?? this.noNeedSoft,
      invalid: invalid ?? this.invalid,
      invalidSoft: invalidSoft ?? this.invalidSoft,
    );
  }

  @override
  LedgerStatusTheme lerp(ThemeExtension<LedgerStatusTheme>? other, double t) {
    if (other is! LedgerStatusTheme) {
      return this;
    }

    return LedgerStatusTheme(
      waitMe: Color.lerp(waitMe, other.waitMe, t) ?? waitMe,
      waitMeSoft: Color.lerp(waitMeSoft, other.waitMeSoft, t) ?? waitMeSoft,
      waitOther: Color.lerp(waitOther, other.waitOther, t) ?? waitOther,
      waitOtherSoft:
          Color.lerp(waitOtherSoft, other.waitOtherSoft, t) ?? waitOtherSoft,
      mutual: Color.lerp(mutual, other.mutual, t) ?? mutual,
      mutualSoft: Color.lerp(mutualSoft, other.mutualSoft, t) ?? mutualSoft,
      noNeed: Color.lerp(noNeed, other.noNeed, t) ?? noNeed,
      noNeedSoft: Color.lerp(noNeedSoft, other.noNeedSoft, t) ?? noNeedSoft,
      invalid: Color.lerp(invalid, other.invalid, t) ?? invalid,
      invalidSoft: Color.lerp(invalidSoft, other.invalidSoft, t) ?? invalidSoft,
    );
  }
}
