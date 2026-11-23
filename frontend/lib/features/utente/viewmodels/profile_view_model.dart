import '../../../core/network/api_exception.dart';
import '../../../core/viewmodel/safe_change_notifier.dart';
import '../../../core/viewmodel/view_status.dart';
import '../data/models/utente.dart';
import '../data/utente_repository.dart';
import 'auth_view_model.dart';

/// Logica della schermata Profilo (`GET /profilo`).
class ProfileViewModel extends SafeChangeNotifier {
  ProfileViewModel({
    required this._repository,
    required this._auth,
  });

  final UtenteRepository _repository;
  final AuthViewModel _auth;

  ViewStatus _status = ViewStatus.loading;
  Utente? _user;
  String? _errorMessage;

  ViewStatus get status => _status;
  Utente? get user => _user;
  String? get errorMessage => _errorMessage;

  Future<void> load() async {
    _status = ViewStatus.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      final user = await _repository.getProfilo();
      _user = user;
      _status = ViewStatus.success;
      _auth.updateUser(user);
    } on UnauthorizedException {
      await _auth.onSessionExpired();
      return;
    } on ApiException catch (e) {
      _status = ViewStatus.failure;
      _errorMessage = e.message;
    }
    notifyListeners();
  }

  Future<void> logout() => _auth.logout();
}
