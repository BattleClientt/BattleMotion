<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="project/assets/battlemotion-hero-dark.png">
    <img src="project/assets/hero.svg" width="860" alt="Battle Motion — classic combat, engineered">
  </picture>
</p>

<p align="center">
  <a href="../../releases/latest"><img src="project/assets/buttons/download.svg" height="46" alt="Download latest"></a>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <a href="../../releases"><img src="project/assets/buttons/releases.svg" height="46" alt="All releases"></a>
</p>

<p align="center">
  <b>Classic 1.7.10 combat for modern Paper &amp; Folia.</b><br>
  The hits, knockback, and combos of golden-era PvP, latency-compensated, friendly to
  anticheats, and tuned from the era's real game code rather than folklore.
</p>

<br>

<p align="center">
  <img src="project/assets/headers/features.svg" height="54" alt="Features">
</p>

<table>
<tr>
<td width="50%" valign="top">
<img src="project/assets/icons/knockback.svg" width="40" alt=""><br>
<b>Precision-tuned Knockback</b><br>
Choose the legacy 1.7.10 formula or the modern one, with presets for both.
<code>signature</code>, Battle Motion's own, feels close to <code>mmc</code> knockback and suits
practice servers well.
</td>
<td width="50%" valign="top">
<img src="project/assets/icons/hitreg.svg" width="40" alt=""><br>
<b>Async Hit Registration &amp; Knockback</b><br>
Attacks are intercepted on the netty thread and register sub-tick, in packet-arrival
order. Combat runs faster than vanilla.
</td>
</tr>
<tr>
<td width="50%" valign="top">
<img src="project/assets/icons/latency.svg" width="40" alt=""><br>
<b>Fair at any ping</b><br>
Latency compensation keeps knockback and combos identical at 5&nbsp;ms or 150&nbsp;ms.
Everything is server-authoritative, so anticheats verify it cleanly.
</td>
<td width="50%" valign="top">
<img src="project/assets/icons/jar.svg" width="40" alt=""><br>
<b>One jar, every server</b><br>
One download covers every Paper build from 1.9.4 to 26.x plus Folia, on Java 8 or newer
with no flags. Every release passes a live test matrix across every supported server.
</td>
</tr>
<tr>
<td width="50%" valign="top">
<img src="project/assets/icons/ruleset.svg" width="40" alt=""><br>
<b>The full 1.8 ruleset (optional)</b><br>
A library of switchable modules: old armour, no attack cooldown, 1.8 crits, golden apples,
sword blocking, and more. <i>Everything here is off by default.</i>
</td>
<td width="50%" valign="top">
<img src="project/assets/icons/gui.svg" width="40" alt=""><br>
<b>Run it from in-game</b><br>
<code>/mental</code> opens a full management menu where you can flip modules and switch
presets live, without a restart. Everything is still plain, documented YAML.
</td>
</tr>
</table>

<br>

<p align="center">
  <img src="project/assets/headers/getting-started.svg" height="54" alt="Getting started">
</p>

1. **Download** `BattleMotion-<version>.jar` from [the latest release](../../releases/latest).
2. **Drop it** into your server's `plugins/` folder.
3. **Restart.** That's it. The defaults are the classic combat, so there is nothing to set up.

Configuration is generated under `plugins/BattleMotion/` on first boot, and every option is
documented inside its own file. Upgrading from an old single-file config? It migrates
automatically: your tuned values become `profiles/custom.yml` (and stay selected), and the
original is kept as `config-v1-backup.yml`.

> **No dependencies.** Battle Motion needs nothing else installed; PlaceholderAPI is supported,
> but optional.

<br>

<p align="center">
  <img src="project/assets/headers/modern-legacy.svg" height="54" alt="Modern or legacy knockback">
</p>

Battle Motion ships two complete knockback calculations. Pick the one your server should feel like:

- **Legacy** is the true 1.7.10 formula: sprint resets, residual stacking, era verticals.
  It comes with a full preset library in `profiles/legacy/`, and every knob is tunable.
- **Modern** is the current vanilla formula with the same customization surface. Its presets
  live in `profiles/modern/`: `modern-vanilla`, `modern-uplift`, `modern-combo`, and `ct8c`
  (Java Edition Combat Test 8c knockback).

Both sides expose the full knob set (pushes, friction, verticals, air multipliers, w-tap
bonuses), so `custom` can start from either world.

<br>

<p align="center">
  <img src="project/assets/headers/presets.svg" height="54" alt="Knockback presets">
</p>

The knockback profile is one server-wide setting; pick it in-game or in `knockback.yml`.

| Preset | The feel |
|---|---|
| `signature` | Battle Motion's own tuning, and the recommended default for competitive 1.8-style PvP. |
| `lunar` | The archived Lunar Network values: a lighter, floatier trade. |
| `badlion` | The classic Badlion feel, with firm horizontals and honest verticals. |
| `velt` | A modern practice-server tuning, snappy and consistent. |

