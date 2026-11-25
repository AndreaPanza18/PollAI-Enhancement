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
/// Allega il token JWT (letto dallo storage condiviso) alle chiamate protette.
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

  @override
  Future<List<Gallina>> getGalline() async {
    final token = await _requireToken();
    try {
      final response = await _api.accessoSalute(token);
      return response.galline;
    } on ConflictException {
      throw const PollaioNonConfiguratoException();
    }
  }

  @override
  Future<SaluteConsiglioResponse> richiediConsiglio(int idGallina) async {
    final token = await _requireToken();
    return _api.richiediConsiglio(idGallina: idGallina, token: token);
  }

  /// Legge sempre il token dallo storage condiviso, senza copie locali:
  /// dopo un logout o un nuovo login vale subito il token aggiornato.
  Future<String> _requireToken() async {
    final token = await _storage.read();
    if (token == null || token.isExpired) {
      throw const UnauthorizedException('Your session has expired.');
    }
    return token.value;
  }
}