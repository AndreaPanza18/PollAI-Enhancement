import 'utente.dart';

/// Risposta di `POST /login` e `POST /register` (`LoginResponse`).
class LoginResponse {
  const LoginResponse({
    required this.accessToken,
    required this.tokenType,
    required this.expiresIn,
    required this.user,
    required this.pollaioConfigurato,
  });

  final String accessToken;
  final String tokenType;

  /// Durata del token in secondi.
  final int expiresIn;
  final Utente user;
  final bool pollaioConfigurato;

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      accessToken: json['accessToken'] as String,
      tokenType: json['tokenType'] as String? ?? 'Bearer',
      expiresIn: (json['expiresIn'] as num).toInt(),
      user: Utente.fromJson(json['user'] as Map<String, dynamic>),
      pollaioConfigurato: json['pollaioConfigurato'] as bool? ?? false,
    );
  }
}
