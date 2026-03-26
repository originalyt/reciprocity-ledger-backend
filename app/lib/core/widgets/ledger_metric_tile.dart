import 'package:flutter/material.dart';

class LedgerMetricTile extends StatelessWidget {
  const LedgerMetricTile({
    super.key,
    required this.label,
    required this.value,
    this.emphasize = false,
  });

  final String label;
  final String value;
  final bool emphasize;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: theme.textTheme.bodyMedium),
        const SizedBox(height: 8),
        Text(
          value,
          style: (emphasize ? theme.textTheme.headlineMedium : theme.textTheme.titleLarge)
              ?.copyWith(fontWeight: FontWeight.w700),
        ),
      ],
    );
  }
}
