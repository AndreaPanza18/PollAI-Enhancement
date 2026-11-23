/// Risposta di `GET /accesso-salute`: contiene l'utente e la lista delle galline.
///
/// Il backend restituisce un JSON con le chiavi `utente` e `galline`.
class SaluteAccessoResponse {
  const SaluteAccessoResponse({
    required this.utente,
    required this.galline,
  });

  final Map<String, dynamic> utente;
  final List<dynamic> galline;

  factory SaluteAccessoResponse.fromJson(Map<String, dynamic> json) {
    return SaluteAccessoResponse(
      utente: json['utente'] as Map<String, dynamic> ?? {},
      galline: json['galline'] as List<dynamic> ?? [],
    );
  }
}
