import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import '../../../core/viewmodel/view_status.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/page_header.dart';
import '../data/models/utente.dart';
import '../data/utente_repository.dart';
import '../viewmodels/auth_view_model.dart';
import '../viewmodels/profile_view_model.dart';
import 'widgets/user_avatar.dart';

/// Profilo utente (equivalente di `profilo.html`).
class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<ProfileViewModel>(
      create: (context) => ProfileViewModel(
        repository: context.read<UtenteRepository>(),
        auth: context.read<AuthViewModel>(),
      )..load(),
      child: const _ProfileView(),
    );
  }
}

class _ProfileView extends StatelessWidget {
  const _ProfileView();

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<ProfileViewModel>();

    switch (viewModel.status) {
      case ViewStatus.loading:
        return const Center(child: CircularProgressIndicator());
      case ViewStatus.failure:
        return ErrorState(
          message: viewModel.errorMessage ?? 'Something went wrong.',
          onRetry: viewModel.load,
        );
      case ViewStatus.success:
        return _ProfileContent(
          user: viewModel.user!,
          onLogout: viewModel.logout,
        );
    }
  }
}

class _ProfileContent extends StatelessWidget {
  const _ProfileContent({required this.user, required this.onLogout});

  final Utente user;
  final VoidCallback onLogout;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Align(
        alignment: Alignment.topLeft,
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 720),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const PageHeader(
                title: 'User Profile',
                subtitle: 'Here you can view your profile information.',
              ),
              Row(
                children: [
                  UserAvatar(initials: user.initials),
                  const SizedBox(width: 20),
                  Expanded(
                    child: Text(user.fullName, style: textTheme.headlineSmall),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              Card(
                child: Column(
                  children: [
                    _InfoTile(
                      icon: Icons.email_outlined,
                      label: 'Email',
                      value: user.email,
                    ),
                    _InfoTile(
                      icon: Icons.phone_outlined,
                      label: 'Phone number',
                      value: user.phoneNumber,
                    ),
                    _InfoTile(
                      icon: Icons.badge_outlined,
                      label: 'P.IVA (Tax ID)',
                      value: user.pIva ?? '-',
                    ),
                    _InfoTile(
                      icon: Icons.verified_outlined,
                      label: 'RASVSL certificate',
                      value: user.rasvsl ? 'Yes' : 'No',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              OutlinedButton.icon(
                onPressed: onLogout,
                icon: const Icon(Icons.logout),
                label: const Text('Logout'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoTile extends StatelessWidget {
  const _InfoTile({
    required this.icon,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon),
      title: Text(label),
      subtitle: Text(value.isEmpty ? '-' : value),
    );
  }
}
