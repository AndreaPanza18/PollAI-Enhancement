/// Risposta di `POST /consiglio/{idGallina}`: contiene il messaggio di salute.
///
/// Il backend restituisce un JSON con la chiave `message`.
class SaluteConsiglioResponse {
  const SaluteConsiglioResponse({required this.message});

  final String message;

  factory SaluteConsiglioResponse.fromJson(Map<String, dynamic> json) {
    return SaluteConsiglioResponse(
      message: json['message'] as String? ?? '',
    );
  }

  /// Restituisce `true` se il messaggio indica che la gallina non è in buona salute.
  bool get isUnhealthy => message.toLowerCase().contains('not');
}
