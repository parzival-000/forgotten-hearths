# Manual Testing Checklist

Use a fresh 26.2 world with NeoForge 26.2.0.25-beta and the current JAR.

## Discovery and world generation

- [ ] Find or locate a homestead in a forest, plains or meadow, and taiga or windswept biome.
- [ ] Confirm all three structure variants can appear and sit sensibly on the terrain.
- [ ] Check every site at noon and dusk for silhouette, lighting, soot, moss, and overgrowth readability.
- [ ] Confirm the barrel contains the two damaged relics and modest supplies.
- [ ] Change `homesteadSpacing`, generate new terrain, and confirm the new spacing applies.
- [ ] Disable generation, enter untouched terrain, and confirm no new homesteads appear.

## Full restoration loop

- [ ] Use a cold hearth and confirm its message names the configured log and brick costs.
- [ ] Try the wrong item, missing materials, and insufficient quantities. Nothing should be consumed.
- [ ] Rekindle with the exact materials and confirm the visual stage, light, sound, and particles change.
- [ ] Repair the kettle and crock, craft patchwork cloth, and return all three to their matching remnants.
- [ ] Try a repaired item on the wrong remnant. It must not be consumed.
- [ ] Put an unrelated block where a test remnant would be and invoke the same restoration path. The block must remain untouched.
- [ ] Finish the third task and confirm the hearth enters its restored stage.
- [ ] Confirm one recipe fragment, 100 experience, and the three food recipe unlocks are granted.
- [ ] Eat both bowl foods and confirm each returns an empty bowl.

## Persistence and multiplayer

- [ ] Leave the area, unload its chunks, return, and confirm the hearth and task state remain correct.
- [ ] Save and stop the server after each stage. Restart and confirm that exact stage returns.
- [ ] Have two players interact with the same hearth and final remnant at nearly the same time.
- [ ] Confirm both players see shared progress and only the completing interaction creates the one-time fragment reward.
- [ ] Reconnect, reload chunks, and repeat interactions. No second completion reward should appear.
- [ ] Restore two nearby hearths and confirm Comforted does not gain extra levels.

## Compatibility and presentation

- [ ] Start a dedicated server with no client classes or resource pack present.
- [ ] Join with two clients and complete a site without desync or ghost blocks.
- [ ] Check every creative-tab item, placed block, particle, effect icon, tooltip, translation, and recipe-book entry.
- [ ] Scan `latest.log` for missing models, textures, translations, loot tables, recipes, structures, or registry entries.
- [ ] Confirm the layout is still usable when a player decorates around the ruin without altering its three remnants.

Automated GameTests cover the complete stage transition, exact marker replacement, one-time reward state, and all three structure-template loads. The checklist above remains the human visual and real multiplayer acceptance pass.
