import 'package:material_ui/material_ui.dart';

enum BannerKind { error, info }

/// Riquadro per messaggi d'errore o informativi sopra i form.
class MessageBanner extends StatelessWidget {
  const MessageBanner({
    super.key,
    required this.message,
    this.kind = BannerKind.error,
  });

  final String message;
  final BannerKind kind;

  @override
  Widget build(BuildContext context) {
    final isError = kind == BannerKind.error;
    return Semantics(
      liveRegion: true,
      child: Container(
        width: double.infinity,
        margin: const EdgeInsets.only(bottom: 16),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isError ? const Color(0xFFFFDAD6) : const Color(0xFFD6E3FF),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(
              isError ? Icons.error_outline : Icons.info_outline,
              size: 20,
              color: isError
                  ? const Color(0xFF93000A)
                  : const Color(0xFF001B3E),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                message,
                style: TextStyle(
                  color: isError
                      ? const Color(0xFF410002)
                      : const Color(0xFF001B3E),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
