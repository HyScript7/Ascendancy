# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Ascendancy is a PaperMC plugin suite split into a framework and its content. See `README.md` for the
feature/lore surface and `docs/` for design documents — don't restate them here.

- `:api` — public contracts only. Interfaces, records, exceptions. Consumed by Core *and* by
  third-party content packs.
- `:core` — the framework implementation (`AscendancyCore` plugin). Implements `:api`, owns
  persistence, registries, and the bundled datapack.
- `:packs:builtInPack` — content (`AscendancyBuiltinPack` plugin). Talks to Core **only** through
  `:api`.

**Boundary rule:** contracts go in `:api`, framework implementation goes in `:core`, gameplay content
goes in a pack. Never leak an implementation type into `:api` — packs compile against `:api` alone.

## Build and run

Gradle 9.2 wrapper, Java 25 toolchain. Every module's jar is redirected into the **root**
`build/libs/`, not its own build dir — `archiveBaseName` is set to
`Ascendancy-<Project>-<version>-MC<mcVersion>-SNAPSHOT`, and Gradle then appends `archiveVersion`, so
the files come out as `Ascendancy-Core-3.0.0-MC26.1.2-SNAPSHOT-3.0.0.jar` (version twice — quirk of
the current build, not a typo). `:core` additionally produces a shaded `-all.jar` (that's the one to
deploy); pack jars are thin and get Core's classes via `join-classpath: true` in their
`paper-plugin.yml`.

```sh
./gradlew build                         # build all modules
./gradlew :core:runServer               # Paper server in core/run/, Core only
./gradlew :packs:builtInPack:runServer  # Paper server in packs/builtinpack/run/, Core + the pack
./gradlew clean                         # root clean (wipes build/libs) + each subproject's clean
```

`runServer` (run-paper) auto-downloads CarbonChat, LuckPerms, Citizens and Sentinel into the run dir.
`core/run/` and `packs/builtinpack/run/` are gitignored; treat them as disposable.

**`:packs:builtInPack:runServer` is the full-stack task** — use it for anything behavioural.
run-paper only ever stages the project it was invoked from, so the pack build adds Core explicitly:
`:core` exposes its shaded jar through a consumable `pluginJar` configuration, and the pack resolves
it via a `corePluginJar` configuration into `runServer.pluginJars`. Core is therefore rebuilt from
source on every run and cannot go stale.

Note that run-paper does **not** copy staged jars into `run/plugins/` — that directory holds only
plugin data directories, never a jar. Plugins are handed to the server by path out of `build/libs/`
instead. Corollary: **never drop an Ascendancy jar into `run/plugins/` by hand.** It will not
overwrite the staged one, it loads *beside* it, and Paper then trips over a duplicate plugin name.

It has to be the pack that names Core, not the reverse. `:core:runServer` stays Core-only on
purpose: a framework build that reaches for its own content inverts the module hierarchy. A pack
depending on Core is just the hierarchy written down, which is why `corePluginJar` lives in the
pack. It is also artifact-only and touches no classpath — packs still compile against `:api` alone,
per the boundary rule.

**Gradle path casing:** the canonical project path is `:packs:builtInPack` (capital I) even though the
directory is `packs/builtinpack` — `settings.gradle` remaps it. Gradle's name matching also accepts
`:packs:builtinpack`, but write the canonical form.

## Formatting

Spotless (Palantir Java Format, 4-space / 120-col) is configured in the root build, matched by
`.editorconfig`.

```sh
./gradlew spotlessApply     # format all Java sources
./gradlew spotlessCheck     # report violations without writing
```

The whole tree was formatted in one dedicated commit, so **`./gradlew build` now fails on formatting
violations** — `spotlessCheck` runs as part of `check`. Run `spotlessApply` before committing.

`spotlessApply` reformats an entire module. To format a single file without dragging unrelated ones
into your diff:

```sh
./gradlew :api:spotlessApply -PspotlessIdeHook=$PWD/path/to/File.java
```

Note that `build` does **not** run `javadoc`, so broken `{@link}` targets compile happily. Run
`./gradlew :api:javadoc` after moving or renaming anything in `:api`.

## Versioning

`gradle/libs.versions.toml` is the single source of truth. `minecraft`/`paper` there flow into
`paper-plugin.yml`'s `api-version` via `processResources` token expansion. Bump versions in the
catalog only — never hardcode a version in a module's `build.gradle` or in `paper-plugin.yml`.

The README's "Paper 1.21.11+" line is stale; the catalog is current.

## Dependency traps

These are load-bearing and easy to undo:

- `libs.bundles.npc` (Citizens, Sentinel) is `compileOnly` with `exclude group: '*', module: '*'`.
  Making it `implementation` shades Citizens/Sentinel into the jar and breaks the target server.
