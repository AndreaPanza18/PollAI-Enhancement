import '../../../core/network/api_exception.dart';
import '../../../core/storage/token_storage.dart';
import 'models/login_response.dart';
import 'models/register_request.dart';
import 'models/utente.dart';
import 'utente_api_service.dart';

/// `GET /area-utente` ha risposto 409: l'utente non ha ancora un pollaio
/// configurato (con almeno una gallina).
class PollaioNonConfiguratoException implements Exception {
  const PollaioNonConfiguratoException();
}

/// Punto di accesso ai dati del package Utente per i ViewModel.
/// Gestisce anche il token JWT: lo salva dopo login/registrazione, lo
/// allega alle chiamate protette e lo cancella al logout.
abstract class UtenteRepository {
  /// Ripristina la sessione salvata. `null` se non c'è o non è più valida.
  Future<Utente?> restoreSession();

  Future<LoginResponse> login({
    required String email,
    required String password,
  });

  Future<LoginResponse> register(RegisterRequest request);

  /// Lancia [UnauthorizedException] se manca il token o è scaduto/rifiutato.
  Future<Utente> getProfilo();

  /// Come [getProfilo], più [PollaioNonConfiguratoException] se il pollaio
  /// non è configurato.
  Future<Utente> getAreaUtente();

  Future<void> logout();
}

class UtenteRepositoryImpl implements UtenteRepository {
  UtenteRepositoryImpl({
    required this._api,
    required TokenStorage tokenStorage,
  }) : _storage = tokenStorage;

  final UtenteApiService _api;
  final TokenStorage _storage;

  AuthToken? _token;

  @override
  Future<Utente?> restoreSession() async {
    final stored = await _storage.read();
    if (stored == null) return null;
    if (stored.isExpired) {
      await _storage.clear();
      return null;
    }
    try {
      final user = await _api.getProfilo(stored.value);
      _token = stored;
      return user;
    } on UnauthorizedException {
      await _storage.clear();
      return null;
    }
    // Gli altri errori (rete, server) si propagano: il token resta salvato
    // e verrà riprovato al prossimo avvio.
  }

  @override
  Future<LoginResponse> login({
    required String email,
    required String password,
  }) async {
    final response = await _api.login(email: email, password: password);
    await _saveSession(response);
    return response;
  }

  @override
  Future<LoginResponse> register(RegisterRequest request) async {
    final response = await _api.register(request);
    await _saveSession(response);
    return response;
  }

  @override
  Future<Utente> getProfilo() async {
    final token = await _requireToken();
    return _api.getProfilo(token);
  }

  @override
  Future<Utente> getAreaUtente() async {
    final token = await _requireToken();
    try {
      return await _api.getAreaUtente(token);
    } on ConflictException {
      throw const PollaioNonConfiguratoException();
    }
  }

  @override
  Future<void> logout() async {
    _token = null;
    await _storage.clear();
  }

  Future<void> _saveSession(LoginResponse response) async {
    final token = AuthToken(
      value: response.accessToken,
      expiresAt: DateTime.now().add(Duration(seconds: response.expiresIn)),
    );
    _token = token;
    await _storage.write(token);
  }

  Future<String> _requireToken() async {
    final token = _token ?? await _storage.read();
    if (token == null || token.isExpired) {
      throw const UnauthorizedException('Your session has expired.');
    }
    _token = token;
    return token.value;
  }
}
