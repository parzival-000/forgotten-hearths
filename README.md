# Forgotten Hearths

Forgotten Hearths is a small exploration and restoration mod for Minecraft 26.2 and NeoForge. It was made for the CurseForge ModJam 2026 theme **Echoes of the Past**.

Abandoned homesteads hide in forests, plains, meadows, taiga, and windswept country. Their hearths are cold, but the homes are not entirely lost.

## The restoration loop

1. Find a woodland cottage, plains farmstead, or taiga shelter in newly generated terrain.
2. Use its Forgotten Hearth to wake the first echo and learn what it needs.
3. Carry four logs, four bricks, and flint and steel. Use the hearth again to rekindle it.
4. Search the ruin's barrel for a blackened kettle and cracked crock. Repair them at a crafting table. Make patchwork cloth from wool and string.
5. Use the correct repaired item on each of the three Household Remnants.
6. Finish the home to earn a recipe fragment, three old household recipes, and the Comforted benefit around its restored hearth.

Restoration is shared by the world. There is no ownership claim or private loot instance. The server records each homestead's identity, stage, completed tasks, and reward state.

## Features

- Three compact abandoned homestead variants
- A persistent four-stage hearth with warm lighting, particles, sound, and clear status messages
- Six household relics and restoration components
- Three visible restored furnishings: Hearth Kettle, Mended Crock, and Patchwork Quilt
- Hearth Porridge, Root Stew, and Honeyed Oatcake recipes
- A four-step advancement story
- Comforted, a restrained refuge effect that heals half a heart every five seconds while the player is well fed
- Server configuration for generation frequency, material costs, and Comforted values
- Safe marker replacement that refuses to change unrelated blocks

## Controls

Forgotten Hearths uses normal Minecraft controls.

- **Use** the hearth with an empty hand to discover it.
- **Use** the discovered hearth with flint and steel while the required logs and bricks are in your inventory.
- **Use** each Household Remnant while holding the matching Mended Kettle, Mended Crock, or Patchwork Cloth.
- **Use** a rekindled hearth at any time to see how many tasks remain.

## Requirements and building

- Minecraft 26.2
- NeoForge 26.2.0.25-beta or newer in the 26.2 line
- Java 25 for development

Build from the project root:

```powershell
.\gradlew.bat build
```

The development JAR is written to `build/libs/forgotten_hearths-0.1.0.jar`.

NeoForge 26.2 is currently a beta line. This project pins NeoForge 26.2.0.25-beta so builds stay reproducible while that line changes.

Useful development checks:

```powershell
py -3 tools\validate_resources.py
.\gradlew.bat runData
.\gradlew.bat runGameTestServer
.\gradlew.bat runServer -PpersistenceProbe=seed
.\gradlew.bat runServer -PpersistenceProbe=verify
.\gradlew.bat runServer -PworldgenProbe=true -PvalidationPort=25566 -PvalidationWorld=worldgen_validation
```

The persistence commands intentionally use the same ordinary server world. The worldgen command should use a fresh validation world name.

## More information

- [Configuration](docs/CONFIGURATION.md)
- [Manual testing](docs/MANUAL_TESTING.md)
- [Screenshot checklist](docs/SCREENSHOT_CHECKLIST.md)
- [Known limitations](KNOWN_LIMITATIONS.md)
- [Future ideas](docs/FUTURE_IDEAS.md)
- [Changelog](CHANGELOG.md)

## Credits

Created by Parzival000 for CurseForge ModJam 2026. Built with NeoForge and Minecraft's data-driven resource systems. All Forgotten Hearths code, structure templates, textures, models, writing, and gameplay are original to this project. No third-party art or published mod content is included.

Forgotten Hearths is released under the [MIT License](LICENSE).
