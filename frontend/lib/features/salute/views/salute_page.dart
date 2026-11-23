import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/viewmodel/view_status.dart';
import '../../../core/widgets/error_state.dart';
import '../../../core/widgets/page_header.dart';
import '../../../core/widgets/primary_button.dart';
import '../../utente/viewmodels/auth_view_model.dart';
import '../data/models/gallina.dart';
import '../data/salute_repository.dart';
import '../viewmodels/salute_view_model.dart';

/// Pagina di gestione salute (equivalente di `gestione-salute.html`).
/// Mostra la griglia delle galline e i dettagli con il consiglio salute.
class SalutePage extends StatelessWidget {
  const SalutePage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<SaluteViewModel>(
      create: (context) => SaluteViewModel(
        repository: context.read<SaluteRepository>(),
        auth: context.read<AuthViewModel>(),
      )..load(),
      child: const _SaluteView(),
    );
  }
}

class _SaluteView extends StatelessWidget {
  const _SaluteView();

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<SaluteViewModel>();

    switch (viewModel.status) {
      case ViewStatus.loading:
        return const Center(child: CircularProgressIndicator());
      case ViewStatus.failure:
        return ErrorState(
          message: viewModel.errorMessage ?? 'Something went wrong.',
          onRetry: viewModel.load,
        );
      case ViewStatus.success:
        if (viewModel.galline.isEmpty) {
          return const _EmptyState();
        }
        return _SaluteContent(viewModel: viewModel);
    }
  }
}

/// Stato vuoto quando non ci sono galline.
class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.pets_outlined, size: 80, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text(
            'No hens found.',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  color: Colors.grey[600],
                ),
          ),
          const SizedBox(height: 8),
          Text(
            'Configure your chicken coop to get started.',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Colors.grey[500],
                ),
          ),
        ],
      ),
    );
  }
}

class _SaluteContent extends StatelessWidget {
  const _SaluteContent({required this.viewModel});

  final SaluteViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Align(
        alignment: Alignment.topLeft,
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 900),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const PageHeader(
                title: 'Health Management',
                subtitle: 'Monitor the health of your hens and get AI-powered advice.',
              ),
              if (viewModel.selectedGallina != null)
                _GallinaDetailsPanel(viewModel: viewModel)
              else
                _GallineGrid(viewModel: viewModel),
            ],
          ),
        ),
      ),
    );
  }
}

/// Griglia di card delle galline (stile `gestione-salute.css`).
class _GallineGrid extends StatelessWidget {
  const _GallineGrid({required this.viewModel});

  final SaluteViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 20,
        mainAxisSpacing: 20,
        childAspectRatio: 0.85,
      ),
      itemCount: viewModel.galline.length,
      itemBuilder: (context, index) {
        final gallina = viewModel.galline[index];
        return _GallinaCard(
          gallina: gallina,
          onTap: () => viewModel.selectGallina(gallina),
        );
      },
    );
  }
}

/// Card singola per una gallina (stile `.gallina-card` del CSS).
class _GallinaCard extends StatelessWidget {
  const _GallinaCard({required this.gallina, required this.onTap});

