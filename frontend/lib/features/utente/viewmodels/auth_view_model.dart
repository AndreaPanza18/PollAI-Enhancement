import '../../../core/network/api_exception.dart';
import '../../../core/viewmodel/safe_change_notifier.dart';
import '../data/models/login_response.dart';
import '../data/models/register_request.dart';
import '../data/models/utente.dart';
import '../data/utente_repository.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

/// Stato di sessione dell'intera app: decide se mostrare le schermate
/// di accesso o l'area utente. I ViewModel dei singoli form lo usano
/// per effettuare login, registrazione e logout.
class AuthViewModel extends SafeChangeNotifier {
  AuthViewModel(this._repository);

  final UtenteRepository _repository;

  AuthStatus _status = AuthStatus.unknown;
  Utente? _user;
  bool _pollaioConfigurato = false;
  String? _notice;

  AuthStatus get status => _status;
  Utente? get user => _user;

  /// Valore di `pollaioConfigurato` restituito dall'ultimo login/registrazione.
  /// La fonte di verità resta `GET /area-utente` (409 se non configurato).
  bool get pollaioConfigurato => _pollaioConfigurato;

  /// Messaggio informativo da mostrare una volta nella schermata di login
  /// (es. sessione scaduta).
  String? get notice => _notice;

  /// Legge e azzera il messaggio informativo.
  String? takeNotice() {
    final value = _notice;
    _notice = null;
    return value;
  }

  /// Da chiamare all'avvio: ripristina la sessione salvata, se valida.
  Future<void> restoreSession() async {
    try {
      final user = await _repository.restoreSession();
      if (user == null) {
        _status = AuthStatus.unauthenticated;
      } else {
        _user = user;
        _status = AuthStatus.authenticated;
      }
    } on ApiException catch (e) {
      // Server non raggiungibile all'avvio: si passa al login, il token
      // salvato non viene toccato.
      _notice = e.message;
      _status = AuthStatus.unauthenticated;
    }
    notifyListeners();
  }

  /// Lancia [ApiException] in caso di errore (gestita dal ViewModel chiamante).
  Future<void> login({required String email, required String password}) async {
    final response = await _repository.login(email: email, password: password);
    _onAuthenticated(response);
  }

  /// Lancia [ApiException] in caso di errore (gestita dal ViewModel chiamante).
  Future<void> register(RegisterRequest request) async {
    final response = await _repository.register(request);
    _onAuthenticated(response);
  }

  Future<void> logout() async {
    await _repository.logout();
    _user = null;
    _pollaioConfigurato = false;
    _notice = null;
    _status = AuthStatus.unauthenticated;
    notifyListeners();
  }

  /// Il backend ha risposto 401 a una chiamata protetta: si torna al login.
  Future<void> onSessionExpired() async {
    await _repository.logout();
    _user = null;
    _pollaioConfigurato = false;
    _notice = 'Your session has expired. Please sign in again.';
    _status = AuthStatus.unauthenticated;
    notifyListeners();
  }

  /// Aggiorna i dati utente mostrati nell'app (es. dopo `GET /profilo`).
  void updateUser(Utente user) {
    if (_user == user) return;
    _user = user;
    notifyListeners();
  }

  void _onAuthenticated(LoginResponse response) {
    _user = response.user;
    _pollaioConfigurato = response.pollaioConfigurato;
    _notice = null;
    _status = AuthStatus.authenticated;
    notifyListeners();
  }
}
