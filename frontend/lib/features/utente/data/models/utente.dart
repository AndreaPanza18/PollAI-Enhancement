/// Dati pubblici dell'utente (`UtenteResponse` del backend).
class Utente {
  const Utente({
    required this.id,
    required this.nome,
    required this.cognome,
    required this.phoneNumber,
    required this.rasvsl,
    required this.pIva,
    required this.email,
  });

  final int id;
  final String nome;
  final String cognome;
  final String phoneNumber;
  final bool rasvsl;
  final String? pIva;
  final String email;

  factory Utente.fromJson(Map<String, dynamic> json) {
    return Utente(
      id: (json['id'] as num).toInt(),
      nome: json['nome'] as String? ?? '',
      cognome: json['cognome'] as String? ?? '',
      phoneNumber: json['phoneNumber'] as String? ?? '',
      rasvsl: json['rasvsl'] as bool? ?? false,
      pIva: json['pIVA'] as String?,
      email: json['email'] as String? ?? '',
    );
  }

  String get fullName => '$nome $cognome'.trim();

  /// Iniziali per l'avatar (come `profile-initials` nella userbar web).
  String get initials {
    final first = nome.trim().isEmpty ? '' : nome.trim()[0];
    final last = cognome.trim().isEmpty ? '' : cognome.trim()[0];
    final initials = '$first$last'.toUpperCase();
    return initials.isEmpty ? '?' : initials;
  }

  @override
  bool operator ==(Object other) =>
      other is Utente &&
      other.id == id &&
      other.nome == nome &&
      other.cognome == cognome &&
      other.phoneNumber == phoneNumber &&
      other.rasvsl == rasvsl &&
      other.pIva == pIva &&
      other.email == email;

  @override
  int get hashCode =>
      Object.hash(id, nome, cognome, phoneNumber, rasvsl, pIva, email);
}
