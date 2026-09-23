# Drudge

A Fabric client mod that takes the tedious, repetitive work off your hands.
Fell a whole tree from the bottom up, bore a straight tunnel in one keypress,
clear out a whole area, and always mine with the best tool in your hotbar.

Drudge is a client-side helper, not a cheat: it automates work you were going to
do anyway, and leaves you in control of every action it takes.

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

## Supported versions

Each Minecraft version lives on its own branch, with no shared history, so a
change for one version can never break another. Pick the branch matching your
game version:

| Minecraft | Branch | Release |
| --- | --- | --- |
| 1.21.1 | [`mc/1.21.1`](https://github.com/ab1f5a/Drudge/tree/mc/1.21.1) | `v1.0.0+1.21.1` |
| 1.21.10 | [`mc/1.21.10`](https://github.com/ab1f5a/Drudge/tree/mc/1.21.10) | `v1.0.0+1.21.10` |
| 1.21.11 | [`mc/1.21.11`](https://github.com/ab1f5a/Drudge/tree/mc/1.21.11) | `v1.0.0+1.21.11` |
| 26.1.1 | [`mc/26.1.1`](https://github.com/ab1f5a/Drudge/tree/mc/26.1.1) | `v1.0.0+26.1.1` |
| 26.1.2 | [`mc/26.1.2`](https://github.com/ab1f5a/Drudge/tree/mc/26.1.2) | `v1.0.0+26.1.2` |
| 26.2 | [`mc/26.2`](https://github.com/ab1f5a/Drudge/tree/mc/26.2) | `v1.0.0+26.2` |
| 26.3 | [`mc/26.3`](https://github.com/ab1f5a/Drudge/tree/mc/26.3) | `v1.0.0+26.3` |

Every version is built against the same feature set. The 1.21.x branches draw
their lines with vanilla's line renderer rather than a bundled shader, so
outlines there are thinner than on 26.x.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for your
   Minecraft version.
2. Download the release for that version, and put its jar into your `mods/`
   folder alongside the [Fabric API](https://modrinth.com/mod/fabric-api).

## Building

Check out the branch for the version you want, then build it. The 1.21.x
branches need JDK 21; the 26.x branches need JDK 25.

```sh
./gradlew build
```

The output jar lands in `build/libs/`.

## License

Licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for
the full text and [NOTICE](NOTICE) for copyright and attribution.

Some source codes refer to [Wurst](https://github.com/Wurst-Imperium/Wurst7)
