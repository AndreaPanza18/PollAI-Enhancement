import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/core/network/api_client.dart';
import 'package:frontend/core/network/api_exception.dart';
import 'package:frontend/core/storage/token_storage.dart';
import 'package:frontend/features/salute/data/salute_api_service.dart';
import 'package:frontend/features/salute/data/salute_repository.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

const _accessoBody = '''
{
  "utente": {"id": 1, "nome": "Mario", "cognome": "Rossi", "phoneNumber": "123",
             "rasvsl": true, "pIVA": "IT123", "email": "mario@example.com"},
  "galline": [{"id": 3, "razza": "Livorno", "eta": 2, "peso": 1800}]
}
''';

const _problemBody = '''
{"type": "about:blank", "title": "Conflict", "status": 409,
 "detail": "Pollaio non configurato", "instance": "/api/v1/salute/accesso-salute"}
''';

class _InMemoryTokenStorage implements TokenStorage {
  AuthToken? _token;

  @override
  Future<AuthToken?> read() async => _token;

  @override
  Future<void> write(AuthToken token) async {
    _token = token;
  }

  @override
  Future<void> clear() async {
    _token = null;
  }
}

AuthToken _validToken(String value) => AuthToken(
  value: value,
  expiresAt: DateTime.now().add(const Duration(hours: 1)),
);

http.Response _json(String body, int status) => http.Response(
  body,
  status,
  headers: {'content-type': 'application/json; charset=utf-8'},
);

void main() {
  SaluteRepositoryImpl build(
      _InMemoryTokenStorage storage,
      Future<http.Response> Function(http.Request request) handler,
      ) {
    return SaluteRepositoryImpl(
      api: SaluteApiService(
        ApiClient(baseUrl: 'http://test', httpClient: MockClient(handler)),
      ),
      tokenStorage: storage,
    );
  }

  test('uses the token currently in storage, never a stale copy', () async {
    final storage = _InMemoryTokenStorage();
    final sentTokens = <String?>[];
    final repository = build(storage, (request) async {
      sentTokens.add(request.headers['Authorization']);
      return _json(_accessoBody, 200);
    });

    await storage.write(_validToken('token-A'));
    await repository.getGalline();

    // Logout: senza token la chiamata deve essere rifiutata.
    await storage.clear();
    await expectLater(
      repository.getGalline(),
      throwsA(isA<UnauthorizedException>()),
    );

    // Nuovo login (anche di un altro utente): deve valere il token nuovo.
    await storage.write(_validToken('token-B'));
    await repository.getGalline();

    expect(sentTokens, ['Bearer token-A', 'Bearer token-B']);
  });

  test('an expired token is rejected without calling the server', () async {
    final storage = _InMemoryTokenStorage();
    var calls = 0;
    final repository = build(storage, (request) async {
      calls++;
      return _json(_accessoBody, 200);
    });

    await storage.write(
      AuthToken(
        value: 'old',
        expiresAt: DateTime.now().subtract(const Duration(minutes: 1)),
      ),
    );

    await expectLater(
      repository.getGalline(),
      throwsA(isA<UnauthorizedException>()),
    );
    expect(calls, 0);
  });

  test('returns the hens from the typed response', () async {
    final storage = _InMemoryTokenStorage();
    final repository = build(storage, (request) async => _json(_accessoBody, 200));
    await storage.write(_validToken('t'));

    final galline = await repository.getGalline();

    expect(galline, hasLength(1));
    expect(galline.single.razza, 'Livorno');
  });

  test('a 409 ProblemDetail becomes PollaioNonConfiguratoException', () async {
    final storage = _InMemoryTokenStorage();
    final repository = build(storage, (request) async => _json(_problemBody, 409));
    await storage.write(_validToken('t'));

    await expectLater(
      repository.getGalline(),
      throwsA(isA<PollaioNonConfiguratoException>()),
    );
  });
}