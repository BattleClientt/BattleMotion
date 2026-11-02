# Security Policy — Battle Motion

## Supported versions

Only the **latest release** on the [releases page](https://github.com/BattleClientt/battlemotion/releases)
receives security fixes. One jar covers the full supported server range
(Paper 1.9.4 → 26.x, plus Folia), so updating is always a drop-in
replacement — there are no maintained older lines to backport to.

| Version | Supported |
| ------- | --------- |
| Latest release | ✅ |
| Anything older | ❌ — update to the latest jar |

## Reporting a vulnerability

Please **do not open a public issue** for security problems.

Use GitHub's private vulnerability reporting instead:
[Report a vulnerability](https://github.com/BattleClientt/battlemotion/security/advisories/new)
(Security tab → *Report a vulnerability*). Reports are private to the
maintainer until a fix ships.

Include what you can of:

- the Battle Motion version (`/version BattleMotion`) and server version/platform,
- what an attacker can do (e.g. crash the server, bypass reach validation,
  spoof another player's packets), and
- reproduction steps or a proof-of-concept.

You can expect an acknowledgement within a week. Confirmed vulnerabilities
are fixed in the next release, and the advisory is published once the fix
is available.

## Scope notes

- Battle Motion parses raw inbound packets on the netty thread (the parse rim) —
  malformed-packet handling and anything reachable pre-authentication are
  **in scope** and especially interesting.
- The server's own libraries (netty, snakeyaml, guava, gson provided by
  Paper at runtime) are **out of scope** here — Battle Motion compiles against but
  never bundles them; vulnerabilities in those belong upstream with
  [PaperMC](https://github.com/PaperMC/Paper/security). Dependencies that
  ARE shipped inside `BattleMotion-<version>.jar` (PacketEvents, Adventure,
  bStats) are in scope.

Battle Motion is a fork of [Mental](https://github.com/owengregson/Mental) by Owen Gregson (MIT).
Issues that also affect upstream Mental are welcome here, but report them to the upstream
project as well so both lines get fixed.
