import 'package:material_ui/material_ui.dart';

/// Titolo + sottotitolo delle pagine interne (`<h1>` + `<p>` del web).
class PageHeader extends StatelessWidget {
  const PageHeader({super.key, required this.title, this.subtitle});

  final String title;
  final String? subtitle;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: textTheme.headlineMedium),
        if (subtitle != null) ...[
          const SizedBox(height: 8),
          Text(subtitle!, style: textTheme.bodyLarge),
        ],
        const SizedBox(height: 24),
      ],
    );
  }
}
