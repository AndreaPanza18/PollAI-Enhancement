import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import 'core/theme/app_theme.dart';
import 'features/utente/data/utente_repository.dart';
import 'features/utente/viewmodels/auth_view_model.dart';
import 'features/utente/views/login_page.dart';
import 'features/utente/views/user_shell.dart';

/// Radice dell'app: registra repository e ViewModel di sessione e mostra
/// le schermate giuste in base allo stato di autenticazione.
class PollAIApp extends StatelessWidget {
  const PollAIApp({super.key, required this.repository});

  final UtenteRepository repository;

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider<UtenteRepository>.value(value: repository),
        ChangeNotifierProvider<AuthViewModel>(
          create: (_) => AuthViewModel(repository)..restoreSession(),
        ),
      ],
      child: MaterialApp(
        title: 'PollAI',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light,
        home: const AuthGate(),
      ),
    );
  }
}

/// Sceglie la schermata iniziale in base alla sessione:
/// splash → login → area utente.
class AuthGate extends StatelessWidget {
  const AuthGate({super.key});

  @override
  Widget build(BuildContext context) {
    final status = context.select<AuthViewModel, AuthStatus>(
      (auth) => auth.status,
    );

    return switch (status) {
      AuthStatus.unknown => const _SplashPage(),
      AuthStatus.unauthenticated => const LoginPage(),
      AuthStatus.authenticated => const UserShell(),
    };
  }
}

class _SplashPage extends StatelessWidget {
  const _SplashPage();

  @override
  Widget build(BuildContext context) {
    return const Scaffold(body: Center(child: CircularProgressIndicator()));
  }
}
