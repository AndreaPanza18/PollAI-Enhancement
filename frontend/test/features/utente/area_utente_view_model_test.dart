import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/core/network/api_exception.dart';
import 'package:frontend/features/utente/data/utente_repository.dart';
import 'package:frontend/features/utente/viewmodels/area_utente_view_model.dart';
import 'package:frontend/features/utente/viewmodels/auth_view_model.dart';

import '../../helpers/fakes.dart';

void main() {
  AreaUtenteViewModel build(FakeUtenteRepository repository) {
    return AreaUtenteViewModel(
      repository: repository,
      auth: AuthViewModel(repository),
    );
  }

  test('loads the user area', () async {
    final viewModel = build(
      FakeUtenteRepository(onGetAreaUtente: () async => sampleUser),
    );

    await viewModel.load();

    expect(viewModel.status, AreaUtenteStatus.ready);
    expect(viewModel.user, sampleUser);
  });

  test('reports an unconfigured chicken coop', () async {
    final viewModel = build(
      FakeUtenteRepository(
        onGetAreaUtente: () async =>
            throw const PollaioNonConfiguratoException(),
      ),
    );

    await viewModel.load();

    expect(viewModel.status, AreaUtenteStatus.pollaioNonConfigurato);
  });

  test('an expired token sends the user back to login', () async {
    final repository = FakeUtenteRepository(
      onGetAreaUtente: () async => throw const UnauthorizedException(),
    );
    final auth = AuthViewModel(repository);
    final viewModel = AreaUtenteViewModel(repository: repository, auth: auth);

    await viewModel.load();

    expect(auth.status, AuthStatus.unauthenticated);
    expect(auth.notice, isNotNull);
  });

  test('other errors are shown with a retry state', () async {
    final viewModel = build(
      FakeUtenteRepository(
        onGetAreaUtente: () async => throw const NetworkException(),
      ),
    );

    await viewModel.load();

    expect(viewModel.status, AreaUtenteStatus.failure);
    expect(viewModel.errorMessage, const NetworkException().message);
  });
}
