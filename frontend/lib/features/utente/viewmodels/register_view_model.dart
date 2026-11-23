import '../../../core/network/api_exception.dart';
import '../../../core/utils/validators.dart';
import '../../../core/viewmodel/safe_change_notifier.dart';
import '../data/models/register_request.dart';
import 'auth_view_model.dart';

/// Logica della schermata di registrazione.
class RegisterViewModel extends SafeChangeNotifier {
  RegisterViewModel(this._auth);

  final AuthViewModel _auth;

  bool _isLoading = false;
  bool _obscurePassword = true;
  bool _hasRasvsl = false;
  String? _errorMessage;

  bool get isLoading => _isLoading;
  bool get obscurePassword => _obscurePassword;
  bool get hasRasvsl => _hasRasvsl;
  String? get errorMessage => _errorMessage;

  String? validateFirstName(String? value) => Validators.firstName(value);
  String? validateLastName(String? value) => Validators.lastName(value);
  String? validatePhoneNumber(String? value) => Validators.phoneNumber(value);
  String? validatePIva(String? value) => Validators.pIva(value);
  String? validateEmail(String? value) => Validators.email(value);
  String? validatePassword(String? value) => Validators.newPassword(value);

  void togglePasswordVisibility() {
    _obscurePassword = !_obscurePassword;
    notifyListeners();
  }

  void setHasRasvsl(bool value) {
    _hasRasvsl = value;
    notifyListeners();
  }

  /// Registra l'utente. Il backend restituisce subito il token JWT,
  /// quindi in caso di successo l'utente risulta già autenticato.
  Future<bool> submit({
    required String nome,
    required String cognome,
    required String phoneNumber,
    required String pIva,
    required String email,
    required String password,
  }) async {
    if (_isLoading) return false;

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      await _auth.register(
        RegisterRequest(
          nome: nome.trim(),
          cognome: cognome.trim(),
          phoneNumber: phoneNumber.trim(),
          rasvsl: _hasRasvsl,
          pIva: pIva.trim(),
          email: email.trim(),
          password: password,
        ),
      );
      return true;
    } on NetworkException catch (e) {
      _errorMessage = e.message;
      return false;
    } on ApiException {
      // Il backend non distingue le cause (es. email o P.IVA già usate):
      // risponde con un errore generico a body vuoto.
      _errorMessage =
          'We could not complete your registration. Check that your email '
          'and P.IVA are not already registered and try again.';
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
