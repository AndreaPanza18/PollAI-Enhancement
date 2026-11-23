import '../../../core/network/api_exception.dart';
import '../../../core/utils/validators.dart';
import '../../../core/viewmodel/safe_change_notifier.dart';
import 'auth_view_model.dart';

/// Logica della schermata di login.
class LoginViewModel extends SafeChangeNotifier {
  LoginViewModel(this._auth) : _notice = _auth.takeNotice();

  final AuthViewModel _auth;

  bool _isLoading = false;
  bool _obscurePassword = true;
  String? _errorMessage;
  String? _notice;

  bool get isLoading => _isLoading;
  bool get obscurePassword => _obscurePassword;
  String? get errorMessage => _errorMessage;
  String? get notice => _notice;

  String? validateEmail(String? value) => Validators.email(value);
  String? validatePassword(String? value) => Validators.loginPassword(value);

  void togglePasswordVisibility() {
    _obscurePassword = !_obscurePassword;
    notifyListeners();
  }

  /// Esegue il login. In caso di successo [AuthViewModel] passa a
  /// `authenticated` e la UI cambia schermata da sola.
  Future<bool> submit({required String email, required String password}) async {
    if (_isLoading) return false;

    _isLoading = true;
    _errorMessage = null;
    _notice = null;
    notifyListeners();

    try {
      await _auth.login(email: email.trim(), password: password);
      return true;
    } on UnauthorizedException {
      _errorMessage = 'Invalid login. Please try again.';
      return false;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
