import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:reciprocity_ledger_app/app/app.dart';

void main() {
  testWidgets('首页可正常渲染', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LedgerApp()));
    await tester.pumpAndSettle();

    expect(find.text('礼账'), findsOneWidget);
    expect(find.text('今年概览'), findsOneWidget);
  });
}
