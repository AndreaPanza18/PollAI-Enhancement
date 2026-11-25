import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/features/salute/data/models/gallina.dart';
import 'package:frontend/features/salute/data/models/salute_accesso_response.dart';
import 'package:frontend/features/salute/data/models/salute_consiglio_response.dart';

Map<String, dynamic> _json(String source) =>
    jsonDecode(source) as Map<String, dynamic>;

void main() {
  group('Gallina', () {
    test('parses a GallinaResponse', () {
      final gallina = Gallina.fromJson(
        _json('{"id": 3, "razza": "Livorno", "eta": 2, "peso": 1800}'),
      );

      expect(gallina.id, 3);
      expect(gallina.razza, 'Livorno');
      expect(gallina.eta, 2);
      expect(gallina.peso, 1800);
      expect(gallina.pesoFormattato, '1800 g');
    });

    test('ignores unknown fields', () {
      final gallina = Gallina.fromJson(
        _json('{"id": 1, "razza": "A", "eta": 1, "peso": 1500, "extra": true}'),
      );

      expect(gallina.id, 1);
    });
  });

  group('SaluteAccessoResponse', () {
    test('parses user and hens', () {
      final response = SaluteAccessoResponse.fromJson(_json('''
        {
          "utente": {
            "id": 1, "nome": "Mario", "cognome": "Rossi",
            "phoneNumber": "123", "rasvsl": true,
            "pIVA": "IT123", "email": "mario@example.com"
          },
          "galline": [
            {"id": 3, "razza": "Livorno", "eta": 2, "peso": 1800},
            {"id": 4, "razza": "Ancona", "eta": 1, "peso": 1600}
          ]
        }
      '''));

      expect(response.utente.nome, 'Mario');
      expect(response.galline, hasLength(2));
      expect(response.galline.last.razza, 'Ancona');
    });

    test('throws a TypeError when galline is missing', () {
      expect(
            () => SaluteAccessoResponse.fromJson(_json('{"utente": {"id": 1}}')),
        throwsA(isA<TypeError>()),
      );
    });
  });

  group('SaluteConsiglioResponse', () {
    test('BUONA is healthy', () {
      final response = SaluteConsiglioResponse.fromJson(
        _json('{"stato": "BUONA", "message": "The hen is in good health."}'),
      );

      expect(response.stato, StatoSalute.buona);
      expect(response.isUnhealthy, isFalse);
      expect(response.message, 'The hen is in good health.');
    });

    test('ATTENZIONE is unhealthy', () {
      final response = SaluteConsiglioResponse.fromJson(
        _json('{"stato": "ATTENZIONE", "message": "The hen is not in good health."}'),
      );

      expect(response.stato, StatoSalute.attenzione);
      expect(response.isUnhealthy, isTrue);
    });

    test('a missing or unknown stato is treated as unhealthy', () {
      expect(
        SaluteConsiglioResponse.fromJson(_json('{"message": "x"}')).isUnhealthy,
        isTrue,
      );
      expect(
        SaluteConsiglioResponse.fromJson(_json('{"stato": "BOH", "message": "x"}'))
            .isUnhealthy,
        isTrue,
      );
    });
  });
}