import 'package:frontend/features/utente/viewmodels/auth_view_model.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/viewmodel/safe_change_notifier.dart';
import '../../../core/viewmodel/view_status.dart';
import '../data/models/gallina.dart';
import '../data/models/salute_consiglio_response.dart';
import '../data/salute_repository.dart';

/// Logica della schermata Salute (`GET /accesso-salute` e `POST /consiglio/{id}`).
class SaluteViewModel extends SafeChangeNotifier {
  SaluteViewModel({
    required this._repository,
    required this._auth,
  });

  final SaluteRepository _repository;
  final AuthViewModel _auth;

  ViewStatus _status = ViewStatus.loading;
  List<Gallina> _galline = [];
  String? _errorMessage;
  Gallina? _selectedGallina;
  SaluteConsiglioResponse? _consiglio;
  ViewStatus _consiglioStatus = ViewStatus.success;

  ViewStatus get status => _status;
  List<Gallina> get galline => _galline;
  String? get errorMessage => _errorMessage;
  Gallina? get selectedGallina => _selectedGallina;
  SaluteConsiglioResponse? get consiglio => _consiglio;
  ViewStatus get consiglioStatus => _consiglioStatus;

  /// Carica la lista delle galline dal backend.
  Future<void> load() async {
    _status = ViewStatus.loading;
    _errorMessage = null;
    _selectedGallina = null;
    _consiglio = null;
    _consiglioStatus = ViewStatus.success;
    notifyListeners();

    try {
      final galline = await _repository.getGalline();
      _galline = galline;
      _status = ViewStatus.success;
    } on UnauthorizedException {
      await _auth.onSessionExpired();
      return;
    } on PollaioNonConfiguratoException {
      _status = ViewStatus.failure;
      _errorMessage = 'Pollaio non configurato. Configura il tuo pollaio prima di accedere alla salute.';
    } on ApiException catch (e) {
      _status = ViewStatus.failure;
      _errorMessage = e.message;
    }
    notifyListeners();
  }

  /// Seleziona una gallina per visualizzarne i dettagli.
  void selectGallina(Gallina gallina) {
    _selectedGallina = gallina;
    _consiglio = null;
    _consiglioStatus = ViewStatus.success;
    notifyListeners();
  }

  /// Richiede un consiglio sulla salute della gallina selezionata.
  Future<void> richiediConsiglio() async {
    final gallina = _selectedGallina;
    if (gallina == null) return;

    _consiglioStatus = ViewStatus.loading;
    _consiglio = null;
    notifyListeners();

    try {
      final response = await _repository.richiediConsiglio(gallina.id);
      _consiglio = response;
      _consiglioStatus = ViewStatus.success;
    } on UnauthorizedException {
      await _auth.onSessionExpired();
      return;
    } on ApiException catch (e) {
      _consiglioStatus = ViewStatus.failure;
      _errorMessage = e.message;
    }
    notifyListeners();
  }

  /// Deseleziona la gallina corrente e torna alla lista.
  void deselectGallina() {
    _selectedGallina = null;
    _consiglio = null;
    _consiglioStatus = ViewStatus.success;
    notifyListeners();
  }
}
