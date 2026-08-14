---
tags:
  - api
  - core
---
# Persistence

This page specifies the **persistence layer**: the foundation every other feature uses to store data.
It is deliberately *not* about players, factions, or any specific feature — those are consumers, and
each gets its own page.

## The Problem

Ascendancy needs to attach data to things:

| We want to store        | Bound to               | Lives for              |
| ----------------------- | ---------------------- | ---------------------- |
| Levels, ExP, Lives      | A player               | Forever                |
| Ritual progress, claims | A chunk or region      | Minutes, or forever    |
| Members, PVP rules      | A **faction**          | Forever                |

Paper's [PDC](https://docs.papermc.io/paper/dev/pdc) covers the first two and nothing else. A faction
is not an entity, not a block, and not a world — there is no vanilla object to hang it off. Neither
is "the list of all factions", which somebody has to own.

So we need our own store, and it needs to address things Minecraft has never heard of.

## The Model: Entities and Components

An **ECS without the S**. There are no systems here, because a persistence layer has no business
running game logic.

- An **entity** is an identity that owns data. It is *just a key* — there is no entity object, no
  behaviour, nothing to subclass.
- A **component** is a typed, immutable chunk of data attached to an entity under an [[Identifier]].
- A **scope** groups entities that share a lifetime and a key format.

A player is an entity. A chunk is an entity. A faction is an entity. The store does not know or care
which is which — that is the entire point, and it is why factions stop being a special case.

### Keys

```java
public record DataKey(Identifier scope, String id) {}
```

The key is a **natural composite key**, not a surrogate UUID with a lookup table. This is load-bearing
for chunks: resolving "the entity for this chunk" must be pure computation on the main thread, not an
index lookup.

| Scope               | `id` format                  | Example                        |
| ------------------- | ---------------------------- | ------------------------------ |
| `ascendancy:player` | Player UUID                  | `c9f4...-8a1e`                 |
| `ascendancy:chunk`  | `<worldUuid>_<x>_<z>`        | `8b21...f0_-12_47`             |
| `ascendancy:world`  | World UUID                   | `8b21...f0`                    |
| `ascendancy:global` | Always `server`              | `server`                       |
| *`<pack>:faction`*  | Generated UUID               | `1d77...-b3c2`                 |

> [!IMPORTANT]
> `ascendancy:world` and `ascendancy:global` are not interchangeable. Bukkit models the overworld,
> the nether and the end as three separate worlds, so world scope has a **separate entity for each**
> — data written to one is invisible from the other two. Anything server-wide (which factions exist,
> which teams have been founded) belongs in `ascendancy:global`, which is world-independent and holds
> exactly one entity.

> [!NOTE]
> `id` is **not** an `Identifier`. `Identifier` forbids colons and its namespace is a plugin name by
> convention — neither fits a chunk coordinate. It is a plain validated `String`.

Because `id` ends up in a file path, it is restricted to `[A-Za-z0-9_.-]`. This is why the chunk
format uses the **world UUID** rather than the world name: server owners name worlds things like
`My World (COPY)`, and we would rather not find out what that does to a filesystem.

### Component Types

```java
ComponentType<T> extends Identifiable
```

