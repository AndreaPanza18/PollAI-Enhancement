/// Configurazione dell'API del backend PollAI.
///
/// `API_BASE_URL` si imposta in fase di build/run con
/// `--dart-define=API_BASE_URL=...` (lo stesso parametro usato dal
/// `Dockerfile` e da `docker-compose.yml`).
///
/// Nota: su emulatore Android `localhost` punta all'emulatore stesso,
/// usare `http://10.0.2.2:8060` per raggiungere il backend sul PC.
abstract final class ApiConfig {
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8060',
  );

  /// Prefisso degli endpoint del package Utente (`UtenteController`).
  static const String userApiPath = '/api/v1/user';

  /// Prefisso degli endpoint del package Salute (`SaluteController`).
  static const String saluteApiPath = '/api/v1/salute';
}
