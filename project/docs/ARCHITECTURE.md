# Battle Motion — architecture

Latency-compensated 1.7.10 combat for Paper 1.9.4 → 26.x (+ Folia).

Battle Motion is a fork of [Mental](https://github.com/owengregson/Mental) by Owen
Gregson (MIT). Java package names (`me.vexmc.mental…`) and relocation prefixes are
intentionally kept as in upstream so upstream fixes stay easy to merge.

## Modules

Multi-module Gradle: `api` / `kernel` / `platform` / `core` / `compat-folia` /
`tester`.

- `kernel` is **pure JDK** (no Bukkit, no PacketEvents — asserted at build).
- `platform` is the Bukkit-facing seam (scheduling, capabilities, NMS resolvers)
  over the kernel.
- `core` is the plugin (shades PacketEvents + bStats, folds in `compat-folia` by
  name behind `Capabilities.folia()`).
- `tester` is the in-server integration harness.

The version lives once in `gradle.properties`. The build tree lives under
`modules/` and all non-code project material (docs, assets, scripts,
`project/support-matrix.json`) under `project/`; the repo root holds only those
two directories plus config and the Gradle wrapper.

## One jar for the whole range

The full range 1.9.4 → 26.x ships as **one jar**, with no holes (1.14.4
included) and no server flags. `core` compiles against the 1.17.1 API floor but
runs down to 1.9.4: every sub-floor absence is handled by boot-time probing at
the platform seam (never scattered version conditionals), so absent APIs resolve
to era-correct fallbacks printed in the boot report (material/enchantment names,
the six Phase-5.5 resolvers, PDC). Text sinks route through `TextPort`
(relocated Adventure → legacy strings; no `Component` crosses a Bukkit
boundary), and the latency probe rides window-confirmation TRANSACTIONs below
1.17 where the play PING/PONG channel is absent. The kernel stays
modern-vocabulary and version-blind.

Classloading down to Java 8 is an **artifact-level** fork (no source touched):
`core` and `tester` each ship ONE **Multi-Release mega-jar** — `shadowJar` →
jvmdg `DowngradeJar` (class-v52 base + `multiReleaseOriginal` original v61 under
`META-INF/versions/17`) → `ShadeJar` (distinct per-plugin runtime prefixes,
`me/vexmc/mental/lib/jvmdg` vs `…/tester/lib/jvmdg`) → the canonical jar
(`BattleMotion-<version>.jar`).

Four gates run in `check`: `verifyDowngrade`, `verifyJdk8Api`,
`verifyTesterIsolation`, `verifyRelocation`.

Every legacy Paper build runs its **native-era Java** — the newest it boots
flagless, per-entry `jdk` in `project/support-matrix.json`; the tester asserts
the actually-loaded tier per entry against its `bytecodeTier` (≥ Java 17 reads
v61, below reads v52).

**D-8 rule:** no post-Java-8 JDK type may cross a cross-plugin API descriptor
(jvmdg rewrites Java-9+ types INTO descriptors and prunes its runtime per-jar —
the kernel's Java-8-native `computeBase(…, java.util.Random)` overload is the
precedent).

## The delivery core

Three single-writer domains, communicating only by immutable values:

- **D1 connection** (per player, the netty read thread): `SprintWire`
  (arrival-order sprint + freshness), `GroundFsm` (jump stamps), CPS window,
  position ring. Reads only its own inbound packets, the `TickClock`, and
  *published* views — never a live entity, never another player's state.
- **D2 session** (per player, the region thread): one `CombatSession` holds the
  `MotionLedger`, live `HitTransaction`s, and kinematics. A per-player 1-tick
  task drains its inbox, decays the ledger, and **publishes** one immutable
  `PlayerView` (`AtomicReference.set`) — the state as of the END of the previous
  tick, the boundary read the era ordering depends on.
- **D3 global** (global/main): config `Snapshot` swap, the feature reconciler,
  the `TickClock` implementation, the entityId→UUID index.

The `DeliveryDesk` (kernel) is the sole `PlayerVelocityEvent` writer, the sole
`MotionLedger` writer, and it writes the delivery **journal** — the single "what
did we actually ship" seam the tester asserts against. The quantized **valve**
consumes exactly one duplicate ENTITY_VELOCITY per pre-sent knock. The netty
realm's only Bukkit-adjacent code is the packet parse **rim**
(`me.vexmc.mental.v5.rim`), pinned by an architecture test.

## Invariants

- **Zero-touch**: a disabled feature does NOTHING to the game.
- **Era-exact no-op defaults**: new knobs default to byte-identical legacy
  behavior; `parse(empty) == LEGACY_17`.
- **Kernel is additive-only** and Bukkit-free (both build-time asserted).
- **Never touch the client-side technique contract** (0.6 self-multiplier,
  w-tap, jump-resets).
- Battle Motion owns knockback + hit delivery (the always-on `DELIVERY` +
  `KNOCKBACK` `Feature` families), and can OPTIONALLY own combat rules via the
  default-OFF `DAMAGE` (armour strength/durability, tool durability, critical
  hits, sword blocking), `CADENCE` (cooldown, sweep, sounds), `SUSTAIN` (golden
  apples, ender-pearl cooldown, player regen, potion durations/values), and
  `LOADOUT` (offhand, crafting, era hitbox reach) families — 16 standalone rule
  `Feature`s (historically ported from OldCombatMechanics). All default OFF;
  zero-touch and era-exact-no-op defaults hold. Battle Motion ALWAYS applies its
  own mechanics — there is no OCM coexistence/yielding; the rule features are
  combat-rules-plugin-agnostic (enabling the same rule in two plugins
  double-applies — pick one per rule).

## Verification gate

```bash
./gradlew build                  # unit tests (+ japicmp, + kernel-Bukkit-free) — always first
./gradlew integrationTestMatrix  # sequential: every paper + folia server, one machine
```

The matrix runs **sequentially** on one machine (every server binds the same
port); `project/scripts/integration-matrix.sh` is the concurrent local variant.
**The nonce is the honesty rule**: each Gradle run stamps a fresh UUID into the
boot, the tester echoes it into `test-results.txt`, and the check task accepts
ONLY that nonce — a leftover result from an earlier boot fails the check
structurally, so it can never masquerade as this run's PASS. Never trust the
"BUILD SUCCESSFUL" banner alone; the nonce + PASS check is what makes the gate
trustworthy.

## Conventions

Conventional commits with prose bodies; immutable records + atomic config (one
`Snapshot` swapped by reference; the GUI writes a machine overlay, the human YAML
is never re-serialized); pure kernel math with hand-computed unit pins; netty
threads read only published `PlayerView`s; entity work via `Scheduling.runOn` /
`runGlobal` (Folia-correct); imports never inline-qualified; comments explain the
why.
