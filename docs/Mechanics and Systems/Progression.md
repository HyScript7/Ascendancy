---
tags:
---
# Progression

This feature introduces a leveling and prestige ("**resurrection**") system.

## Player Data

In order to make this feature work, we need to track the following player stats:

| Key              | Type           | Default Value |
| ---------------- | -------------- | ------------- |
| **Level**        | Integer <1;50> | 1             |
| **Resurrection** | Integer <0;+∞> | 0             |
| **Experience**   | Decimal <0;+∞> | 0.0           |
| Kills            | Integer <0;+∞> | 0             |
| Deaths           | Integer <0;+∞> | 0             |

Optionally, we may also track **Kills** and **Deaths**, both of which would be **non-negative Integers** with a default value of **0**.

## Leveling Up and Experience

**Execution Points** (or ExP for short) can be gained from PvP or PvE kills. Either can be enabled or disabled in the **configuration file**.
Once enough **ExP** is gathered, the player will level up.

> [!TODO]
> Please do note: It currently isn't specified how to calculate the exact amount of ExP required per level.  
> Among the options I am considering are a constant or logarithmic scale. Both of these seem to be the most promising, but we will ultimately have to run some **play tests** to decide which approach is the most enjoyable for the players.

The amount of **ExP** gained from **PvP** is dependent on the **victim's level**, while the amount of ExP gained from **PvE** is dependent on the **killed mobs attributes**, such as max health, damage, armor / defense, etc.

### On the topic of Mob Grinders

It is up to consideration whether mob farms should be penalized in terms of ExP gained.

Perhaps a **dedicated config option** could be implemented to let server owners decide whether they want to allow mob grinders.

The debuff would work by decreasing the amount of **ExP** gained following a **logarithmic scale**.

### Preventing Bed Camping / Trapping

Since **ExP** gained from **Player Kills** scales with the **victim's level**, players might consider bed trapping a high level player and farming them for experience.

While one could argue that it's fair play, we **do need** a system to prevent spawn camping, not because of player ExP farms, but most due to the **Void Ban** system, but that will be covered in it's own document as it concerns multiple features / domains.

As such, we will need to remember to not give the killer ExP if whatever implementation of the anti spawn kill simply refunds lives instead of preventing death, as not doing so would create an infinite ExP farm or "dupe".

## Resurrection / Prestige

Once the player reaches Level **50**, which is the maximum level, they can perform a **Resurrection Ritual** to "Prestige" and gain new **permanent perks**.

Doing so also resets their **ExP and Level** back to **0**.

These perks would be provided by **content packs**.

### Resurrection Ritual

W.I.P.

### Void Ban Integration

If the player manages to complete a **Resurrection** while **Void Banned** or on less than their **maximum amount of lives**, their lives should **reset** back to the max value and they will be **revived**.

Both of these should have a configuration switch so that server owners can enable or disable this mechanic.
