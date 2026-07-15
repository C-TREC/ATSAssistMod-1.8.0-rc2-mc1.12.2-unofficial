# Modifications

## Status

This repository is an unofficial modified version of ATSAssistMod.
It is not affiliated with, endorsed by, or supported by the original author.

## Upstream

- Modified source: https://github.com/Chishiya-Teikoku-Daigakuin/ATSAssistMod-1.8.0-rc2-mc1.12.2-unofficial
- Original project: https://github.com/Kai-Z-JP/ATSAssistMod
- Original author: Kaiz_JP
- Base branch used for the Minecraft 1.12.2 port: `1.12.2`
- Ported feature source: `v1.8.0-rc.2`

## License

ATSAssistMod is distributed under the GNU General Public License version 3.
This modified version keeps the same GPL-3.0 license. See [LICENSE](LICENSE).

## Modification summary

- Ported ATSAssistMod v1.8.0-rc.2 changes to the Minecraft 1.12.2 source tree.
- Added the v1.8.0-rc.2 IFTTT serialization and TrainDirection-related changes.
- Adapted the packet and RTM rail API handling for the Minecraft 1.12.2/RTM 2.4.x codebase.
- Updated the Gradle build metadata for `v1.8.0-rc.2` and Minecraft 1.12.2.
- Updated 1.12.2 dependencies to NGTLib file `4641592` and RealTrainMod file `4641603`.
- Kept compatibility fallback reading for older IFTTT JSON string NBT data.

## Support note

Please do not report issues caused by this modified version to the upstream ATSAssistMod project unless the same issue can be reproduced with an official upstream release.
