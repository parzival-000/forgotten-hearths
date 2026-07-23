# Configuration

Forgotten Hearths creates `forgotten-hearths-server.toml` in the active world's server config directory. Dedicated servers control these values for every player.

## World generation

| Setting | Default | Range | Purpose |
| --- | ---: | ---: | --- |
| `worldgen.generateHomesteads` | `true` | `true` or `false` | Allows homesteads to generate in new chunks. Turning this off does not remove existing sites. |
| `worldgen.homesteadSpacing` | `52` | `24` to `160` | Sets the average structure grid spacing in chunks. Larger values make sites rarer. |

## Restoration

| Setting | Default | Range | Purpose |
| --- | ---: | ---: | --- |
| `restoration.requiredLogs` | `4` | `1` to `16` | Logs consumed when a discovered hearth is rekindled. Any item in the vanilla logs tag counts. |
| `restoration.requiredBricks` | `4` | `1` to `16` | Bricks consumed when a discovered hearth is rekindled. |

## Comforted

| Setting | Default | Range | Purpose |
| --- | ---: | ---: | --- |
| `comfort.enabled` | `true` | `true` or `false` | Enables the completed-hearth benefit. |
| `comfort.radius` | `6.0` | `2.0` to `16.0` | Radius around a restored hearth. |
| `comfort.refreshTicks` | `40` | `20` to `200` | How often the hearth refreshes the effect. |
| `comfort.healIntervalTicks` | `100` | `40` to `600` | Time between half-heart healing pulses while the player has at least 18 food points. |

Twenty ticks are approximately one second. Comforted does not stack into stronger levels when several restored hearths overlap.

Restart the server after changing the file. World generation changes affect only chunks generated after the new setting is active.