A `ComponentType` bundles four things: its `Identifier`, its value class, a **default value**, and a
codec to and from [[#The Value Tree|DataValue]]. Registering it in a `Registry<ComponentType<?>>`
guards against two packs claiming the same `Identifier` and silently corrupting each other's data —
that collision check is the registry's job here, *not* codec lookup (see
[[#Unknown Components Must Survive]] for why decoding never consults it).

Registration is **enforced on write**: `set()` throws `ComponentTypeNotRegisteredException` for a type
that was never registered. A guard that a pack can skip by simply not calling `register()` is not a
guard, and the failure it is meant to catch — two packs quietly sharing an identifier — is invisible
until somebody's data is already gone.

Scopes are the looser case. Registering one declares its residency policy, but an unregistered scope
still works and is treated as `LAZY`; enumeration just falls back to querying the backend rather than
reading memory. Nothing is corrupted by getting it wrong, so it stays a declaration rather than a
gate.

> [!IMPORTANT]
> **Component values must be immutable**, ideally records. The store tracks changes at `set()`, so a
> value mutated in place behind its back will not be saved — and may be torn by a concurrent flush.
> Read, `with`, write back.

The default value is what `get()` returns for an entity that has never had the component set. It means
consumers never handle `Optional` for a component they own, and matches how [[Progression]] specifies
its stats — every one of them has a stated default.

## The Value Tree

Content packs compile against `:api` alone, so whatever serialization form appears there is **frozen
the moment a third party uses it**. We therefore do not expose Gson.

> [!NOTE]
> Mojang's `Codec`/`DynamicOps` would be the obvious choice, and is not available:
> `com.mojang:datafixerupper` is **not** on the `paper-api` compile classpath. Only `brigadier` is.

Instead `:api` defines a small sealed tree, and `:core` decides what it becomes on disk:

```java
sealed interface DataValue
    permits DataMap, DataList, DataString,
            DataInteger, DataDecimal, DataBoolean, DataBytes {}
```

`DataInteger` (a `long`) and `DataDecimal` (a `double`) are separate types on purpose. Collapsing them
into one "number" loses precision above 2^53 — which is exactly where UUID halves and world seeds
live. [[Progression]] also distinguishes `Integer` from `Decimal` in its own stat table, so the
vocabulary matches the spec that needs it.

This costs about two hundred lines that a raw `JsonElement` would not. What it buys is the ability to
move to SQLite, or to NBT, **without breaking a single content pack** — which is the difference
between a foundation and a shortcut.

## Unknown Components Must Survive

> [!WARNING]
> This is the requirement that is impossible to retrofit later. It must be true from the first commit.

Server owners add and remove content packs. If Core loads a faction whose stored data contains
`somepack:members`, and `somepack` is not installed this boot, then a decode-everything-on-load design
drops that data — and the next autosave writes the entity back **without it**, permanently. The player
who removed a pack for one restart loses their factions.

So the store never eagerly decodes. Each entity holds its components as **raw `DataValue`**, and
decodes lazily, only when somebody calls `get()` with a `ComponentType` they are already holding.

The pleasant consequence: an unregistered `Identifier` is never looked up, never fails, and is written
back **verbatim** on save. Unknown data survives because nothing ever touches it.

## Residency and Threading

Accessors are **synchronous and main-thread-safe**. Disk I/O is not.

Every scope declares a residency policy:

| Policy | Loaded             | Unloaded            | Used by             |
| ------ | ------------------ | ------------------- | ------------------- |
| EAGER  | All, at startup    | Never, until shutdown | Global scope, world scope, factions — anything with no natural load trigger |
| LAZY   | On demand          | By explicit lifecycle hook | Players (join→quit), chunks (load→unload) |

`EAGER` exists because a faction has **no natural load trigger** — nobody "joins" a faction the way a
player joins a server, and "list all factions" has to work before anyone asks for a specific one.
Loading the whole scope up front makes enumeration trivially correct.

### Where factions actually go

Two shapes work, and the choice is about size:

- **A component on `ascendancy:global`.** Simplest. Fine for a bounded list — "which factions exist"
  as one component. Every write rewrites the whole component, so this stops being appropriate once
  the collection is large or busy.
- **A pack-owned eager scope**, one entity per faction:
  ```java
  DataScope factions = DataScope.eager(Identifier.of(plugin, "faction"));
  persistence.scopes().register(factions);
  persistence.store().get(factions.key(id)).set(MEMBERS, members);
  ```
  Each faction is then independently readable and writable, and `store().keys(...)` enumerates them
  from memory. This is the recommended shape for anything faction-sized.

Neither needs Core to know factions exist, which is the point.

Writes go through dirty-tracking:

1. `set()` marks the entity dirty. Nothing touches the disk.
2. A repeating task flushes dirty entities on an interval.
3. A flush **snapshots on the calling thread**, then serializes and writes asynchronously.

Step 3 matters. `DataValue` is immutable, so a snapshot taken on the main thread cannot be torn by
gameplay code mutating the entity while the writer thread is mid-serialization. Handing the live map
to an async writer instead would be a data race that only shows up under load, on someone else's
server, at 3am.

Entities are also flushed when unloaded, and everything is flushed **synchronously** during shutdown —
after [[Events|AscendancyDisabledEvent]], so packs get their last writes in.

### Cold reads

Reading a `LAZY` entity that is not resident blocks on the disk, on whatever thread asked.
`preload()` returns a future for callers that can plan ahead.

Core warns about this **only when it is actually slow**: a cold read on the main thread taking longer
than `persistence.cold-read-warn-millis` (default 5ms) logs a warning naming the key; everything else
goes to `DEBUG`.

The threshold is the whole point. Warning on *every* cold main-thread read would be useless, because
for chunks that is the intended design — chunk data is read on demand rather than preloaded on
`ChunkLoadEvent`, since the overwhelming majority of chunks never carry any Ascendancy data and
preloading them all would cost far more than it saved. A warning that fires constantly is one people
learn to scroll past, which is worse than no warning at all. Set the threshold to `0` to silence it
entirely.

## Versioning and Migration

Every component carries a `version()`, starting at 1, written to disk beside its data. When stored
data is behind the running code, the component's `migrator()` brings it forward *in `DataValue`
space* — before `decode` is ever called, because the whole point is that the old shape cannot be
decoded by the current codec.

```java
ComponentType.<Stats>builder(id, Stats.class)
    .defaultValue(() -> new Stats(1))
    .version(2)
    .mapCodec(encoder, decoder)
    .migrator((data, fromVersion) -> /* rename, retype, restructure */)
    .build();
```

Three rules keep this from going wrong:

- **A version bump without a migrator fails at construction**, not on some player's next login. You
  cannot ship a component that renders existing data unreadable with no way back.
- **Data from a newer version is refused**, never guessed at. Downgrading a pack throws rather than
  misreading a shape from the future.
- **A migration is paid for once.** The upgraded data is written back into the entity and marks it
  dirty, so the next flush persists the new shape. This does mean a pure read can schedule a write —
  intended, since the alternative is migrating the same data on every read forever.

Migrators should handle *every* version they might meet, not just the previous one. A player who has
not logged in for a year arrives with whatever was current back then.

> [!NOTE]
> A component belonging to an uninstalled pack keeps its own version untouched alongside its data, so
> removing a pack for one restart cannot silently downgrade it. This is the same guarantee as
> [[#Unknown Components Must Survive]], extended to the version.

## Storage Backend

`:core` defines a `StorageBackend` SPI and ships one implementation: **JSON files**, one per entity.
Zero setup for server owners, and hand-inspectable when something goes wrong.

```
plugins/AscendancyCore/data/<namespace>/<path>/<id>.json
                            ascendancy/player/c9f4....json
                            builtin/faction/1d77....json
```

The scope `Identifier` *is* the directory path, which is a nice accident of it already being
`namespace:path`.

Writes are **atomic** — written to a temporary file, then moved into place. A server killed mid-write
leaves the previous version intact rather than a half-written file that fails to parse on boot.

Each component is stored inside a small envelope carrying its version:

```json
{
  "$version": 1,
  "ascendancy:stats": { "$v": 2, "$d": { "experience": 1234.0 } }
}
```

`$version` is the *file* format; `$v` is the *component* version and `$d` its data. Reserving the
`$` prefix in `DataMap` up front is what makes this unambiguous — no component's own data can imitate
an envelope. An entry with no `$v` predates versioning and is read back as version 1, so files
written before this existed still load.

### JSON Encoding Notes

Two cases where the value tree does not map cleanly onto JSON:

- **Integer vs decimal.** JSON has one number type. On read, a numeric literal containing `.`, `e`, or
  `E` becomes `DataDecimal`; everything else becomes `DataInteger`.
- **Byte arrays.** JSON has none. `DataBytes` is written as `{"$b64": "<base64>"}`. To keep that
  sentinel unambiguous, `DataMap` **rejects keys beginning with `$`**.

Neither is elegant. Both are contained entirely within the JSON backend, which is the point of having
an SPI — a future SQLite or NBT backend has neither problem.

## API Surface

`AscendancyAPI` gains exactly **one** accessor:

```java
Persistence persistence();
```

`Persistence` exposes `store()`, `componentTypes()` and `scopes()`. Grouping them keeps the root API
from accumulating three methods for one feature, and gives later persistence additions somewhere to
live that is not `AscendancyAPI`.

## Code Layout

Split by concern rather than left in one `data` package, so a reader looking for "how is a value
represented" doesn't have to wade past scopes and stores to find out.

```
:api  io.github.hyscript7.ascendancy.api.data
        .                 Persistence — the entry point, and nothing else
        .value            DataValue and its seven implementations, DataCodecException
        .component        ComponentType, ComponentHolder, and their exception
        .store            DataStore, DataKey, DataScope, DataScopes, ResidencyPolicy

:core io.github.hyscript7.ascendancy.data
        .                 BasePersistence, DataLifecycleListener
        .store            BaseDataStore, BaseComponentHolder, the two registries
        .backend          StorageBackend and its JSON implementation
```

Two things pin this arrangement rather than a tidier-looking one:

- **The registries sit with the store, not in a `registry` package of their own.** They call
  package-private methods on `BaseDataStore` to mirror scopes and component types across. Separating
  them would mean widening that internal wiring to `public`.
- **`store` and `backend` are deliberately different words.** `store` is the in-memory access layer
  callers touch; `backend` is the medium underneath it. Naming the latter `storage` sat one letter
  away from the former and read as a typo.

## What This Page Does Not Cover

Deliberately out of scope, each pending its own page:

- **Player data.** `ascendancy:player` is defined here as a scope; the ergonomic API for reading a
  player's stats is a separate feature built *on* this one. See [[Progression]] for the stats it will
  need to carry.
- **Queries by component value.** Enumerating a scope is supported. "Find every faction containing
  player X" is an index, and indexes are a feature, not a foundation.
- **Cross-component migrations.** A component migrates itself. Moving data *between* two components,
  or migrating on behalf of a pack that is not loaded, is not supported.

## Failure Is Loud

**Decode failure throws.** A component whose stored data cannot be read raises `DataCodecException`
rather than quietly resetting to its default — silently discarding a player's levels because one
field was renamed is the worse outcome by a wide margin.

Because it throws, every method that can trigger a decode **declares it**, following the same
convention as `Registry#register`: the exception is unchecked, but naming it in the `throws` clause is
what makes it visible in an IDE and catchable on purpose rather than by accident.

| Declares | Methods |
| -------- | ------- |
| `DataCodecException` | `ComponentHolder#get`, `#find`, `#set`; `ComponentType#encode`, `#decode`, `#defaultValue`; `ComponentMigrator#migrate`; `DataMap`'s typed accessors |
| `DataStorageException` | `DataStore#get`, `#exists`, `#keys`, `#delete` |
| `ComponentTypeNotRegisteredException` | `ComponentHolder#set` |

The entity itself still loads — the failure is confined to the one component, and its raw data is
retained untouched, so nothing is lost while the bug is fixed.
