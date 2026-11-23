import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import '../../../../core/theme/app_theme.dart';
import '../../viewmodels/auth_view_model.dart';
import 'user_avatar.dart';

/// Sezioni dell'area utente raggiungibili dalla sidebar.
///
/// Per ora solo quelle del package Utente: le altre voci della userbar web
/// (Production, Health, Warehouse, Chicken coop) si aggiungono qui quando
/// i rispettivi package saranno esposti via API.
enum UserDestination {
  dashboard('Dashboard', Icons.dashboard_outlined),
  health('Health', Icons.medical_services_outlined),
  profile('Profile', Icons.person_outline);

  const UserDestination(this.label, this.icon);

  final String label;
  final IconData icon;
}

/// Barra laterale (equivalente di `fragments/userbar.html`).
class UserSidebar extends StatelessWidget {
  const UserSidebar({
    super.key,
    required this.current,
    required this.onSelected,
  });

  final UserDestination current;
  final ValueChanged<UserDestination> onSelected;

  @override
  Widget build(BuildContext context) {
    final user = context
        .select<AuthViewModel, ({String name, String initials})>(
          (auth) => (
            name: auth.user?.fullName ?? '',
            initials: auth.user?.initials ?? '?',
          ),
        );

    return Material(
      color: AppColors.sidebar,
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              InkWell(
                borderRadius: BorderRadius.circular(12),
                onTap: () => onSelected(UserDestination.profile),
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  child: Row(
                    children: [
                      UserAvatar(initials: user.initials),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Text(
                          user.name,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
              for (final destination in UserDestination.values)
                _SidebarItem(
                  destination: destination,
                  selected: destination == current,
                  onTap: () => onSelected(destination),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SidebarItem extends StatelessWidget {
  const _SidebarItem({
    required this.destination,
    required this.selected,
    required this.onTap,
  });

  final UserDestination destination;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: ListTile(
        selected: selected,
        selectedColor: Colors.white,
        selectedTileColor: Colors.white.withValues(alpha: 0.12),
        textColor: Colors.white70,
        iconColor: Colors.white70,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        leading: Icon(destination.icon),
        title: Text(destination.label, style: const TextStyle(fontSize: 18)),
        onTap: onTap,
      ),
    );
  }
}
