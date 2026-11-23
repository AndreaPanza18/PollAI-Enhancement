import 'package:material_ui/material_ui.dart';

import '../../../core/theme/app_theme.dart';
import 'area_utente_page.dart';
import 'profile_page.dart';
import 'widgets/user_sidebar.dart';

/// Struttura dell'area autenticata: sidebar fissa su schermi larghi,
/// drawer su schermi stretti.
class UserShell extends StatefulWidget {
  const UserShell({super.key});

  @override
  State<UserShell> createState() => _UserShellState();
}

class _UserShellState extends State<UserShell> {
  static const double _wideBreakpoint = 900;
  static const double _sidebarWidth = 300;

  UserDestination _current = UserDestination.dashboard;

  void _select(UserDestination destination) {
    setState(() => _current = destination);
  }

  Widget get _content {
    return switch (_current) {
      UserDestination.dashboard => const AreaUtentePage(),
      UserDestination.profile => const ProfilePage(),
    };
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth >= _wideBreakpoint) {
          return Scaffold(
            body: Row(
              children: [
                SizedBox(
                  width: _sidebarWidth,
                  child: UserSidebar(current: _current, onSelected: _select),
                ),
                Expanded(child: _content),
              ],
            ),
          );
        }

        return Scaffold(
          appBar: AppBar(
            title: Text(_current.label),
            backgroundColor: AppColors.sidebar,
            foregroundColor: Colors.white,
          ),
          drawer: Drawer(
            backgroundColor: AppColors.sidebar,
            child: UserSidebar(
              current: _current,
              onSelected: (destination) {
                Navigator.of(context).pop(); // chiude il drawer
                _select(destination);
              },
            ),
          ),
          body: _content,
        );
      },
    );
  }
}
