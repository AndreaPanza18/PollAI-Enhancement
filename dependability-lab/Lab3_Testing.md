# Testing Strategy Report — PollAI Enhancement

## 1. Overview & Tooling
To guarantee software dependability and prevent regressions in the **PollAI** web application (`webapp` module), we implemented an automated unit testing suite built with **JUnit Jupiter (6.1.3)** and **Mockito (5.24.0)**, managed via **Maven**. 

Our testing strategy focuses on verifying critical business rules, data validation, and HTTP controller behavior in isolation, decoupled from the relational database and the external Python/Flask AI prediction microservice (`UovaService`).

---

## 2. Test Suite Architecture
The test suite mirrors the modular domain architecture under `webapp/src/test/java/com/example/pollai/`:

| Domain Package | Tested Components | Key Scenarios & Validations |
| :--- | :--- | :--- |
| **`magazzino`** | `Magazzino`, `Cibo`, `Farmaco`, `MagazzinoService`, `MagazzinoController` | Stock registration, feed/medicine updates, low-stock threshold alerts (`NotificaCarenzaScorteTest`), and inventory validation. |
| **`pollaio`** | `Pollaio`, `Gallina`, `PollaioService`, `PollaioController` | Coop configuration, hen weight and age validation (`InserimentoPesoGallinaTest`, `InserimentoEtaGallinaTest`), and flock management. |
| **`produzione`** | `DatiProduzione`, `Prediction`, `ProduzioneService`, `ProduzioneController` | Egg production tracking, input validation, and mocked interaction with the AI forecasting endpoint. |
| **`salute`** | `SaluteController` | Health monitoring workflows and edge-case handling (`PollAISaluteTest`). |
| **`utente`** | `Utente`, `UtenteService`, `UtenteController` | User registration, credential validation, authentication flows, and profile updates. |

---

## 3. Adherence to FIRST Principles
Every unit test in the repository is designed according to the **FIRST** principles of Test-Driven Development:

- **Fast**: Tests run in memory without starting a full database or external containers, executing the entire Maven suite in seconds.
- **Independent**: Each test class uses `@BeforeEach` to initialize fresh domain objects and reset mock states, preventing shared mutable state or execution-order dependencies.
- **Repeatable**: Deterministic inputs and stubbed dependencies ensure identical pass/fail outcomes across local IDEs and CI runners.
- **Self-Validating**: Standard JUnit assertions (`assertEquals`, `assertNotNull`, `assertThrows`, `assertTrue`) and Mockito verifications (`verify`) automatically evaluate outcomes without manual inspection.
- **Thorough**: Beyond standard getter/setter and happy-path checks, tests cover boundary values, invalid inputs, null handling, and domain-specific error conditions.

---

## 4. Mocking Strategy with Mockito
To maintain strict unit isolation:
- **Persistence Layer Isolation**: Spring Data / DAO interfaces (`MagazzinoDAO`, `CiboDAO`, `FarmacoDAO`, `PollaioDAO`, `GallinaDAO`, `ProduzioneDAO`, `UtenteDAO`) are mocked using `@Mock` and injected into service classes via `@InjectMocks`.
- **Controller & Service Isolation**: Controller tests mock underlying service beans to verify request routing, model attributes, and HTTP status responses independently of business logic execution.
- **External Service Decoupling**: Calls to external prediction services (`UovaService`) are stubbed with deterministic `Prediction` payloads to test production workflows reliably.

---

## 5. Continuous Integration (CI/CD)
Automated verification is enforced through GitHub Actions (`.github/workflows/ci.yml`). On every `push` and `pull_request` targeting `main`, the pipeline provisions **JDK 21 (Temurin)**, executes `mvn test` on the `webapp` module, and archives Surefire XML/TXT test reports as build artifacts to provide immediate feedback on code quality.
