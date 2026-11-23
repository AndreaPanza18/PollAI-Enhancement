/// Corpo di `POST /register` (`RegisterRequest` del backend).
class RegisterRequest {
  const RegisterRequest({
    required this.nome,
    required this.cognome,
    required this.phoneNumber,
    required this.rasvsl,
    required this.pIva,
    required this.email,
    required this.password,
  });

  final String nome;
  final String cognome;
  final String phoneNumber;
  final bool rasvsl;
  final String pIva;
  final String email;
  final String password;

  Map<String, dynamic> toJson() => {
    'nome': nome,
    'cognome': cognome,
    'phoneNumber': phoneNumber,
    'rasvsl': rasvsl,
    'pIVA': pIva,
    'email': email,
    'password': password,
  };
}
