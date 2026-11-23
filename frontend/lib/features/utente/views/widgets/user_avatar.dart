import 'package:material_ui/material_ui.dart';

import '../../../../core/theme/app_theme.dart';

/// Cerchio con le iniziali dell'utente (`.initials` nella userbar web).
class UserAvatar extends StatelessWidget {
  const UserAvatar({super.key, required this.initials, this.size = 80});

  final String initials;
  final double size;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      alignment: Alignment.center,
      decoration: const BoxDecoration(
        color: AppColors.avatar,
        shape: BoxShape.circle,
      ),
      child: Text(
        initials,
        style: TextStyle(
          color: Colors.white,
          fontSize: size * 0.33,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
}
