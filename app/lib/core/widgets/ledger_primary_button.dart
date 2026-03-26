import 'package:flutter/material.dart';

class LedgerPrimaryButton extends StatelessWidget {
  const LedgerPrimaryButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.icon,
    this.isLoading = false,
  });

  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    final child = isLoading
        ? const SizedBox(
            width: 18,
            height: 18,
            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
          )
        : Text(label);

    if (icon == null) {
      return FilledButton(
        onPressed: isLoading ? null : onPressed,
        child: child,
      );
    }

    return FilledButton.icon(
      onPressed: isLoading ? null : onPressed,
      icon: isLoading ? const SizedBox.shrink() : Icon(icon),
      label: child,
    );
  }
}
