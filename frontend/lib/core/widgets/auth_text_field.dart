import 'package:material_ui/material_ui.dart';

import '../theme/app_theme.dart';

/// Campo di testo con lo stile dei form di accesso (sfondo blu, testo bianco).
class AuthTextField extends StatelessWidget {
  const AuthTextField({
    super.key,
    required this.controller,
    required this.label,
    this.validator,
    this.keyboardType,
    this.textInputAction,
    this.textCapitalization = TextCapitalization.none,
    this.autofillHints,
    this.obscureText = false,
    this.suffixIcon,
    this.maxLength,
    this.enabled = true,
    this.onFieldSubmitted,
  });

  final TextEditingController controller;
  final String label;
  final FormFieldValidator<String>? validator;
  final TextInputType? keyboardType;
  final TextInputAction? textInputAction;
  final TextCapitalization textCapitalization;
  final Iterable<String>? autofillHints;
  final bool obscureText;
  final Widget? suffixIcon;
  final int? maxLength;
  final bool enabled;
  final ValueChanged<String>? onFieldSubmitted;

  @override
  Widget build(BuildContext context) {
    OutlineInputBorder border([
      Color color = Colors.transparent,
      double width = 1,
    ]) {
      return OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: color, width: width),
      );
    }

    return TextFormField(
      controller: controller,
      enabled: enabled,
      obscureText: obscureText,
      keyboardType: keyboardType,
      textInputAction: textInputAction,
      textCapitalization: textCapitalization,
      autofillHints: autofillHints,
      maxLength: maxLength,
      validator: validator,
      autovalidateMode: AutovalidateMode.onUserInteraction,
      onFieldSubmitted: onFieldSubmitted,
      style: const TextStyle(color: Colors.white, fontSize: 16),
      cursorColor: Colors.white,
      decoration: InputDecoration(
        labelText: label,
        labelStyle: const TextStyle(color: AppColors.authHint),
        floatingLabelStyle: const TextStyle(color: Colors.white),
        filled: true,
        fillColor: AppColors.authField,
        // Nasconde il contatore ma mantiene il limite di lunghezza.
        counterText: '',
        suffixIcon: suffixIcon,
        errorMaxLines: 3,
        errorStyle: const TextStyle(color: AppColors.errorOnCard),
        border: border(),
        enabledBorder: border(),
        disabledBorder: border(),
        focusedBorder: border(Colors.white, 1.5),
        errorBorder: border(AppColors.errorOnCard),
        focusedErrorBorder: border(AppColors.errorOnCard, 1.5),
      ),
    );
  }
}
