/// Regole di validazione dei form, le stesse del frontend HTTP
/// (`check.js`, `checkLogin.js` e gli `pattern` dei template Thymeleaf).
///
/// Ogni metodo restituisce `null` se il valore è valido, altrimenti il
/// messaggio d'errore da mostrare (compatibile con `FormField.validator`).
abstract final class Validators {
  static final RegExp _email = RegExp(
    r'^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,10}$',
  );
  static final RegExp _phone = RegExp(
    r'^\+?\d{1,4}[\s\-]?\(?\d{1,3}\)?[\s\-]?\d{1,4}[\s\-]?\d{1,4}$',
  );
  static final RegExp _pIva = RegExp(r'^IT\d{11}$');
  static final RegExp _password = RegExp(
    r'''^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\[\]{};':"\\|,.<>\/?]).{8,}$''',
  );

  static const int nameMaxLength = 20;
  static const int phoneMaxLength = 20;
  static const int emailMaxLength = 45;
  static const int pIvaMaxLength = 45;

  static String? requiredField(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'This field is required.';
    }
    return null;
  }

  static String? email(String? value) {
    final missing = requiredField(value);
    if (missing != null) return missing;
    final email = value!.trim();
    if (email.length > emailMaxLength || !_email.hasMatch(email)) {
      return 'Invalid email format. Please use a correct email address.';
    }
    return null;
  }

  /// Per il login basta che la password sia presente: la policy di
  /// complessità vale solo in registrazione, così non blocchiamo chi ha
  /// una password creata con regole precedenti.
  static String? loginPassword(String? value) {
    if (value == null || value.isEmpty) return 'This field is required.';
    return null;
  }

  static String? newPassword(String? value) {
    if (value == null || value.isEmpty) return 'This field is required.';
    if (!_password.hasMatch(value)) {
      return 'Password must be at least 8 characters long and contain '
          'uppercase letters, lowercase letters, numbers, and special '
          'characters.';
    }
    return null;
  }

  static String? firstName(String? value) {
    final missing = requiredField(value);
    if (missing != null) return missing;
    if (value!.trim().length > nameMaxLength) {
      return 'The name must be at most $nameMaxLength characters.';
    }
    return null;
  }

  static String? lastName(String? value) {
    final missing = requiredField(value);
    if (missing != null) return missing;
    if (value!.trim().length > nameMaxLength) {
      return 'The last name must be at most $nameMaxLength characters.';
    }
    return null;
  }

  static String? phoneNumber(String? value) {
    final missing = requiredField(value);
    if (missing != null) return missing;
    final phone = value!.trim();
    if (phone.length > phoneMaxLength || !_phone.hasMatch(phone)) {
      return 'Invalid phone number. Please use the correct format.';
    }
    return null;
  }

  static String? pIva(String? value) {
    final missing = requiredField(value);
    if (missing != null) return missing;
    if (!_pIva.hasMatch(value!.trim())) {
      return 'Invalid P.IVA. Please use the correct format (e.g. IT12345678901).';
    }
    return null;
  }
}
