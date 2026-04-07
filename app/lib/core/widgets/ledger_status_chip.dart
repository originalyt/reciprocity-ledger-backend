import 'package:flutter/material.dart';

import '../../app/theme/app_theme.dart';
import '../../app/theme/ledger_theme_extension.dart';
import '../../shared/models/ledger_models.dart';

class LedgerStatusChip extends StatelessWidget {
  const LedgerStatusChip.status({super.key, required this.status}) : kind = null;

  const LedgerStatusChip.recordKind({super.key, required this.kind}) : status = null;

  final ReciprocityStatus? status;
  final RecordKind? kind;

  @override
  Widget build(BuildContext context) {
    final style = _resolveStyle(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: style.background,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        style.label,
        style: Theme.of(context).textTheme.labelMedium?.copyWith(
              color: style.foreground,
              fontWeight: FontWeight.w700,
            ),
      ),
    );
  }

  _ChipStyle _resolveStyle(BuildContext context) {
    final statusTheme = Theme.of(context).extension<LedgerStatusTheme>()!;

    if (kind != null) {
      switch (kind!) {
        case RecordKind.give:
          return const _ChipStyle('随礼', AppTheme.primarySoft, AppTheme.primaryText);
        case RecordKind.receive:
          return const _ChipStyle('收礼', AppTheme.accentSoft, AppTheme.accent);
      }
    }

    switch (status!) {
      case ReciprocityStatus.unmatched:
        return _ChipStyle('待往来', statusTheme.waitMeSoft, statusTheme.waitMe);
      case ReciprocityStatus.matched:
        return _ChipStyle('已往来', statusTheme.mutualSoft, statusTheme.mutual);
      case ReciprocityStatus.manualConfirmed:
        return _ChipStyle('手动确认', statusTheme.waitOtherSoft, statusTheme.waitOther);
      case ReciprocityStatus.manualCanceled:
        return _ChipStyle('已取消', statusTheme.noNeedSoft, statusTheme.noNeed);
    }
  }
}

class _ChipStyle {
  const _ChipStyle(this.label, this.background, this.foreground);

  final String label;
  final Color background;
  final Color foreground;
}
