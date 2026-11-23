import 'package:material_ui/material_ui.dart';

import 'app.dart';
import 'core/config/api_config.dart';
import 'core/network/api_client.dart';
import 'core/storage/token_storage.dart';
import 'features/salute/data/salute_api_service.dart';
import 'features/salute/data/salute_repository.dart';
import 'features/utente/data/utente_api_service.dart';
import 'features/utente/data/utente_repository.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // Composizione delle dipendenze (nessun service locator: basta qui).
  final apiClient = ApiClient(baseUrl: ApiConfig.baseUrl);
  final tokenStorage = SecureTokenStorage();

  final utenteRepository = UtenteRepositoryImpl(
    api: UtenteApiService(apiClient),
    tokenStorage: tokenStorage,
  );

  final saluteRepository = SaluteRepositoryImpl(
    api: SaluteApiService(apiClient),
    tokenStorage: tokenStorage,
  );

  runApp(PollAIApp(
    utenteRepository: utenteRepository,
    saluteRepository: saluteRepository,
  ));
}
