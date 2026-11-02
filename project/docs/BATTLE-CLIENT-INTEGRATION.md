# Battle Motion × Battle Client — integration report

Status: **report only.** Nothing described under "Proposals" was implemented. Nothing in this
repository was built or tested while writing it (the sandbox that prepared this branch could not
download Gradle or Paper), so every build-related statement below is *unverified* until someone
runs `./gradlew build` locally.

## 1. Which Minecraft version does Battle Motion target?

- **Known (from the repo):** `project/support-matrix.json` lists Paper up to **26.1.2** (plus
  Folia 26.1.2), and the `modern-vanilla` knockback profile is documented as the "byte-exact
  Paper 26.1.2 melee formula".
- **Not confirmed:** whether Minecraft/Paper **26.3** exists yet or what changed in it. The Paper
  docs only use `26.3` as an example string in their API samples; that is not evidence of a
  release. The Paper downloads site could not be queried from the sandbox.
- **How to check (30 seconds):** open `https://fill.papermc.io/v3/projects/paper` in a browser and
  read the `versions` list. If a newer series than 26.1.x is present, add it as a new entry in
  `project/support-matrix.json` and run `./gradlew integrationTestMatrix` before announcing support.
- **Risk to flag:** the modern knockback formula is copied from a specific server version. If the
  Battle Client targets a newer Minecraft than 26.1.2, `modern-*` profiles may differ from vanilla
  on that version until re-derived. The legacy (1.7/1.8) profiles do not depend on this.

## 2. Handshake between the Battle Client and the plugin (proposal — confirm with Renan)

Goal: the client can tell the server "I am the Battle Client" (and which version), and the plugin
can answer with what it is running, without changing any combat math.

Suggested minimal design:
1. A plugin-messaging channel, e.g. `battleclient:handshake` (name to be agreed).
2. Client → server on join: protocol version + client version.
3. Server → client reply: plugin name/version, active knockback profile name, API generation
   (`Mental.MentalApi#apiVersion()` already exposes generation 3).
4. Purely informational at first. Nothing in combat should branch on it until both sides are
   tested together. Unknown/absent handshake = behave exactly as today (zero-touch rule).
5. The existing public API (`dev.battleclient:battlemotion-api`, package `me.vexmc.mental.api`)
   is the natural place for a companion plugin to read the active profile; it is additive-only
   and checked by the `apiCompat` gate.

Open questions for Renan: channel name, who owns the protocol spec, whether the client should be
able to *request* a profile (recommend: no — server stays authoritative), and anti-cheat
interaction (Battle Motion already has an anticheat "posture" setting).

## 3. Default preset (suggestion, not applied)

Shipped default today: `knockback.profile = legacy-1.7` (see `knockback.yml`). The README calls
`signature` the recommended default for competitive 1.8-style PvP. Both are upstream choices.
Nothing was changed. Decision for Battle Client: keep upstream's default, or ship `signature`
(or a Battle Client–tuned profile) as default. Changing a default is a gameplay change and should
be field-tested first.

## 4. Portuguese (pt-BR) translation — estimate

- Player/admin-facing text is **hard-coded English in Java** (GUI titles, lore, command messages,
  log lines) — roughly **420 string literals** in `modules/core/src/main/java` were found by a
  rough pattern search, plus long English comments in the shipped YAML files. There is no
  message-file/i18n system today.
- Translating only visible GUI + command text without an i18n layer would mean editing many Java
  files and diverging from upstream on almost every merge.
- Recommended order: (1) introduce a small `messages_<lang>.yml` lookup in one PR (no behaviour
  change), (2) move strings over gradually, (3) add `pt_BR`. Rough effort: a few days for
  the layer + strings; translation itself is the smaller part. Config comments can stay English.

## 5. Merging future upstream (Mental) changes

- Remote `upstream` points to `https://github.com/owengregson/Mental.git` (added, **not
  fetched**). Fork base: Mental 2.12.0-beta.
- Package names (`me.vexmc.mental…`) and relocation prefixes were deliberately **not** renamed,
  so source-level merges stay mostly clean.
- Expect conflicts in files this fork edits on purpose: `gradle.properties` (version, history
  moved to `project/docs/UPSTREAM-HISTORY.md`), `plugin.yml`, `README.md`, `LICENSE`,
  `.github/**`, `settings.gradle.kts`, `modules/api/build.gradle.kts`, `Brand.java`, the GUI
  title strings, and the command/permission code. Combat code is untouched and should merge clean.
- Upstream release tags (2.x) must never be reused; this fork's line is `1.x`.

## 6. Things the repository owner must know

- **Merging to `main` publishes a release** (`release.yml` runs on push to `main` when the
  version has no tag). First version: `1.0.0-beta.1` → tag `v1.0.0-beta.1`, GitHub pre-release.
- GitHub Actions are usually **disabled by default on forks**; enable them deliberately.
- Scheduled workflows `scorecard.yml` and `codeql.yml` run weekly once Actions are on; remove
  them if they are not wanted. `release.yml` needs `contents: write` for the release job.
- Releases may be *immutable* (the workflow is written for that); do not try to re-upload assets.
- bStats is disabled until Battle Motion registers its own plugin id on bstats.org (upstream's id
  31788 belongs to Owen Gregson and must not be used).
- Cosmetic leftovers: banner/hero SVG images under `project/assets/` still show upstream
  artwork/text; replace when Battle Client artwork exists.
