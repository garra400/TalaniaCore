## Talania Dev Build

This repo supports a core dev build that enables debugging tools (commands + UI).
Release builds include debug assets, but they remain disabled unless dev mode is on.

### How It Works
- Dev-only classes live under `core/src/dev/java`.
- Dev-only UI assets live under `core/src/dev/resources`.
- All modules always compile `main` + `dev` sources, but debug entry points are gated by Talania dev mode.
- Talania dev mode is enabled only when running the Core dev jar (`TalaniaCoreDEV`).

### Build Commands
Build release jar (no dev tools):
```bash
./gradlew :core:jar
```

Build core dev jar (enables debug commands + UI):
```bash
./gradlew :core:devJar
```

### Notes
- Dev tools are registered at runtime via reflection, but only when dev mode is enabled.
- Debug commands are under `/talania debug ...` (core dev jar only).
- Combat log and debug services exist in `src/main`, but are only exposed by dev commands/UI.
- Custom UI pages should reference the game-provided `Common.ui`/`Sounds.ui` (`$C = "../Common.ui"`), but we do **not** ship those files in our asset packs.
