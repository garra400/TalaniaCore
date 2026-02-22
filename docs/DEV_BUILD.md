## Talania Dev Build

This repo supports a dev-only build for debugging tools (commands + UI).
Release builds do not include dev-only classes.

### How It Works
- Dev-only classes live under `core/src/dev/java`.
- Dev-only UI assets live under `core/src/dev/resources`.
- The dev jar is built with both `main` and `dev` sources.
- The release jar includes only `main` sources.

### Build Commands
Build release jar (no dev tools):
```bash
./gradlew :core:jar
```

Build dev jar (includes debug commands + UI):
```bash
./gradlew :core:devJar
```

Build races dev jar (includes races debug UI):
```bash
./gradlew :races:devJar
```

### Notes
- Dev tools are registered at runtime via reflection. Release builds skip this.
- Debug commands are under `/talania debug ...` (dev jar only).
- Combat log and debug services exist in `src/main`, but are only exposed by dev commands/UI.
