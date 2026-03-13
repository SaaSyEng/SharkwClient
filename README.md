# SharkwClient

**Open-source Minecraft 1.8.9 Forge mod** for PvP enhancement and server security auditing.

SharkwClient is a feature-rich client mod built on Forge + SpongeMixin, designed for testing anticheat systems on private servers. It includes 38 configurable modules across combat, movement, visual, and utility categories.

---

## Features

### Combat (10 modules)
| Module | Description |
|--------|-------------|
| KillAura | Silent aura with smooth rotations and GCD fix |
| AimAssist | Frame-independent smooth aim correction |
| AutoClicker | Gaussian-distributed CPS autoclicker |
| Criticals | Packet criticals on every hit |
| Velocity | Cancel or reduce knockback (packet-level) |
| Reach | Extended attack range with proper raycasting |
| WTap | Automatic sprint reset for max knockback |
| Strafe | Combat strafing automation |
| AutoArmor | Automatic best armor equipping |
| Backtrack | Delayed entity positions for extended hit window |

### Movement (11 modules)
| Module | Description |
|--------|-------------|
| Speed | Sprint / SlowHop / GroundStrafe modes |
| Fly | Vanilla and Glide modes with anti-kick |
| Sprint | Auto-sprint with omni-directional option |
| Scaffold | Auto bridge with silent rotations |
| NoFall | Fall damage prevention |
| Bhop | Bunny hop with speed boost |
| Jesus | Walk on water |
| NoSlow | Cancel item use slowdown |
| Step | Step up blocks without jumping |
| Phase | Noclip through blocks |
| AirJump | Multiple jumps in the air |

### Visual (7 modules)
| Module | Description |
|--------|-------------|
| ESP | Player/mob boxes through walls |
| Tracers | Lines to entities |
| Fullbright | Maximum visibility |
| XRay | See ores through blocks |
| TargetHUD | Target health display |
| KeyStrokes | Input display with CPS |
| HUD | Module list, watermark, info |

### Misc (10 modules)
| Module | Description |
|--------|-------------|
| Timer | Game speed modification |
| Blink | Packet buffering (real packet interception) |
| AntiAFK | Anti-idle with configurable modes |
| AutoEat | Automatic eating when hungry |
| AutoRespawn | Instant respawn on death |
| FreeCam | Free camera movement |
| FlagDetector | Anticheat detection monitor |
| FastBreak | Faster block breaking |
| FastPlace | Faster block placement |

### Technical Features
- **GCD Fix** — Rotations calibrated to mouse hardware sensitivity
- **Gaussian CPS** — Statistically human click patterns
- **Silent Rotations** — Server-side rotation via packets
- **Mixin Injection** — Direct hooks into NetworkManager, Block rendering, and more
- **Full Settings System** — Every module has configurable sliders, toggles, and modes
- **Premium Dark GUI** — Full-screen ClickGUI with animated toggles, sliders, and keybind support

---

## Download & Install

### Requirements
- Minecraft 1.8.9
- [Forge 1.8.9](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html) installed
- Java 8

### Option 1: Download Pre-built JAR
1. Go to the [**Releases**](../../releases) page
2. Download the latest `SharkwClient-1.0.jar`
3. Place it in your `.minecraft/mods/` folder
4. Launch Minecraft with Forge 1.8.9
5. Press **RIGHT SHIFT** to open the GUI

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/SaaSyEng/SharkwClient.git
cd SharkwClient

# Setup workspace (first time only, ~15 min)
# Windows:
gradlew.bat setupDecompWorkspace

# Build
gradlew.bat build
```

The compiled JAR will be at `build/libs/SharkwClient-1.0.jar`. Copy it to your `.minecraft/mods/` folder.

### Quick Start
1. Install Forge 1.8.9 in your Minecraft launcher
2. Drop the JAR into `.minecraft/mods/`
3. Launch Minecraft with Forge 1.8.9
4. Press **RIGHT SHIFT** in-game to open the ClickGUI
5. Click modules to toggle, expand for settings
6. Right-click a module to assign a keybind

---

## Controls

| Key | Action |
|-----|--------|
| RIGHT SHIFT | Open/Close ClickGUI |
| Right-click module | Set keybind |
| Mouse wheel | Scroll module list |
| ESC | Close GUI |

---

## Building Requirements

- **JDK 8** (Adoptium/Temurin recommended)
- **Windows** (ForgeGradle 2.1 + Gradle 2.14)

---

## License

This project is open source. Use it, modify it, learn from it.

---

## Disclaimer

This mod is intended for use on private servers where you have explicit permission to test. Use responsibly.
