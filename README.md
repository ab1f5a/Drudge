# Drudge

A Fabric client mod that takes the tedious, repetitive work off your hands.
Fell a whole tree from the bottom up, bore a straight tunnel in one keypress,
clear out a whole area, and always mine with the best tool in your hotbar.

Built for **Minecraft 26.2** with **Fabric Loader 0.19.5+** and the **Fabric API**.

## Features

- **Tree Felling** — cuts down an entire tree from the bottom up, no climbing.
- **Tunnel Boring** — digs a straight tunnel in the direction you're facing, with
  an optional length limit and automatic torch placement.
- **Area Excavation** — clears every block between two corners you select.
- **Continuous Mining** — removes the pause between blocks so you can keep mining.
- **Auto Tool Switch** — swaps to the best tool (or an empty hand) for whatever
  you're about to break.

## Usage

- **Right Shift** opens the Drudge settings screen, a vanilla-style screen where
  each feature's options can be tuned.
- Every feature has its own toggle keybind under
  **Options → Controls → Key Binds → Drudge**. Toggles are unbound by default —
  bind them to whatever you like.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.2.
2. Put the jar from `build/libs/` into your `mods/` folder, alongside the
   [Fabric API](https://modrinth.com/mod/fabric-api).

## Building

Requires JDK 25.

```sh
./gradlew build
```

The output jar is `build/libs/drudge-1.0.0.jar`.

## License

Licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for
the full text and [NOTICE](NOTICE) for copyright and attribution.

This project is based on [Wurst](https://github.com/Wurst-Imperium/Wurst7),
Copyright (c) 2014-2026 Wurst-Imperium and contributors.
