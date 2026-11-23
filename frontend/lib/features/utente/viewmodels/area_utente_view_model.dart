import '../../../core/network/api_exception.dart';
import '../../../core/viewmodel/safe_change_notifier.dart';
import '../data/models/utente.dart';
import '../data/utente_repository.dart';
import 'auth_view_model.dart';

enum AreaUtenteStatus { loading, ready, pollaioNonConfigurato, failure }

/// Logica della schermata Area utente (`GET /area-utente`).
class AreaUtenteViewModel extends SafeChangeNotifier {
  AreaUtenteViewModel({
    required this._repository,
    required this._auth,
  });

  final UtenteRepository _repository;
  final AuthViewModel _auth;

  AreaUtenteStatus _status = AreaUtenteStatus.loading;
  Utente? _user;
  String? _errorMessage;

  AreaUtenteStatus get status => _status;
  Utente? get user => _user;
  String? get errorMessage => _errorMessage;

  Future<void> load() async {
    _status = AreaUtenteStatus.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      final user = await _repository.getAreaUtente();
      _user = user;
      _status = AreaUtenteStatus.ready;
      _auth.updateUser(user);
    } on PollaioNonConfiguratoException {
      _status = AreaUtenteStatus.pollaioNonConfigurato;
    } on UnauthorizedException {
      await _auth.onSessionExpired();
      return;
    } on ApiException catch (e) {
      _status = AreaUtenteStatus.failure;
      _errorMessage = e.message;
    }
    notifyListeners();
  }
}
