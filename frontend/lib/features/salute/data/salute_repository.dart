import '../../../core/network/api_exception.dart';
import '../../../core/storage/token_storage.dart';
import 'models/gallina.dart';
import 'models/salute_consiglio_response.dart';
import 'salute_api_service.dart';

/// `GET /accesso-salute` ha risposto 409: l'utente non ha ancora un pollaio
/// configurato (con almeno una gallina).
class PollaioNonConfiguratoException implements Exception {
  const PollaioNonConfiguratoException();
}

/// Punto di accesso ai dati del package Salute per i ViewModel.
/// Gestisce il token JWT allegandolo alle chiamate protette.
abstract class SaluteRepository {
  /// Restituisce la lista delle galline dell'utente autenticato.
  /// Lancia [UnauthorizedException] se manca il token o è scaduto.
  /// Lancia [PollaioNonConfiguratoException] se il pollaio non è configurato.
  Future<List<Gallina>> getGalline();

  /// Richiede un consiglio sulla salute di una gallina specifica.
  /// Lancia [UnauthorizedException] se manca il token o è scaduto.
  /// Lancia [NotFoundException] se la gallina non esiste.
  Future<SaluteConsiglioResponse> richiediConsiglio(int idGallina);
}

class SaluteRepositoryImpl implements SaluteRepository {
  SaluteRepositoryImpl({
    required this._api,
    required TokenStorage tokenStorage,
  }) : _storage = tokenStorage;

  final SaluteApiService _api;
  final TokenStorage _storage;

  AuthToken? _token;

  @override
  Future<List<Gallina>> getGalline() async {
    final token = await _requireToken();
    try {
      final response = await _api.accessoSalute(token);
      return _api.parseGalline(response.galline ?? []);
    } on ConflictException {
      throw const PollaioNonConfiguratoException();
    }
  }

  @override
  Future<SaluteConsiglioResponse> richiediConsiglio(int idGallina) async {
    final token = await _requireToken();
    return _api.richiediConsiglio(idGallina: idGallina, token: token);
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
