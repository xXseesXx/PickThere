# PickThere Mod

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.7.10-green.svg)](https://minecraft.net/)
[![Forge Version](https://img.shields.io/badge/Forge-10.13.4.1558+-orange.svg)](https://files.minecraftforge.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A Minecraft Forge mod that adds an item to directly store picked-up items in selected blocks with visual wireframe highlighting.

![PickThere Device Demo](https://via.placeholder.com/800x400/2d2d2d/ffffff?text=PickThere+Device+Demo)

## 📋 Table of Contents

- [Features](#-features)
- [Installation](#-installation)
- [How to Use](#-how-to-use)
- [Configuration](#-configuration)
- [Building from Source](#-building-from-source)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

- **🎯 Pick There Device**: A special item that allows you to select unlimited inventory blocks (chests, barrels, etc.)
- **👁️ Visual Selection Highlighting**: Selected inventories are highlighted with golden wireframe outlines (similar to X-ray mods)
- **🧠 Smart Item Distribution**: Automatically distributes picked-up items to selected inventories with intelligent stacking logic
- **⚡ Easy Selection Management**: Simple shift+right-click interface to toggle inventories in selection
- **🔄 Dual Selection Types**: Regular inventories (gold wireframes) and same-item-only inventories (red wireframes)
- **🎛️ Pickup Toggle**: Enable or disable automatic pickup redirection without losing your selections (item texture changes to show status)
- **📏 Distance Control**: No distance limit by default (configurable)
- **🔧 Easy Reset**: Craft the device by itself to clear all selections and reset the device

## 📦 Installation

### Requirements
- Minecraft 1.7.10
- Minecraft Forge 10.13.4.1558 or higher

### Steps
1. Download and install [Minecraft Forge](https://files.minecraftforge.net/) for version 1.7.10
2. Download the latest PickThere mod jar from the [Releases](../../releases) page
3. Place the jar file in your `mods` folder
4. Launch Minecraft

## 🎮 How to Use

### Crafting the Pick There Device

The Pick There Device is crafted using:
```
I G I
G E G
I G I
```
Where:
- I = Iron Ingot
- G = Gold Ingot  
- E = Ender Pearl

### Using the Device

1. **Regular Selection**: Shift + Right-click on any inventory block (chest, barrel, etc.) while holding the Pick There Device to toggle it in your regular selection (shows as **gold wireframes**)
2. **Same-Item-Only Selection**: Left-click on any inventory block to toggle it as "same-item-only" (shows as **red wireframes**) - these inventories will only accept items of types they already contain
3. **Visual Feedback**: Selected inventories are only visible when the Pick There Device is in your main hand
4. **Toggle Pickup Redirection**: Shift + Right-click on air to enable/disable automatic pickup redirection
5. **Reset Device**: Craft the Pick There Device by itself to clear all selections and reset settings
6. **Automatic Pickup**: When pickup redirection is enabled and you have inventories selected, any items you pick up will automatically be distributed to them

### Smart Distribution Logic

The mod uses an intelligent multi-phase distribution system with **same-item-only inventories having priority**:

1. **Stack with Existing - Same-Item-Only**: First tries to stack items with identical items already in same-item-only inventories (PRIORITY)
2. **Stack with Existing - Regular**: Then tries to stack items with identical items already in regular inventories
3. **Same Item Type - Same-Item-Only**: Tries to place items in same-item-only inventories that already contain the same item type (PRIORITY)
4. **Same Item Type - Regular**: Then tries to place items in regular inventories that already contain the same item type
5. **Any Available Space**: Finally places items in any available regular inventory space (same-item-only inventories are skipped in this phase)

## ⚙️ Configuration

The mod includes several configurable options that can be found in the config file after running the mod:

- **maxPickupDistance**: Set to `-1` for unlimited range, or a positive value to limit pickup distance
- **enablePickupSound**: Enable/disable pickup sound effects
- **showPickupParticles**: Enable/disable pickup particle effects
- **showSelectedBlockOutlines**: Enable/disable visual wireframe outlines

The config file will be generated at `run/config/pickthere.cfg` after first launch.

## 🔧 Technical Details

- **Minecraft Version**: 1.7.10
- **Forge Version**: 10.13.4.1558+
- **Mod ID**: `pickthere`
- **License**: MIT

## 🔨 Building from Source

1. Clone this repository:
   ```bash
   git clone https://github.com/xXseesXx/PickThere.git
   cd PickThere
   ```

2. Build the mod:
   ```bash
   ./gradlew build
   ```

3. The built jar will be in `build/libs/`

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Setup
1. Fork the repository
2. Clone your fork
3. Import the project into your IDE (IntelliJ IDEA recommended)
4. Run `./gradlew setupDecompWorkspace` and `./gradlew setupDevWorkspace`
5. Make your changes and test them
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Thanks to the Minecraft Forge team for the modding framework
- Thanks to the Minecraft modding community for inspiration and support
- Thanks Kiro and Claude

---

**Enjoy using PickThere! If you encounter any issues or have suggestions, please [open an issue](../../issues).**