import 'package:material_ui/material_ui.dart';
import 'package:provider/provider.dart';

import '../../../core/widgets/auth_layout.dart';
import '../../../core/widgets/auth_text_field.dart';
import '../../../core/widgets/message_banner.dart';
import '../../../core/widgets/primary_button.dart';
import '../viewmodels/auth_view_model.dart';
import '../viewmodels/login_view_model.dart';
import 'register_page.dart';

/// Schermata di login (equivalente di `login.html`).
class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<LoginViewModel>(
      create: (context) => LoginViewModel(context.read<AuthViewModel>()),
      child: const _LoginView(),
    );
  }
}

class _LoginView extends StatefulWidget {
  const _LoginView();

  @override
  State<_LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<_LoginView> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final viewModel = context.read<LoginViewModel>();
    if (!(_formKey.currentState?.validate() ?? false)) return;
    await viewModel.submit(
      email: _emailController.text,
      password: _passwordController.text,
    );
  }

  void _openRegister() {
    Navigator.of(context)
        .push(MaterialPageRoute<void>(builder: (_) => const RegisterPage()));
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<LoginViewModel>();

    return AuthLayout(
      title: 'Sign in to your account',
      child: Form(
        key: _formKey,
        child: AutofillGroup(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (viewModel.notice != null)
                MessageBanner(
                  message: viewModel.notice!,
                  kind: BannerKind.info,
                ),
              if (viewModel.errorMessage != null)
                MessageBanner(message: viewModel.errorMessage!),
              AuthTextField(
                controller: _emailController,
                label: 'Email',
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                autofillHints: const [AutofillHints.email],
                enabled: !viewModel.isLoading,
                validator: viewModel.validateEmail,
              ),
              const SizedBox(height: 20),
              AuthTextField(
                controller: _passwordController,
                label: 'Password',
                obscureText: viewModel.obscurePassword,
                textInputAction: TextInputAction.done,
                autofillHints: const [AutofillHints.password],
                enabled: !viewModel.isLoading,
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
              const SizedBox(height: 28),
              PrimaryButton(
                label: 'Log in',
                isLoading: viewModel.isLoading,
                onPressed: _submit,
              ),
              const SizedBox(height: 12),
              TextButton(
                style: TextButton.styleFrom(foregroundColor: Colors.white70),
                onPressed: viewModel.isLoading ? null : _openRegister,
                child: const Text("Don't have an account? Register"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
