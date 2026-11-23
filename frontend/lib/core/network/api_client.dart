import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import 'api_exception.dart';

/// Client HTTP generico: costruisce gli URL, aggiunge gli header
/// (incluso il Bearer token), decodifica il JSON e traduce gli errori
/// in [ApiException]. Non conosce nessun endpoint specifico.
class ApiClient {
  ApiClient({
    required String baseUrl,
    http.Client? httpClient,
    this._timeout = const Duration(seconds: 15),
  }) : _baseUrl = baseUrl.endsWith('/')
           ? baseUrl.substring(0, baseUrl.length - 1)
           : baseUrl,
       _http = httpClient ?? http.Client();

  final String _baseUrl;
  final http.Client _http;
  final Duration _timeout;

  Future<Object?> get(String path, {String? bearerToken}) {
    return _send(
      () => _http.get(_uri(path), headers: _headers(bearerToken: bearerToken)),
    );
  }

  Future<Object?> post(String path, {Object? body, String? bearerToken}) {
    return _send(
      () => _http.post(
        _uri(path),
        headers: _headers(bearerToken: bearerToken, hasJsonBody: body != null),
        body: body == null ? null : jsonEncode(body),
      ),
    );
  }

  Uri _uri(String path) => Uri.parse('$_baseUrl$path');

  Map<String, String> _headers({
    String? bearerToken,
    bool hasJsonBody = false,
  }) {
    return {
      'Accept': 'application/json',
      // Il Content-Type solo quando serve evita preflight CORS inutili sulle GET.
      if (hasJsonBody) 'Content-Type': 'application/json; charset=utf-8',
      // Il backend accetta solo lo schema "Bearer " (vedi extractBearerToken).
      if (bearerToken != null) 'Authorization': 'Bearer $bearerToken',
    };
  }

  Future<Object?> _send(Future<http.Response> Function() request) async {
    final http.Response response;
    try {
      response = await request().timeout(_timeout);
    } on TimeoutException {
      throw const NetworkException(
        'The server took too long to respond. Please try again.',
      );
    } catch (_) {
      // Su web e mobile le eccezioni di rete sono diverse: le uniamo qui.
      throw const NetworkException();
    }
    return _handle(response);
  }

  Object? _handle(http.Response response) {
    final status = response.statusCode;

    if (status >= 200 && status < 300) {
      if (response.bodyBytes.isEmpty) return null;
      try {
        return jsonDecode(utf8.decode(response.bodyBytes));
      } on FormatException {
        throw ApiException(
          'Unexpected response from the server.',
          statusCode: status,
        );
      }
    }

    throw switch (status) {
      400 => const BadRequestException(),
      401 => const UnauthorizedException(),
      404 => const NotFoundException(),
      409 => const ConflictException(),
      >= 500 => ServerException(
        'The server had a problem. Please try again later.',
        status,
      ),
      _ => ApiException('Unexpected error (HTTP $status).', statusCode: status),
    };
  }

  void close() => _http.close();
}
