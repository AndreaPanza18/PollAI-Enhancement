import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/app.dart';

import 'helpers/fakes.dart';

void main() {
  testWidgets('shows the login screen when there is no saved session', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(
      PollAIApp(
        repository: FakeUtenteRepository(onRestoreSession: () async => null),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Sign in to your account'), findsOneWidget);
    expect(find.text('Log in'), findsOneWidget);
  });

  testWidgets('opens the user area when a saved session is valid', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(
      PollAIApp(
        repository: FakeUtenteRepository(
          onRestoreSession: () async => sampleUser,
          onGetAreaUtente: () async => sampleUser,
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('User area'), findsOneWidget);
    expect(find.text('Welcome back, Mario!'), findsOneWidget);
  });
}
