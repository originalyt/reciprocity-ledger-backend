import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:reciprocity_ledger_app/app/app.dart';
import 'package:reciprocity_ledger_app/shared/providers/mock_providers.dart';

void main() {
  testWidgets('首页可正常渲染', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          useMockDataProvider.overrideWith((ref) => true),
        ],
        child: const LedgerApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('礼账'), findsOneWidget);
    expect(find.text('首页概览'), findsOneWidget);
  });
}
