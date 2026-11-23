import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/utils/validators.dart';
import '../../../core/widgets/auth_layout.dart';
import '../../../core/widgets/auth_text_field.dart';
import '../../../core/widgets/message_banner.dart';
import '../../../core/widgets/primary_button.dart';
import '../viewmodels/auth_view_model.dart';
import '../viewmodels/register_view_model.dart';

/// Schermata di registrazione (equivalente di `registeruser.html`).
class RegisterPage extends StatelessWidget {
  const RegisterPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<RegisterViewModel>(
      create: (context) => RegisterViewModel(context.read<AuthViewModel>()),
      child: const _RegisterView(),
    );
  }
}

class _RegisterView extends StatefulWidget {
  const _RegisterView();

  @override
  State<_RegisterView> createState() => _RegisterViewState();
}

class _RegisterViewState extends State<_RegisterView> {
  final _formKey = GlobalKey<FormState>();
  final _nomeController = TextEditingController();
  final _cognomeController = TextEditingController();
  final _phoneController = TextEditingController();
  final _pIvaController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  void dispose() {
    _nomeController.dispose();
    _cognomeController.dispose();
    _phoneController.dispose();
    _pIvaController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final viewModel = context.read<RegisterViewModel>();
    final navigator = Navigator.of(context);
    if (!(_formKey.currentState?.validate() ?? false)) return;

    final success = await viewModel.submit(
      nome: _nomeController.text,
      cognome: _cognomeController.text,
      phoneNumber: _phoneController.text,
      pIva: _pIvaController.text,
      email: _emailController.text,
      password: _passwordController.text,
    );

    // L'utente è ora autenticato: la schermata di login sotto viene
    // sostituita dall'area utente, quindi chiudiamo anche questa pagina.
    if (success) navigator.popUntil((route) => route.isFirst);
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<RegisterViewModel>();
    final enabled = !viewModel.isLoading;

    return AuthLayout(
      title: 'Registration form',
      subtitle: 'Fill out the form below to create your account',
      child: Form(
        key: _formKey,
        child: AutofillGroup(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (viewModel.errorMessage != null)
                MessageBanner(message: viewModel.errorMessage!),
              AuthTextField(
                controller: _nomeController,
                label: 'First Name',
                maxLength: Validators.nameMaxLength,
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                autofillHints: const [AutofillHints.givenName],
                enabled: enabled,
                validator: viewModel.validateFirstName,
              ),
              const SizedBox(height: 20),
              AuthTextField(
                controller: _cognomeController,
                label: 'Last Name',
                maxLength: Validators.nameMaxLength,
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                autofillHints: const [AutofillHints.familyName],
                enabled: enabled,
                validator: viewModel.validateLastName,
              ),
              const SizedBox(height: 20),
              AuthTextField(
                controller: _phoneController,
                label: 'Phone Number',
                maxLength: Validators.phoneMaxLength,
                keyboardType: TextInputType.phone,
                textInputAction: TextInputAction.next,
                autofillHints: const [AutofillHints.telephoneNumber],
                enabled: enabled,
                validator: viewModel.validatePhoneNumber,
              ),
              const SizedBox(height: 20),
              AuthTextField(
                controller: _pIvaController,
                label: 'P.IVA (Tax ID)',
                maxLength: Validators.pIvaMaxLength,
                textCapitalization: TextCapitalization.characters,
                textInputAction: TextInputAction.next,
                enabled: enabled,
                validator: viewModel.validatePIva,
              ),
              const SizedBox(height: 20),
              AuthTextField(
                controller: _emailController,
                label: 'Email',
                maxLength: Validators.emailMaxLength,
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                autofillHints: const [AutofillHints.email],
                enabled: enabled,
                validator: viewModel.validateEmail,
              ),
              const SizedBox(height: 20),
              AuthTextField(
                controller: _passwordController,
                label: 'Password',
                obscureText: viewModel.obscurePassword,
                textInputAction: TextInputAction.done,
                autofillHints: const [AutofillHints.newPassword],
                enabled: enabled,
                validator: viewModel.validatePassword,
                onFieldSubmitted: (_) => _submit(),
                suffixIcon: IconButton(
                  tooltip: viewModel.obscurePassword
                      ? 'Show password'
                      : 'Hide password',
                  color: Colors.white70,
                  icon: Icon(
                    viewModel.obscurePassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                  ),
                  onPressed: viewModel.togglePasswordVisibility,
                ),
              ),
              const SizedBox(height: 8),
              CheckboxListTile(
                contentPadding: EdgeInsets.zero,
                controlAffinity: ListTileControlAffinity.leading,
                activeColor: AppColors.primaryAction,
                checkColor: Colors.white,
                side: const BorderSide(color: Colors.white70, width: 2),
                title: const Text(
                  'I hold the RASVSL certificate',
                  style: TextStyle(color: Colors.white),
                ),
                value: viewModel.hasRasvsl,
                onChanged: enabled
                    ? (value) => viewModel.setHasRasvsl(value ?? false)
                    : null,
              ),
              const SizedBox(height: 20),
              PrimaryButton(
                label: 'Register',
                isLoading: viewModel.isLoading,
                onPressed: _submit,
              ),
              const SizedBox(height: 12),
              TextButton(
                style: TextButton.styleFrom(foregroundColor: Colors.white70),
                onPressed: enabled
                    ? () => Navigator.of(context).maybePop()
                    : null,
                child: const Text('Already have an account? Sign in'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