More ship alongside: `kohi`, `minehq`, `mmc`, `legacy-1.7`, `legacy-1.8`, and `custom`
(yours to edit) in `profiles/legacy/`, plus `modern-vanilla`, `modern-uplift`, `modern-combo`,
and `ct8c` in `profiles/modern/` for servers on the modern knockback formula. The full guide
to what each knob does lives in [project/docs/knockback-profiles.md](project/docs/knockback-profiles.md).

<br>

<a id="faster-than-vanilla"></a>
<p align="center">
  <img src="project/assets/headers/faster.svg" height="54" alt="Faster than vanilla">
</p>

**Vanilla waits twice.** An attack packet queues for the next tick, then the knockback waits
for the next entity-tracker pulse. That is 10 to 50 ms of server-side dead time before the
victim feels anything, on top of ping.

Battle Motion removes both waits:

- Attacks are read the moment they arrive: intercepted on the netty thread and validated
  against per-tick snapshots, so the attack never waits on the main thread.
- The victim's knockback and both players' hurt animations ship straight from the netty
  thread, one round-trip leg earlier than vanilla.
- On 1.19.4+ the velocity and hurt animation travel in one packet bundle, so the knock and
  the flinch can never split across frames.
- Registration runs async to the server core, so if TPS drops, hits still register at full
  speed, exactly as they would on a server running at optimal performance.
- Damage still runs on the main thread through the full vanilla event chain, and the
  pre-send uses the same profile math, so the prediction and the authoritative result never
  disagree.

The full pipeline, edge cases included, is written up in
[project/docs/fast-path.md](project/docs/fast-path.md).

<br>

<p align="center">
  <img src="project/assets/headers/ingame.svg" height="54" alt="In-game management">
</p>

`/battlemotion` (aliases `/mental` and `/mtl`) opens the management suite, restricted to operators by default. Each
feature family gets its own colour-coded screen, on chrome that renders in full colour right
down to 1.9.4:

- The **Dashboard** is a five-tile home: Combat Engine, Era Rules, Sustain & Potions,
  Effects & Loot, and Server & Diagnostics, flanked by Combat Presets and Close. Each tile
  opens its category's sections, which keeps every family two clicks away without a wall of
  buttons.
- **Family screens** flip any module live. Right-click a module to open its settings screen
  and tune it in-game (left/right steps a value, Q resets it to the file default).
- The **Preset Gallery** is where you browse and apply knockback profiles (legacy and modern
  tabs) and combat-effects tunes, each with a live preview.
- **Combat Presets** applies a whole ruleset at once: the Combat Test 8c snapshot, the
  classic 1.7 feel, or Battle Motion-transparent vanilla.
- Nothing needs a restart. Changes apply immediately, and `/battlemotion reload` re-reads the
  files from console.

Your hand-edited YAML is never rewritten: in-game changes are stored as a separate overlay,
so comments and formatting in the files you maintain stay exactly as you left them.

<br>

<p align="center">
  <img src="project/assets/headers/configuration.svg" height="54" alt="Configuration">
</p>

Everything lives under `plugins/BattleMotion/`, split by topic, and each file documents every key
it holds:

| File | What it controls |
|---|---|
| `config.yml` | Module toggles (incl. the 1.8 &amp; Combat Test 8c ruleset), metrics, debug, anticheat. |
| `knockback.yml` | The selected knockback profile. |
| `profiles/` | The preset library (`legacy/` and `modern/`); add your own here. |
| `bundles/` | Rules bundles: apply a whole ruleset (`ct8c`, `signature`, `vanilla`) at once. |
| `hit-registration.yml` | Hit registration, the fast path, and reach validation. |
| `latency-compensation.yml` | Ping measurement and latency correction. |
| `combo.yml` | The combo solver family. |
| `loadout.yml` | Off-hand &amp; crafting restrictions, era hitboxes &amp; reach. |
| `effects.yml` + `effects/presets/` | Combat effects and their presets. |
| `pots.yml` | Splash-potion utilities. |
| `drop-protection.yml` | Loot protection. |

<br>

<p align="center">
  <img src="project/assets/headers/ruleset.svg" height="54" alt="The 1.8 ruleset">
</p>

Battle Motion always owns hit delivery and knockback. Everything below is opt-in, off by default,
and grouped by family, with each rule a one-line toggle:

| Family | What turning it on does |
|---|---|
| **Damage** | 1.8 armour strength &amp; durability, old critical hits, old tool durability, sword blocking. |
| **Combat Cadence** | Removes the 1.9 attack cooldown, sweep attacks, and swing sounds. |
| **Sustain** | 1.8 golden apples, potion values &amp; durations, old regen, no ender-pearl cooldown. |
| **Loadout** | Off-hand and crafting restrictions, era hitboxes and reach. |
| **Combo Solver** | Holds the sweet-spot combo distance on the fresh knock; optional reach handicap. |
| **Potions** | `/potfill` and steep-throw instant pots. |
| **Combat Effects** | Hit sounds and particles, pop-off damage indicators, death effects. |
| **Loot Protection** | A slain player's drops reserved for their killer, gold-glowing until they expire. |

