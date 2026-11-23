/// Errore restituito dal livello di rete, già mappato sullo status HTTP.
///
/// Il backend risponde con body vuoto sugli errori, quindi i messaggi
/// mostrati all'utente sono definiti qui e non letti dalla risposta.
class ApiException implements Exception {
  const ApiException(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

  @override
  String toString() => 'ApiException($statusCode): $message';
}

/// Nessuna risposta dal server (offline, timeout, CORS, DNS...).
class NetworkException extends ApiException {
  const NetworkException([
    super.message =
        'Unable to reach the server. Check your connection and try again.',
  ]);
}

/// 400: richiesta non valida o header Authorization mancante/malformato.
class BadRequestException extends ApiException {
  const BadRequestException([super.message = 'The request was not valid.'])
    : super(statusCode: 400);
}

/// 401: credenziali o token JWT non validi.
class UnauthorizedException extends ApiException {
  const UnauthorizedException([super.message = 'Authentication failed.'])
    : super(statusCode: 401);
}

/// 404: risorsa (es. utente del token) non trovata.
class NotFoundException extends ApiException {
  const NotFoundException([
    super.message = 'The requested item was not found.',
  ]) : super(statusCode: 404);
}

/// 409: conflitto (es. pollaio non configurato su `/area-utente`).
class ConflictException extends ApiException {
  const ConflictException([
    super.message = 'The request could not be completed.',
  ]) : super(statusCode: 409);
}

/// 5xx: errore lato server.
class ServerException extends ApiException {
  const ServerException([
    super.message = 'The server had a problem. Please try again later.',
    int? statusCode,
  ]) : super(statusCode: statusCode ?? 500);
}
