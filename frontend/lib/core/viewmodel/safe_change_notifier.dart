import 'package:flutter/foundation.dart';

/// Base dei ViewModel: ignora le notifiche arrivate dopo il `dispose`
/// (tipico quando una chiamata di rete termina a pagina già chiusa).
abstract class SafeChangeNotifier extends ChangeNotifier {
  bool _disposed = false;

  bool get isDisposed => _disposed;

  @override
  void notifyListeners() {
    if (!_disposed) super.notifyListeners();
  }

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }
}
