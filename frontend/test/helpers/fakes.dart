import 'package:frontend/features/utente/data/models/login_response.dart';
import 'package:frontend/features/utente/data/models/register_request.dart';
import 'package:frontend/features/utente/data/models/utente.dart';
import 'package:frontend/features/utente/data/utente_repository.dart';

const sampleUser = Utente(
  id: 1,
  nome: 'Mario',
  cognome: 'Rossi',
  phoneNumber: '+39 3331234567',
  rasvsl: true,
  pIva: 'IT12345678901',
  email: 'mario.rossi@example.com',
);

const sampleLoginResponse = LoginResponse(
  accessToken: 'jwt-token',
  tokenType: 'Bearer',
  expiresIn: 3600,
  user: sampleUser,
  pollaioConfigurato: true,
);

/// Repository finto: ogni chiamata esegue la funzione configurata nel test.
class FakeUtenteRepository implements UtenteRepository {
  FakeUtenteRepository({
    this.onRestoreSession,
    this.onLogin,
    this.onRegister,
    this.onGetProfilo,
    this.onGetAreaUtente,
  });

  final Future<Utente?> Function()? onRestoreSession;
  final Future<LoginResponse> Function(String email, String password)? onLogin;
  final Future<LoginResponse> Function(RegisterRequest request)? onRegister;
  final Future<Utente> Function()? onGetProfilo;
  final Future<Utente> Function()? onGetAreaUtente;

  int logoutCalls = 0;

  @override
  Future<Utente?> restoreSession() =>
      onRestoreSession != null ? onRestoreSession!() : Future.value(null);

  @override
  Future<LoginResponse> login({
    required String email,
    required String password,
  }) => onLogin!(email, password);

  @override
  Future<LoginResponse> register(RegisterRequest request) =>
      onRegister!(request);

  @override
  Future<Utente> getProfilo() => onGetProfilo!();

  @override
  Future<Utente> getAreaUtente() => onGetAreaUtente!();

  @override
  Future<void> logout() async {
    logoutCalls++;
  }
}
