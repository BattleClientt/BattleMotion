<!-- v1.0.0-beta.1 -->
- First Battle Motion release: a fork of Mental 2.12.0-beta by Owen Gregson, set up for
  the Battle Client ecosystem. Combat behaviour (knockback, damage, hit registration,
  presets) is unchanged from the fork base.
- The plugin is now called Battle Motion. Use `/battlemotion`; `/mental` and `/mtl` still
  work as aliases.
- Settings live in `plugins/BattleMotion/`. If you are coming from Mental, your
  `plugins/Mental/` folder is copied over automatically on first boot (the old folder is
  left untouched).
- Permissions are now `battlemotion.command.use`, `.reload` and `.debug`. Existing
  `mental.command.*` grants keep working.
- No usage statistics are sent: the bStats hook is switched off until Battle Motion has
  its own bStats id.
