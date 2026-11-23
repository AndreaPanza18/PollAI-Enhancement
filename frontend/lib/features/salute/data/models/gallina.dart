/// Rappresenta una gallina del pollaio, mappata dall'entity backend `Gallina`.
///
/// I campi `id`, `razza`, `eta` e `peso` provengono dal database;
/// `temperatura` e `mangime` sono campi transitori calcolati dal backend.
class Gallina {
  const Gallina({
    required this.id,
    required this.razza,
    required this.eta,
    required this.peso,
    this.temperatura,
    this.mangime,
  });

  final int id;
  final String razza;
  final int eta;
  final int peso;
  final double? temperatura;
  final String? mangime;

  factory Gallina.fromJson(Map<String, dynamic> json) {
    return Gallina(
      id: (json['id'] as num).toInt(),
      razza: json['razza'] as String? ?? '',
      eta: (json['eta'] as num).toInt(),
      peso: (json['peso'] as num).toInt(),
      temperatura: json['temperatura'] != null
          ? (json['temperatura'] as num).toDouble()
          : null,
      mangime: json['mangime'] as String?,
    );
  }

  /// Restituisce il peso formattato in grammi.
  String get pesoFormattato => '$peso g';

  /// Restituisce la temperatura formattata in gradi Celsius.
  String? get temperaturaFormattata => temperatura != null ? '${temperatura}°C' : null;

  @override
  bool operator ==(Object other) =>
      other is Gallina &&
      other.id == id &&
      other.razza == razza &&
      other.eta == eta &&
      other.peso == peso &&
      other.temperatura == temperatura &&
      other.mangime == mangime;

  @override
  int get hashCode =>
      Object.hash(id, razza, eta, peso, temperatura, mangime);
}