> **Running another combat-rules plugin?** Enable each rule in exactly one plugin; the same
> rule enabled twice applies twice.

> **Combat Test 8c.** A port of Mojang's experimental *Combat Test 8c* snapshot ships as
> thirteen more opt-in modules (reworked shields, charged attacks, per-weapon attack speeds,
> the split-vertical knockback, and more), with a matching `ct8c` knockback profile. Apply
> the whole set in one click from **Combat Presets**, or turn each module on by itself.

<br>

<p align="center">
  <img src="project/assets/headers/faq.svg" height="54" alt="FAQ">
</p>

**Does it work with anticheats?**
Yes. Combat is server-authoritative, so movement-prediction anticheats verify it cleanly.
When one is detected, Battle Motion stands down its packet-level fast path automatically and stays
correct.

**Do my players need a mod or a specific client?**
No. Everything is server-side; vanilla clients, Lunar, Badlion, and 1.7-animation mods all
just work.

**Can I keep modern 1.9+ combat and only fix knockback?**
Yes, that's the default install. The 1.8 ruleset is entirely opt-in; out of the box, Battle Motion
changes knockback, hit registration, and latency fairness only.

**Does it replace OldCombatMechanics?**
It can. The optional ruleset covers the OCM rule set. Pick one plugin per rule and Battle Motion
handles the rest natively, including the parts OCM can't touch (delivery and knockback).

**Folia?**
Supported. Every release is gated on a real Folia server before it ships.

**Something feels off. How do I debug it?**
`/battlemotion debug subscribe` streams what Battle Motion sees (hits, knocks, sprint reads) to your chat
in real time. You can also open the **Debug** screen in `/battlemotion` and hit *Stream to my chat*.

<br>

<p align="center">
  <img src="project/assets/headers/compatibility.svg" height="54" alt="Compatibility">
</p>

| | |
|---|---|
| **Server** | Paper 1.9.4 → 26.x (every version) · Folia |
| **Java** | 8 or newer, whatever your server already runs; no flags needed |
| **Dependencies** | None (PlaceholderAPI optional) |
| **Tested** | Every supported Paper version + Folia boots live in the release gate, every release |

One jar covers the whole range: modern JVMs load Battle Motion's Java-17 code, and older JVMs load
a byte-equivalent Java-8 build packed into the same file. On 1.9.4 to 1.10.2 a handful of
trajectory self-tests are skipped, because servers of that era don't simulate offline
players' motion; gameplay is unaffected.

<br>

<p align="center">
  <img src="project/assets/headers/developers.svg" height="54" alt="For developers">
</p>

Modules: `api` (public surface) · `kernel` (pure-JDK combat model) · `platform` (Bukkit seam)
· `core` (the plugin) · `compat-folia` · `tester` (live-server harness).

The **public API**, `dev.battleclient:battlemotion-api` (generation 3), covers combo lifecycle events, an
authoritative combat-state query service, capability discovery, knockback outcome control,
and temporary per-(victim, attacker) hit-timing overrides, which let a companion plugin
re-price a victim's hurt window for one attacker inside Battle Motion's own timing (see
[project/docs/api-timing-overrides.md](project/docs/api-timing-overrides.md)). The jar ships
with every release; the contract lives in
[project/docs/api-gen3-integration-surface.md](project/docs/api-gen3-integration-surface.md)
with rulings in [project/docs/api-gen3-rulings.md](project/docs/api-gen3-rulings.md).

```bash
./gradlew build                  # unit tests + compatibility gates
./gradlew integrationTestMatrix  # every supported server, live
```

Deep dives: [fast path](project/docs/fast-path.md) · [knockback profiles](project/docs/knockback-profiles.md)
· [combo hold](project/docs/combo-hold.md) · [legacy tier](project/docs/legacy-combat.md) · the combat
research ledger in [project/docs/research/](project/docs/research/).

## Credits

Battle Motion is a fork of **[Mental](https://github.com/owengregson/Mental)** by
**Owen Gregson** (MIT), adapted for the Battle Client ecosystem. The combat model,
knockback engine and hit-registration work are Owen's; his copyright and license are
kept in [`LICENSE`](LICENSE). Third-party components are listed in
[`project/THIRD-PARTY-NOTICES.md`](project/THIRD-PARTY-NOTICES.md) and the fork notice in
[`NOTICE`](NOTICE). Plugin package names (`me.vexmc.mental…`) are kept as upstream's so
upstream fixes stay easy to merge.

Licensed [MIT](LICENSE) · third-party notices [here](project/THIRD-PARTY-NOTICES.md) · anonymous
no usage metrics are sent (the bStats hook is disabled until Battle Motion registers its own id).

<br>

<p align="center">
  <img src="project/assets/divider.svg" height="22" alt="">
</p>

<p align="center"><sub><b>Battle Motion</b> by <a href="https://github.com/BattleClientt">Battle Client</a></sub></p>
