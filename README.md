# last-mob-standing
A hide-and-seek minigame plugin for [Bukkit]-based [Minecraft] servers

## Requirement
* [SpigotMC] 1.12
* [iDisguise] 5.6.3 (on `plugins` directory)

## Build
Use [Gradle Wrapper]!

## Permission
| Name | Description | Default |
| :--: | :---------: | :-----: |
| `lastmobstanding.use` | Allows you to control game | `OP` |

## Command
### `/lms`
- **Permission**: `lastmobstanding.use`
- **Aliases**: `/lastmob`, `/lastmobstanding`

#### `/lms start`
Elects an _attacker_ (others are _pig_) and starts the game.

- **Pigs** should kill the attacker.
- **Attacker** should kill whole pigs.

##### Side-effects
- Sets the world's [gamerules][Gamerule]
  - `doMobLoot` to `false`
  - `doMobSpawning` to `false`
  - `doDaylightCycle` to `false`
  - `keepInventory` to `true`
  - `naturalRegeneration` to `false`
- Changes `@a` gamemode to [Adventure]
- Clears entire items from `@a` inventory
- Teleports `@a` to the [spawn position][Spawn]
- [Disguise](iDisguise) `@a` to pig or zombie (for _attacker_).

#### `/lms stop`

## License
[MIT License](LICENSE)

Copyright (c) 2017 Chalk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


[Minecraft]: https://minecraft.net
[Bukkit]: https://bukkit.org
[SpigotMC]: https://www.spigotmc.org
[iDisguise]: https://github.com/robingrether/iDisguise
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html


[Adventure]: http://minecraft.gamepedia.com/Adventure
[Spawn]: http://minecraft.gamepedia.com/Spawn#World_spawn
[Gamerule]: http://minecraft.gamepedia.com/Commands#gamerule
