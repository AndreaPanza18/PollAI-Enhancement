import 'package:material_ui/material_ui.dart';

import 'app.dart';
import 'core/config/api_config.dart';
import 'core/network/api_client.dart';
import 'core/storage/token_storage.dart';
import 'features/utente/data/utente_api_service.dart';
import 'features/utente/data/utente_repository.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // Composizione delle dipendenze (nessun service locator: basta qui).
  final apiClient = ApiClient(baseUrl: ApiConfig.baseUrl);
  final repository = UtenteRepositoryImpl(
    api: UtenteApiService(apiClient),
    tokenStorage: SecureTokenStorage(),
  );

  runApp(PollAIApp(repository: repository));
}
