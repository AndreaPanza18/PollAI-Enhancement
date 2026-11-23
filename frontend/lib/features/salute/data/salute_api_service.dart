import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import 'models/gallina.dart';
import 'models/salute_accesso_response.dart';
import 'models/salute_consiglio_response.dart';

/// Chiamate agli endpoint di `SaluteController` (`/api/v1/salute`).
/// Si limita a (de)serializzare: la logica di sessione sta nel repository.
class SaluteApiService {
  SaluteApiService(this._client);

  final ApiClient _client;

  static const String _base = ApiConfig.saluteApiPath;

  /// `GET /accesso-salute` — 200 con la lista delle galline,
  /// 409 se il pollaio non è configurato.
  Future<SaluteAccessoResponse> accessoSalute(String token) async {
    final json = await _client.get('$_base/accesso-salute', bearerToken: token);
    return _parse(json, SaluteAccessoResponse.fromJson);
  }

  /// `POST /consiglio/{idGallina}` — 200 con il messaggio di salute,
  /// 404 se la gallina non esiste.
  Future<SaluteConsiglioResponse> richiediConsiglio({
    required int idGallina,
    required String token,
  }) async {
    final json = await _client.post(
      '$_base/consiglio/$idGallina',
      bearerToken: token,
    );
    return _parse(json, SaluteConsiglioResponse.fromJson);
  }

  /// Converte la lista grezza di JSON in oggetti [Gallina].
  List<Gallina> parseGalline(List<dynamic> rawList) {
    const error = ApiException('Unexpected response from the server.');
    try {
      return rawList.map((item) {
        if (item is! Map<String, dynamic>) throw error;
        return Gallina.fromJson(item);
      }).toList();
    } on TypeError {
      throw error;
    }
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
