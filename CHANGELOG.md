# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-12-XX

### Added
- Initial release of PickThere mod
- Pick There Device item with crafting recipe
- Visual wireframe highlighting for selected inventories
- Dual selection system (regular and same-item-only inventories)
- Smart multi-phase item distribution system
- Pickup redirection toggle functionality
- Dynamic item texture based on active/inactive state
- Unlimited inventory selection (no limit)
- Distance-based pickup filtering (unlimited by default, configurable)
- Easy device reset via crafting recipe
- Comprehensive tooltip information
- Main hand only rendering for visual feedback

### Features
- **Regular Selection**: Gold wireframes for inventories that accept any items
- **Same-Item-Only Selection**: Red wireframes for inventories that only accept matching item types
- **Priority System**: Same-item-only inventories get priority in distribution
- **Visual Feedback**: Item texture changes based on pickup enabled/disabled state
- **Smart Distribution**: 5-phase intelligent item distribution system
- **Configuration**: Configurable pickup distance and visual settings
- **Reset Functionality**: Craft device by itself to reset all selections

### Technical Details
- Minecraft 1.20.1 compatibility
- Forge 47.4.5+ support
- Client-side rendering optimizations
- NBT-based data storage
- Event-driven pickup handling