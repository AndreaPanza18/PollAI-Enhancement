import '../../../utente/data/models/utente.dart';
import 'gallina.dart';

/// Risposta di `GET /accesso-salute` (`AccessoSaluteResponse` del backend):
/// l'utente autenticato e le galline del suo pollaio.
class SaluteAccessoResponse {
  const SaluteAccessoResponse({
    required this.utente,
    required this.galline,
  });

  final Utente utente;
  final List<Gallina> galline;

  factory SaluteAccessoResponse.fromJson(Map<String, dynamic> json) {
    return SaluteAccessoResponse(
      utente: Utente.fromJson(json['utente'] as Map<String, dynamic>),
      galline: (json['galline'] as List<dynamic>)
          .map((item) => Gallina.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}