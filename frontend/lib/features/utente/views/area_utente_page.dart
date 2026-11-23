import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/page_header.dart';
import '../data/utente_repository.dart';
import '../viewmodels/area_utente_view_model.dart';
import '../viewmodels/auth_view_model.dart';

/// Area utente (equivalente di `areautente.html`).
class AreaUtentePage extends StatelessWidget {
  const AreaUtentePage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<AreaUtenteViewModel>(
      create: (context) => AreaUtenteViewModel(
        repository: context.read<UtenteRepository>(),
        auth: context.read<AuthViewModel>(),
      )..load(),
      child: const _AreaUtenteView(),
    );
  }
}

class _AreaUtenteView extends StatelessWidget {
  const _AreaUtenteView();

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<AreaUtenteViewModel>();

    return switch (viewModel.status) {
      AreaUtenteStatus.loading => const Center(
        child: CircularProgressIndicator(),
      ),
      AreaUtenteStatus.failure => ErrorState(
        message: viewModel.errorMessage ?? 'Something went wrong.',
        onRetry: viewModel.load,
      ),
      AreaUtenteStatus.pollaioNonConfigurato => const _Content(
        header: PageHeader(
          title: 'User area',
          subtitle: 'Welcome to your personal area.',
        ),
        card: _InfoCard(
          icon: Icons.home_work_outlined,
          title: "Your chicken coop isn't set up yet",
          message:
              'Set up your chicken coop to unlock your user area. '
              'Coop configuration is not available in the app yet.',
        ),
      ),
      AreaUtenteStatus.ready => _Content(
        header: const PageHeader(
          title: 'User area',
          subtitle: 'Welcome to your personal area.',
        ),
        card: _InfoCard(
          icon: Icons.waving_hand_outlined,
          title: 'Welcome back, ${viewModel.user?.nome ?? ''}!',
          message: 'Select an option from the menu.',
        ),
      ),
    };
  }
}

class _Content extends StatelessWidget {
  const _Content({required this.header, required this.card});

  final Widget header;
  final Widget card;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Align(
        alignment: Alignment.topLeft,
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 720),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [header, card],
          ),
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  const _InfoCard({
    required this.icon,
    required this.title,
    required this.message,
  });

  final IconData icon;
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, size: 32),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: textTheme.titleMedium),
                  const SizedBox(height: 6),
                  Text(message, style: textTheme.bodyMedium),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
