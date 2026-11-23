import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Token JWT con la sua scadenza (calcolata da `expiresIn` del login).
class AuthToken {
  const AuthToken({required this.value, required this.expiresAt});

  final String value;
  final DateTime expiresAt;

  bool get isExpired => DateTime.now().isAfter(expiresAt);
}

/// Persistenza del token tra un avvio e l'altro dell'app.
abstract class TokenStorage {
  Future<AuthToken?> read();
  Future<void> write(AuthToken token);
  Future<void> clear();
}

/// Implementazione basata su Keychain / Keystore (e localStorage su web).
///
/// Gli errori della piattaforma non devono bloccare l'app: in caso di
/// problemi la sessione resta valida solo in memoria.
class SecureTokenStorage implements TokenStorage {
  SecureTokenStorage({FlutterSecureStorage? storage})
    : _storage = storage ?? const FlutterSecureStorage();

  static const _tokenKey = 'pollai.access_token';
  static const _expiresAtKey = 'pollai.expires_at';

  final FlutterSecureStorage _storage;

  @override
  Future<AuthToken?> read() async {
    try {
      final value = await _storage.read(key: _tokenKey);
      final expiresAtRaw = await _storage.read(key: _expiresAtKey);
      if (value == null || expiresAtRaw == null) return null;
      final expiresAt = DateTime.tryParse(expiresAtRaw);
      if (expiresAt == null) return null;
      return AuthToken(value: value, expiresAt: expiresAt);
    } catch (_) {
      return null;
    }
  }

  @override
  Future<void> write(AuthToken token) async {
    try {
      await _storage.write(key: _tokenKey, value: token.value);
      await _storage.write(
        key: _expiresAtKey,
        value: token.expiresAt.toUtc().toIso8601String(),
      );
    } catch (_) {
      // Sessione solo in memoria.
    }
  }

  @override
  Future<void> clear() async {
    try {
      await _storage.delete(key: _tokenKey);
      await _storage.delete(key: _expiresAtKey);
    } catch (_) {
      // Niente da fare: il token verrà comunque scartato alla scadenza.
    }
  }
}
