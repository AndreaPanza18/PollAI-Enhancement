import 'package:material_ui/material_ui.dart';

/// Palette ripresa dai CSS del frontend HTTP
/// (`login.css`, `userbar.css`).
abstract final class AppColors {
  static const Color background = Color(0xFFDFFFE0);
  static const Color authCard = Color(0xFF354580);
  static const Color authField = Color(0xFF3E4EA8);
  static const Color authHint = Color(0xFFA4B2CC);
  static const Color primaryAction = Color(0xFF088008);
  static const Color sidebar = Color(0xFF203752);
  static const Color avatar = Color(0xFF019ADE);
  static const Color errorOnCard = Color(0xFFFFB4AB);
}

abstract final class AppTheme {
  static ThemeData get light {
    return ThemeData(
      colorScheme: ColorScheme.fromSeed(seedColor: AppColors.authCard),
      scaffoldBackgroundColor: AppColors.background,
    );
  }
}