  final Gallina gallina;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: const BorderSide(color: AppColors.avatar, width: 1),
      ),
      color: const Color(0xFFEAF1F8),
      child: InkWell(
        borderRadius: BorderRadius.circular(8),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(15),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircleAvatar(
                radius: 30,
                backgroundColor: AppColors.avatar,
                child: const Icon(Icons.pets, color: Colors.white, size: 32),
              ),
              const SizedBox(height: 10),
              Text(
                gallina.razza,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                  color: AppColors.sidebar,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 4),
              Text(
                'Age: ${gallina.eta} yrs',
                style: const TextStyle(fontSize: 12, color: Colors.grey),
              ),
              Text(
                gallina.pesoFormattato,
                style: const TextStyle(fontSize: 12, color: Colors.grey),
              ),
              const SizedBox(height: 10),
              FilledButton(
                onPressed: onTap,
                style: FilledButton.styleFrom(
                  backgroundColor: AppColors.avatar,
                  minimumSize: const Size(double.infinity, 36),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(5),
                  ),
                ),
                child: const Text('View Details'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Pannello dettagli gallina (stile `#dettagliGallina` del CSS).
class _GallinaDetailsPanel extends StatelessWidget {
  const _GallinaDetailsPanel({required this.viewModel});

  final SaluteViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    final gallina = viewModel.selectedGallina!;
    final textTheme = Theme.of(context).textTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Pulsante indietro
        TextButton.icon(
          onPressed: viewModel.deselectGallina,
          icon: const Icon(Icons.arrow_back),
          label: const Text('Back to hens list'),
        ),
        const SizedBox(height: 16),
        // Card dettagli
        Card(
          elevation: 2,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
            side: const BorderSide(color: AppColors.avatar, width: 1),
          ),
          color: const Color(0xFFEAF1F8),
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      radius: 35,
                      backgroundColor: AppColors.avatar,
                      child: const Icon(Icons.pets, color: Colors.white, size: 36),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            gallina.razza,
                            style: textTheme.headlineSmall?.copyWith(
                                  color: AppColors.sidebar,
                                ),
                          ),
                          Text(
                            'Hen ID: ${gallina.id}',
                            style: textTheme.bodyMedium?.copyWith(
                                  color: Colors.grey[600],
                                ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                const Divider(),
                const SizedBox(height: 12),
                _DetailRow(label: 'Breed', value: gallina.razza),
                _DetailRow(label: 'Age', value: '${gallina.eta} years'),
                _DetailRow(label: 'Weight', value: gallina.pesoFormattato),
                if (gallina.temperaturaFormattata != null)
                  _DetailRow(
                    label: 'Temperature',
                    value: gallina.temperaturaFormattata!,
                  ),
                if (gallina.mangime != null && gallina.mangime!.isNotEmpty)
                  _DetailRow(label: 'Feed', value: gallina.mangime!),
                const SizedBox(height: 20),
                // Pulsante per richiedere consiglio
                PrimaryButton(
                  label: 'Request Health Advice',
                  onPressed: viewModel.richiediConsiglio,
                  isLoading: viewModel.consiglioStatus == ViewStatus.loading,
                ),
                const SizedBox(height: 16),
                // Messaggio salute
                _ConsiglioSection(viewModel: viewModel),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

/// Riga di dettaglio (label + value).
class _DetailRow extends StatelessWidget {
  const _DetailRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: AppColors.sidebar,
              ),
            ),
          ),
          Expanded(
            child: Text(value.isEmpty ? '-' : value),
          ),
        ],
      ),
    );
  }
}

/// Sezione che mostra il consiglio salute ricevuto dal backend.
class _ConsiglioSection extends StatelessWidget {
  const _ConsiglioSection({required this.viewModel});

  final SaluteViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    switch (viewModel.consiglioStatus) {
      case ViewStatus.loading:
        return const SizedBox(
          height: 24,
          width: double.infinity,
          child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
        );
      case ViewStatus.failure:
        return _HealthMessage(
          message: viewModel.errorMessage ?? 'Error fetching advice.',
          isError: true,
        );
      case ViewStatus.success:
        final consiglio = viewModel.consiglio;
        if (consiglio == null) return const SizedBox.shrink();
        return _HealthMessage(
          message: consiglio.message,
          isError: consiglio.isUnhealthy,
        );
    }
  }
}

/// Messaggio di salute con colore verde/rosso (stile `#messaggioSalute` del CSS).
class _HealthMessage extends StatelessWidget {
  const _HealthMessage({required this.message, required this.isError});

  final String message;
  final bool isError;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: isError ? Colors.red[50] : Colors.green[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: isError ? Colors.red[200]! : Colors.green[200]!,
        ),
      ),
      child: Row(
        children: [
          Icon(
            isError ? Icons.error_outline : Icons.check_circle_outline,
            color: isError ? Colors.red[700] : Colors.green[700],
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              message,
              style: TextStyle(
                color: isError ? Colors.red[700] : Colors.green[700],
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
