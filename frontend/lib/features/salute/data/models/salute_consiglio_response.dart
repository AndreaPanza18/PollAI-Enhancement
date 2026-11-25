/// Stato di salute restituito dal backend (`StatoSalute`).
enum StatoSalute {
  buona,
  attenzione;

  /// Un valore mancante o sconosciuto viene trattato come `attenzione`
  /// (scelta prudente: meglio un falso allarme che nascondere un problema).
  static StatoSalute fromJson(String? value) =>
      value == 'BUONA' ? buona : attenzione;
}

/// Risposta di `POST /consiglio/{idGallina}` (`ConsiglioSaluteResponse`).
class SaluteConsiglioResponse {
  const SaluteConsiglioResponse({required this.stato, required this.message});

  final StatoSalute stato;

  /// Testo del consiglio, da mostrare così com'è.
  final String message;

  factory SaluteConsiglioResponse.fromJson(Map<String, dynamic> json) {
    return SaluteConsiglioResponse(
      stato: StatoSalute.fromJson(json['stato'] as String?),
      message: json['message'] as String? ?? '',
    );
  }

  /// `true` se la gallina richiede attenzione.
  bool get isUnhealthy => stato == StatoSalute.attenzione;
}