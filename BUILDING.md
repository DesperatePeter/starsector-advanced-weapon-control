# Building Advanced Gunnery Control Locally

This repository builds against your local Starsector install. You do not need to copy dependency jars into this repo.

## Requirements

- Java/JDK compatible with the Gradle wrapper.
- A local Starsector install.
- Required dependency mods installed in `Starsector/mods`:
  - LazyLib
  - MagicLib
  - LunaLib
  - Console Commands

Console Commands is required for the current source because AGC compiles a console command integration.

## Configure The Developer Settings

Most build and contributor settings live in one file:

```text
development.properties
```

The file is ordered from most to least important. For first-time setup, check the `starsector.defaultDirs` fallback list first, then the version/release values if you are preparing a release.

If you want a private machine-specific Starsector path without editing the shared config, use one of these overrides.

PowerShell:

```powershell
$env:STARSECTOR_DIRECTORY='C:\Path\To\Starsector'
```

Bash:

```bash
export STARSECTOR_DIRECTORY="$HOME/games/starsector"
```

Gradle property:

```bash
./gradlew --no-daemon -PstarsectorDir="$HOME/games/starsector" buildMod
```

Local properties:

Create an untracked file named `local.properties` for private overrides:

```properties
starsector.dir=C:/Path/To/Starsector
```

You can copy `local.properties.example` as a starting point.

Path priority is:

1. `-PstarsectorDir=...`
2. `STARSECTOR_DIRECTORY=...`
3. `local.properties` key `starsector.dir=...`
4. `development.properties` key `starsector.defaultDirs=...`, using the first existing path.

## Build

PowerShell:

```powershell
.\gradlew.bat --no-daemon buildMod
```

Bash:

```bash
./gradlew --no-daemon buildMod
```

`buildMod` validates the local Starsector/dependency paths, compiles Kotlin, builds the jar, and regenerates metadata/settings files.

## Verify Paths

```powershell
.\gradlew.bat --no-daemon validateLocalBuildEnvironment
```

This prints the resolved Starsector root, Starsector core folder, and dependency mod folders.

## IDE Setup

1. Clone the repo.
2. Install Starsector and the required dependency mods.
3. Check `development.properties`, or configure a private Starsector path with `STARSECTOR_DIRECTORY`, `-PstarsectorDir`, or `local.properties`.
4. Open the repo in the IDE as a Gradle project.
5. Run `validateLocalBuildEnvironment`, then `buildMod`.

If changing environment variables, prefer `--no-daemon` so Gradle does not reuse a daemon started with stale environment values.

## Deploy For Local Testing

Deployment is explicit and never part of ordinary builds:

```powershell
.\gradlew.bat --no-daemon deployLocalMod
```

This copies the built mod into:

```text
Starsector/mods/Advanced-Gunnery-Control
```

It excludes VCS, IDE, Gradle cache/wrapper, build output, local path settings, and backup files.

## Common Failures

### Starsector directory not configured

Set `STARSECTOR_DIRECTORY`, pass `-PstarsectorDir=...`, create `local.properties`, or update `starsector.defaultDirs` in `development.properties`.

### Could not find Starsector core jars

The path should point to the Starsector install root, not the `mods` folder. The build checks both `<starsector>/starsector-core` and `<starsector>`.

### Missing required dependency mod

Install the named dependency mod into `Starsector/mods`.

### Dependency mod has no jars directory

The dependency mod folder was found, but it does not look like a usable installed mod. Reinstall or re-extract that dependency.

### Unresolved Starsector symbols

Run:

```powershell
.\gradlew.bat --no-daemon validateLocalBuildEnvironment
```

If you recently changed `STARSECTOR_DIRECTORY`, also make sure you are using `--no-daemon`.
