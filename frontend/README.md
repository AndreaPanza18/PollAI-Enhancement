# PollAI frontend (Flutter)

App Flutter (web, Android, iOS, desktop) che consuma le API REST del backend PollAI.
Per ora copre il package **Utente** (`/api/v1/user`, autenticazione con token JWT):
login, registrazione, area utente e profilo.

## Architettura (MVVM)

```
lib/
├── main.dart                      composizione delle dipendenze
├── app.dart                       PollAIApp + AuthGate (splash → login → area utente)
├── core/                          codice condiviso tra i package
│   ├── config/api_config.dart     API_BASE_URL (--dart-define)
│   ├── network/                   ApiClient (HTTP + Bearer) e ApiException
│   ├── storage/token_storage.dart token JWT su Keychain/Keystore
│   ├── utils/validators.dart      regole dei form (stesse del frontend web)
│   ├── viewmodel/                 SafeChangeNotifier, ViewStatus
│   ├── theme/ widgets/            palette e componenti riutilizzabili
└── features/utente/
    ├── data/                      Model: DTO, UtenteApiService, UtenteRepository
    ├── viewmodels/                ViewModel: Auth, Login, Register, Profile, AreaUtente
    └── views/                     View: schermate e sidebar
```

- **View** (`views/`): solo widget, leggono lo stato dal ViewModel (`provider`) e inoltrano le azioni.
- **ViewModel** (`viewmodels/`): `ChangeNotifier` con stato della schermata, validazione e logica d'uso; non conosce i widget.
- **Model** (`data/`): `UtenteRepository` è l'unico punto d'accesso ai dati; gestisce anche il token JWT (salvataggio, Bearer, logout).
- `AuthViewModel` tiene la sessione dell'intera app. Un `401` su una chiamata protetta riporta al login.

Corrispondenza con il frontend HTTP (Thymeleaf):

| Flutter | Template web | Endpoint |
|---|---|---|
| `LoginPage` | `login.html` | `POST /login` |
| `RegisterPage` | `registeruser.html` | `POST /register` |
| `AreaUtentePage` | `areautente.html` | `GET /area-utente` (409 = pollaio non configurato) |
| `ProfilePage` | `profilo.html` | `GET /profilo` |
| `UserSidebar` / `UserShell` | `fragments/userbar.html` | — |

## Avvio

```bash
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8060
```

Su emulatore Android usare `--dart-define=API_BASE_URL=http://10.0.2.2:8060`.
In Docker il valore arriva dall'argomento `API_BASE_URL` (vedi `docker-compose.yml`).

```bash
flutter analyze
flutter test
```

## Note

- **CORS (web)**: se l'app web e il backend sono su origini diverse (es. `:8081` e `:8060`) il backend deve
  consentire l'origine del frontend, altrimenti il browser blocca le chiamate. Mobile e desktop non sono interessati.
- `flutter_secure_storage` su web funziona solo in HTTPS o su `localhost`.
- In produzione usare HTTPS: iOS e Android bloccano il traffico HTTP in chiaro verso host non locali.