- InventoryFramework (`libs.bundles.menu`) *is* shaded in `:core` and relocated to
  `io.github.hyscript7.ascendancy.inventoryframework`. Keep the `relocate` in sync if the package
  moves.
- Server-provided APIs (Paper, Carbon) are always `compileOnly`.

## Conventions

- Lombok is applied to all subprojects from the root build. `@Slf4j` is the logging idiom — use
  `log.info(...)` with `{}` placeholders, not string concatenation and not `getLogger()`.
- Everything public in `:api` gets Javadoc, including `@param`, `@return`, and `@throws` for every
  declared exception. `Identifier` is the reference example.
- Comments explain *why*, not *what*. The existing dry humour in build files and comments is
  deliberate — match it rather than sanitising it.
- Registry keys are `Identifier` (`namespace:path`); registrable types implement `Identifiable`.
  Use `Identifier.of(plugin, path)` so the namespace is the owning plugin's name. The `Registry`
  contract lives in `:api`; `BaseRegistry` is its `:core` implementation.

## Core ↔ pack handshake

Content packs must not assume Core is loaded at their `onEnable()`. The intended pattern:

1. `paper-plugin.yml` declares `AscendancyCore` with `load: BEFORE`, `required: true`.
2. Register a `Listener` in `onEnable()`.
3. Do actual registration inside an `@EventHandler` for `AscendancyEnabledEvent`
   (`io.github.hyscript7.ascendancy.api.event` — singular).
4. Reach the API via `AscendancyAPI.get()` (throws if Core hasn't registered yet) or
   `AscendancyAPI.fromServicesManager()` for the `Optional` form.
5. **Cache the instance in a field and null it out on disable.** `get()` is a services manager
   lookup that takes a lock, so calling it per entity per tick is a needless main-thread cost.
   Releasing it in the `AscendancyDisabledEvent` handler is what stops a pack holding a reference to
   a Core that has gone away.

Teardown work belongs in an `AscendancyDisabledEvent` handler, which Core fires at the start of its
`onDisable()`.

**Core fires `AscendancyEnabledEvent` on the first tick, not from `onEnable()`.** Packs declare Core
as a required dependency, so Bukkit enables Core *before* them — firing during `onEnable()` announced
to an empty room, because no pack had registered a listener yet. `loadEagerScopes()` runs immediately
after the event for the same reason: a scope registered by a pack has to exist before Core tries to
populate it.

Core already fires both events, but `AscendancyBuiltinPack` is currently an empty `JavaPlugin` — the
pattern above is the design, not something you can copy from an existing implementation yet.

`AscendancyAPI` exposes the instance plumbing (`get`, `set`, `fromServicesManager`) and
`persistence()`. The services manager is the only source of truth — there is deliberately no static
holder, so nothing survives a reload. See `docs/Mechanics and Systems/Persistence.md` for the
persistence layer; there is no player or stats layer on it yet, and an earlier attempt was reverted as
non-compliant with the spec in `docs/`. Check `docs/` before designing one.

## Testing

There are no tests and JUnit is deliberately commented out in `libs.versions.toml`. **Ask before
adding a test framework or test files.** Default verification is `./gradlew build` plus, for
behavioural changes, a `./gradlew :packs:builtInPack:runServer` session — that one boots the whole
stack, so it is the one that can actually catch a broken Core↔pack handshake.

## Git

- Trunk is **`3.0.0`** — branch from it and target it with PRs. The local `Main` branch is a leftover
  from an older versioning scheme; don't treat it as trunk.
- Branch names: `<Author>/ASC-<ticket>` (e.g. `lipinoDaDerg/ASC-7`).
- Commits use gitmoji prefixes. Used in this repo so far, with their official meanings: `✨` new
  feature, `🐛` bug fix, `♻️` refactor, `🎨` improve structure/formatting, `🔧` config files,
  `📝` docs, `⬆️` upgrade dependencies, `➕` add a dependency, `🍱` assets, `💥` breaking change,
  `🚚` move or rename, `🙈` .gitignore, `🏗️` architectural change, `👷` CI, `🔨` dev scripts,
  `🎉` begin a project.
  Need one that isn't listed? `curl https://gitmoji.dev/api/gitmojis` returns all 75 as JSON
  (`emoji`, `code`, `description`, `semver`) — far cleaner to read than scraping the site. Add
  whatever you end up using to the list above, so it keeps describing *this* project's habits rather
  than becoming a copy of the catalogue nobody reads.
- Don't commit unless asked.

## Documentation

`docs/` is an Obsidian vault of developer-facing design docs, using `[[wikilinks]]`. Non-trivial
features get a page there, linked from `docs/README.md`'s Crossroads section.

`docs/.obsidian/workspace.json` is editor UI state that churns constantly — never stage it.

## Scope

Don't implement `TODO` stubs, refactor adjacent code, or "fix" things outside what was asked. This
codebase has several deliberate in-progress seams.
