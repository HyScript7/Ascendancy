<div align="center">
    <img src="logo.png" height="128" width="128" alt="Ascendancy">
</div>

<h1 align="center">
    Ascendancy
</h1>

<p align="center">
    A mystical Paper 1.21.11+ plugin which makes minecraft a fantasy world of magic and gods.
</p>

<p align="center">
    <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
    <img src="https://img.shields.io/badge/PaperMC-%232c7ffa.svg?style=for-the-badge" alt="PaperMC">
</p>

## About

Ascendancy transforms Minecraft into a progression-heavy, lore-rich, divinity-driven experience where players climb from
mere mortals to terrifying mythological beings.

Inspired by _Verdant Moon, Lord of the Mysteries, Lifesteal SMP, BlissSMP Gems, Made in Abyss,_ and the _Origins_ mod.

## Features

<details>
    <summary>Player Stats & Progression</summary>

### Player Stats & Progression

- **Innate Name** – a player’s unique identity, used for spells, commands, and divine targeting.
- **Lives** – run out, and you become **Void Banned**, trapped in the Void Realm until rescued.
- **Execution Points (ExP)** – gained from PvP/PvE; levels you up.
- **Levels** – reach **Level 50** to perform a Resurrection.
- **Resurrection (Prestige)** – grants permanent perks such as:
  - Detecting when others hold your innate items
  - Knowing when your Innate Name is spoken
  - Teleporting to players attempting malicious use of your name

**Innate Name Mechanics**

- Names can be freely rerolled only if no one else has heard them.
- Otherwise, changing your name requires a ritual, boss defeat, or other high-tier feat.
- Names appear censored unless the listener already knows it or is within a set radius.
</details>

<details>
    <summary>Magic System</summary>

### Magic System

- **Torn Spell Books** spawn in loot chests and teach incantations or keywords.
- Players can inscribe learned spells into a written book to create **spell scrolls**.
- Right-click scrolls to cast spells (results not guaranteed to be OSHA-compliant).

</details>

<details>
    <summary>Rituals</summary>

### Rituals

Rituals use structures like:

- Soul fire setups
- Candle-lined ritual circles

They can:

- Sacrifice items and mobs
- Summon entities or bosses
- Apply effects
- Trigger environmental changes

Rituals can be **instant** or **lasting**, remaining active as long as the structure stays intact.

</details>

<details>
    <summary>Divinity & Prayers</summary>

### Divinity & Prayers

Ascendancy lets players bind **incantations** to entities or concepts ("the messenger belonging to \<player\>").
These references can evolve into divine entities as their progression increases.

Ascendancy itself adds certain higher existences which can be prayed to. For example:

- The Primordial Abyss

  ```
  The Abyss deep beneath this world
  You are the origin and bane of all life
  The end of creation and destruction
  ```

  (Example Incantation - Might and will change)

**Prayers** are multistep incantations or rituals requesting favors from the linked being.
Whether it answers politely or smites you is… up to it.

For example:

```
I pray for your attention
I pray for you to take me home
```

Could teleport the player to their respawn point.

</details>

<details>
    <summary>Void Realm</summary>

### Void Realm

A multi-layered dimension players fall into when touching the void.
Each layer:

- Is its own world
- Has unique structures, mobs, and bosses
- Provides fast-travel via increasing **Void : Overworld** ratios (starting at 1:16 and doubling each layer)

Players can move between layers by:

- Falling deeper
- Performing rituals
- Using special items

</details>

<details>
    <summary>Bosses</summary>

### Bosses

Bosses may appear through:

- Rituals
- Divine incantations
- Special structures
- Summoning items
- Naturally in certain Void layers

They drop **Relics**, **Arcanas**, rare items, or progression resources.

</details>

<details>
    <summary>Innate Names (Command Invocation)</summary>

### Innate Names (Command Invocation)

Players’ Innate Names can be used to perform direct verbal commands:

- "_\<name\> I bestow you health_"
- "_\<name\> close your eyes_"

These can buff, debuff, support, or outright harass the target based on spell rules.

</details>

<details>
    <summary>Innate Items</summary>

### Innate Items

Players can bind effects to items that become:

- Soulbound
- Valuable ritual components if dropped
- Tools for tracking, teleporting, casting spells, or guiding players to locations

Innate items are incredibly flexible and extremely abusable.

</details>

<details>
    <summary>Races</summary>

### Races

On first join, players choose a race (similar to _Origins_).
Each race has unique traits and belongs to either:

- **Soul Beings**
- **Void Beings**

</details>

<details>
    <summary>Relics</summary>

### Relics

Powerful passive, active, or equipment-based items with strict rules:

- Only **one equipped** at a time
- Cannot be stored in containers
- Dropped only via Innate Name bestowals
- Destroyed on death
- Can be stored by a special NPC found near Void Realm structures

Examples include:

- **Manasense** – chance to dodge incoming damage
- **Lightshot** – long-range grappling shot
- **Forbidden Blade** – deals more damage against mana-rich enemies
- **Abyssal Blade** – manipulates dimensional gateways

</details>

<details>
    <summary>Arcanas</summary>

### Arcanas

High-tier spell abilities:

- Dropped exclusively from bosses
- Far stronger than normal magic
- Share a **global cooldown**, length determined by the last Arcana used

</details>

<details>
    <summary>Artifacts</summary>

### Artifacts

Player-crafted items engraved with spells or abilities.
Unlike innate items, they are **not** soulbound and can be freely traded.
Drawbacks may include:

- Limited uses
- Stat penalties
- Swapped breathing types
- Other deliciously cursed side effects
</details>

## Wiki

Coming soon...

## Installation

Coming soon...

## For Developers

### Framework vs Content

* **AscendancyCore** supplies APIs for:
  Stats, rituals, magic system, divinity features, dimension logic, races, relic architecture, etc.

* **AscendancyPacks** supply:
  Actual spells, relics, bosses, races, items, structures, and more, by leveraging the Core APIs.

### Building

Coming soon...

### Documentation

Coming soon...

### Writing your own Content Pack

Coming soon...

## License

This project is licensed under the **BSD 3-Clause License**.
See the LICENSE file for more information.

Copyright (c) 2025, HyScript7 and Ascendancy Core contributors
