import 'package:flutter_test/flutter_test.dart';
import 'package:frontend/core/utils/validators.dart';

void main() {
  group('Validators.email', () {
    test('accepts a well-formed address', () {
      expect(Validators.email('mario.rossi@example.com'), isNull);
    });

    test('rejects empty and malformed addresses', () {
      expect(Validators.email(''), isNotNull);
      expect(Validators.email('mario.rossi'), isNotNull);
      expect(Validators.email('mario@example'), isNotNull);
    });
  });

  group('Validators.newPassword', () {
    test('accepts a password that satisfies the policy', () {
      expect(Validators.newPassword('Passw0rd!'), isNull);
    });

    test('rejects passwords missing a required character class', () {
      expect(Validators.newPassword('password1!'), isNotNull); // no uppercase
      expect(Validators.newPassword('PASSWORD1!'), isNotNull); // no lowercase
      expect(Validators.newPassword('Password!!'), isNotNull); // no digit
      expect(Validators.newPassword('Password12'), isNotNull); // no symbol
      expect(Validators.newPassword('Pa1!'), isNotNull); // too short
    });
  });

  group('Validators.loginPassword', () {
    test('only requires a value', () {
      expect(Validators.loginPassword('anything'), isNull);
      expect(Validators.loginPassword(''), isNotNull);
    });
  });

  group('Validators.pIva', () {
    test('requires IT followed by 11 digits', () {
      expect(Validators.pIva('IT12345678901'), isNull);
      expect(Validators.pIva('12345678901'), isNotNull);
      expect(Validators.pIva('IT1234567890'), isNotNull);
    });
  });

  group('Validators.phoneNumber', () {
    test('accepts common formats and rejects letters', () {
      expect(Validators.phoneNumber('+39 3331234567'), isNull);
      expect(Validators.phoneNumber('3331234567'), isNull);
      expect(Validators.phoneNumber('abc'), isNotNull);
    });
  });

  group('Validators.firstName / lastName', () {
    test('enforce the 20 characters limit of the database columns', () {
      expect(Validators.firstName('Mario'), isNull);
      expect(Validators.firstName('a' * 21), isNotNull);
      expect(Validators.lastName(' '), isNotNull);
    });
  });
}
