import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/core/network/api_exception.dart';
import 'package:frontend/features/utente/viewmodels/auth_view_model.dart';
import 'package:frontend/features/utente/viewmodels/login_view_model.dart';

import '../../helpers/fakes.dart';

void main() {
  test('successful login authenticates the session', () async {
    final repository = FakeUtenteRepository(
      onLogin: (email, password) async => sampleLoginResponse,
    );
    final auth = AuthViewModel(repository);
    final viewModel = LoginViewModel(auth);

    final success = await viewModel.submit(
      email: ' mario.rossi@example.com ',
      password: 'Passw0rd!',
    );

    expect(success, isTrue);
    expect(auth.status, AuthStatus.authenticated);
    expect(auth.user, sampleUser);
    expect(viewModel.errorMessage, isNull);
    expect(viewModel.isLoading, isFalse);
  });

  test('wrong credentials show the invalid login message', () async {
    final repository = FakeUtenteRepository(
      onLogin: (email, password) async => throw const UnauthorizedException(),
    );
    final auth = AuthViewModel(repository);
    final viewModel = LoginViewModel(auth);

    final success = await viewModel.submit(
      email: 'mario.rossi@example.com',
      password: 'wrong',
    );

    expect(success, isFalse);
    expect(auth.status, AuthStatus.unknown);
    expect(viewModel.errorMessage, 'Invalid login. Please try again.');
    expect(viewModel.isLoading, isFalse);
  });

  test('network errors are reported with their own message', () async {
    final repository = FakeUtenteRepository(
      onLogin: (email, password) async => throw const NetworkException(),
    );
    final viewModel = LoginViewModel(AuthViewModel(repository));

    await viewModel.submit(email: 'a@b.it', password: 'x');

    expect(viewModel.errorMessage, const NetworkException().message);
  });

  test('shows and clears the session-expired notice', () async {
    final repository = FakeUtenteRepository();
    final auth = AuthViewModel(repository);
    await auth.onSessionExpired();

    final viewModel = LoginViewModel(auth);

    expect(viewModel.notice, 'Your session has expired. Please sign in again.');
    expect(auth.notice, isNull);
    expect(repository.logoutCalls, 1);
  });
}
