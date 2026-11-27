# Lab 5: Formal Verification with JML

We used JML (Java Modeling Language) annotations and OpenJML's Extended Static Checker (ESC) to specify and statically verify selected properties of the Java code. ESC checks whether the implementation satisfies the written specifications; it does not execute the application or replace runtime tests.

## JML choices

- `nullable_by_default` makes references non-null by default. We used `nullable` for values that may be absent and `non_null` for required values, such as the elements of the coop and warehouse lists.
- Method contracts use `requires` to state preconditions, `ensures` to state guarantees on normal return, and `signals_only` to restrict the exception types considered. For example, `UtenteService` requires non-null inputs and a repository before saving or authenticating a user.
- `assignable` describes which state a method may modify. We used `\\everything` where repository calls or other side effects make a narrower frame difficult to specify, and a precise field frame for the password setter in `Utente`.
- `pure` marks getters and password verification as methods that do not modify state. `spec_public` exposes selected private fields to specifications without changing their Java visibility.
- `assume` supplies facts about framework-managed or repository-returned values, such as Spring's injected repository and DAO results. These assumptions help ESC reason across code whose behavior is external to the analyzed method; they are trusted premises, not properties proven by JML.
- `skipesc` excludes code that ESC cannot meaningfully analyze, such as BCrypt hashing and `toString`. `code_bigint_math` makes the food quantity sum use mathematical integer arithmetic during checking, avoiding Java `int` overflow in that specification context.

## Running OpenJML

From the repository root, move to the `webapp` directory, copy Maven dependencies to `target/libs`, then run OpenJML on the source file to verify:

```powershell
cd webapp
..\mvnw dependency:copy-dependencies "-DoutputDirectory=target/libs"
cmd /c '"C:\Dev\openjml-windows-21.0.29\openjml.bat" --esc -cp "target/libs/\*" -sourcepath src/main/java src/main/java/com/example/pollai/utente/UtenteService.java'
```

The first command changes to the module directory. The second downloads or copies the dependencies required to resolve the project's external types. The third launches OpenJML in ESC mode, supplies the dependencies and source path, and selects `UtenteService.java` as the file to check. To check another class, replace the final source-file path with the path of that class.

## What was checked

The selected verification target is `UtenteService`. Its contracts describe the expected input conditions for saving, listing, retrieving, deleting, and authenticating users, along with guarantees such as a non-null result and a password hash that verifies against the supplied password. The checker also analyzes nullability and the declared method effects along the paths in these methods. BCrypt operations are marked `skipesc`, so their internal implementation is not verified; the `assume` statements about injected dependencies and DAO results are taken as given.

Other JML annotations are present in domain classes including `Utente`, `Pollaio`, and `Magazzino`, but the command above selects only `UtenteService.java` as its verification target. The result therefore does not establish that every class or the entire application has been formally verified.
