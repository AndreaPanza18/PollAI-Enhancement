import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/core/network/api_exception.dart';
import 'package:frontend/core/viewmodel/view_status.dart';
import 'package:frontend/features/salute/data/models/gallina.dart';
import 'package:frontend/features/salute/data/models/salute_consiglio_response.dart';
import 'package:frontend/features/salute/data/salute_repository.dart';
import 'package:frontend/features/salute/viewmodels/salute_view_model.dart';
import 'package:frontend/features/utente/viewmodels/auth_view_model.dart';

import '../../helpers/fakes.dart';

const _gallina = Gallina(id: 1, razza: 'Livorno', eta: 2, peso: 1800);

void main() {
  SaluteViewModel build(FakeSaluteRepository repository, {AuthViewModel? auth}) {
    return SaluteViewModel(
      repository: repository,
      auth: auth ?? AuthViewModel(FakeUtenteRepository()),
    );
  }

  group('load', () {
    test('loads the hens', () async {
      final viewModel = build(
        FakeSaluteRepository(onGetGalline: () async => [_gallina]),
      );

      await viewModel.load();

      expect(viewModel.status, ViewStatus.success);
      expect(viewModel.galline, [_gallina]);
    });

    test('reports an unconfigured chicken coop', () async {
      final viewModel = build(
        FakeSaluteRepository(
          onGetGalline: () async => throw const PollaioNonConfiguratoException(),
        ),
      );

      await viewModel.load();

      expect(viewModel.status, ViewStatus.failure);
      expect(viewModel.errorMessage, isNotNull);
    });

    test('an expired token sends the user back to login', () async {
      final auth = AuthViewModel(FakeUtenteRepository());
      final viewModel = build(
        FakeSaluteRepository(
          onGetGalline: () async => throw const UnauthorizedException(),
        ),
        auth: auth,
      );

      await viewModel.load();

      expect(auth.status, AuthStatus.unauthenticated);
      expect(auth.notice, isNotNull);
    });

    test('other errors are shown with a retry state', () async {
      final viewModel = build(
        FakeSaluteRepository(
          onGetGalline: () async => throw const NetworkException(),
        ),
      );

      await viewModel.load();

      expect(viewModel.status, ViewStatus.failure);
      expect(viewModel.errorMessage, const NetworkException().message);
    });
  });

  group('richiediConsiglio', () {
    test('does nothing when no hen is selected', () async {
      var calls = 0;
      final viewModel = build(
        FakeSaluteRepository(
          onRichiediConsiglio: (id) async {
            calls++;
            return const SaluteConsiglioResponse(
              stato: StatoSalute.buona,
              message: 'ok',
            );
          },
        ),
      );

      await viewModel.richiediConsiglio();

      expect(calls, 0);
      expect(viewModel.consiglio, isNull);
    });

    test('loads the advice for the selected hen', () async {
      int? requestedId;
      final viewModel = build(
        FakeSaluteRepository(
          onRichiediConsiglio: (id) async {
            requestedId = id;
            return const SaluteConsiglioResponse(
              stato: StatoSalute.attenzione,
              message: 'The hen is not in good health.',
            );
          },
        ),
      );

      viewModel.selectGallina(_gallina);
      await viewModel.richiediConsiglio();

      expect(requestedId, 1);
      expect(viewModel.consiglioStatus, ViewStatus.success);
      expect(viewModel.consiglio?.isUnhealthy, isTrue);
    });

    test('a missing hen is reported as a failure', () async {
      final viewModel = build(
        FakeSaluteRepository(
          onRichiediConsiglio: (id) async => throw const NotFoundException(),
        ),
      );

      viewModel.selectGallina(_gallina);
      await viewModel.richiediConsiglio();

      expect(viewModel.consiglioStatus, ViewStatus.failure);
      expect(viewModel.errorMessage, const NotFoundException().message);
    });

    test('deselecting clears the advice', () async {
      final viewModel = build(
        FakeSaluteRepository(
          onRichiediConsiglio: (id) async => const SaluteConsiglioResponse(
            stato: StatoSalute.buona,
            message: 'ok',
          ),
        ),
      );

      viewModel.selectGallina(_gallina);
      await viewModel.richiediConsiglio();
      viewModel.deselectGallina();

      expect(viewModel.selectedGallina, isNull);
      expect(viewModel.consiglio, isNull);
    });
  });
}