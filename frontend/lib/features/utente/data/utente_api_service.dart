import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import 'models/login_response.dart';
import 'models/register_request.dart';
import 'models/utente.dart';

/// Chiamate agli endpoint di `UtenteController` (`/api/v1/user`).
/// Si limita a (de)serializzare: la logica di sessione sta nel repository.
class UtenteApiService {
  UtenteApiService(this._client);

  final ApiClient _client;

  static const String _base = ApiConfig.userApiPath;

  /// `POST /login` — 200 con [LoginResponse], 401 se le credenziali sono errate.
  Future<LoginResponse> login({
    required String email,
    required String password,
  }) async {
    final json = await _client.post(
      '$_base/login',
      body: {'email': email, 'password': password},
    );
    return _parse(json, LoginResponse.fromJson);
  }

  /// `POST /register` — 201 con [LoginResponse] (utente già autenticato).
  Future<LoginResponse> register(RegisterRequest request) async {
    final json = await _client.post('$_base/register', body: request.toJson());
    return _parse(json, LoginResponse.fromJson);
  }

  /// `GET /profilo` — richiede il Bearer token.
  Future<Utente> getProfilo(String token) async {
    final json = await _client.get('$_base/profilo', bearerToken: token);
    return _parse(json, Utente.fromJson);
  }

  /// `GET /area-utente` — richiede il Bearer token; 409 se il pollaio
  /// non è configurato.
  Future<Utente> getAreaUtente(String token) async {
    final json = await _client.get('$_base/area-utente', bearerToken: token);
    return _parse(json, Utente.fromJson);
  }

  T _parse<T>(Object? json, T Function(Map<String, dynamic>) fromJson) {
    const error = ApiException('Unexpected response from the server.');
    if (json is! Map<String, dynamic>) throw error;
    try {
      return fromJson(json);
    } on TypeError {
      // Campo mancante o di tipo inatteso nel JSON.
      throw error;
    }
  }
}
