# SharkwClient

**Open-source Minecraft 1.8.9 Forge mod** for PvP enhancement and server security auditing.

**Mod open-source de Minecraft 1.8.9 Forge** para mejora de PvP y auditoría de seguridad en servidores.

SharkwClient is a feature-rich client mod built on Forge + SpongeMixin, with 38 configurable modules across combat, movement, visual, and utility categories.

SharkwClient es un cliente con muchas funciones construido sobre Forge + SpongeMixin, con 38 módulos configurables en las categorías de combate, movimiento, visual y utilidades.

---

## Features / Características

### Combat / Combate (10)
| Module | Description / Descripción |
|--------|--------------------------|
| KillAura | Silent aura with smooth rotations and GCD fix / Aura silenciosa con rotaciones suaves y GCD fix |
| AimAssist | Frame-independent smooth aim correction / Corrección de apuntado suave e independiente del framerate |
| AutoClicker | Gaussian-distributed CPS autoclicker / Autoclicker con distribución gaussiana de CPS |
| Criticals | Packet criticals on every hit / Críticos por packet en cada golpe |
| Velocity | Cancel or reduce knockback (packet-level) / Cancela o reduce el knockback (a nivel de packet) |
| Reach | Extended attack range with raycasting / Alcance de ataque extendido con raycasting |
| WTap | Automatic sprint reset for max knockback / Reset de sprint automático para máximo knockback |
| Strafe | Combat strafing automation / Automatización de strafe en combate |
| AutoArmor | Automatic best armor equipping / Equipado automático de la mejor armadura |
| Backtrack | Delayed entity positions for extended hit window / Posiciones retrasadas para más ventana de hit |

### Movement / Movimiento (11)
| Module | Description / Descripción |
|--------|--------------------------|
| Speed | Sprint / SlowHop / GroundStrafe modes / Modos Sprint / SlowHop / GroundStrafe |
| Fly | Vanilla and Glide modes with anti-kick / Modos Vanilla y Glide con anti-kick |
| Sprint | Auto-sprint with omni-directional option / Auto-sprint con opción omnidireccional |
| Scaffold | Auto bridge with silent rotations / Auto bridge con rotaciones silenciosas |
| NoFall | Fall damage prevention / Prevención de daño por caída |
| Bhop | Bunny hop with speed boost / Bunny hop con boost de velocidad |
| Jesus | Walk on water / Caminar sobre el agua |
| NoSlow | Cancel item use slowdown / Cancela la ralentización por uso de items |
| Step | Step up blocks without jumping / Subir bloques sin saltar |
| Phase | Noclip through blocks / Noclip a través de bloques |
| AirJump | Multiple jumps in the air / Saltos múltiples en el aire |

### Visual (7)
| Module | Description / Descripción |
|--------|--------------------------|
| ESP | Player/mob boxes through walls / Cajas de jugadores/mobs a través de paredes |
| Tracers | Lines to entities / Líneas hacia entidades |
| Fullbright | Maximum visibility / Visibilidad máxima |
| XRay | See ores through blocks / Ver minerales a través de bloques |
| TargetHUD | Target health display / Muestra la vida del objetivo |
| KeyStrokes | Input display with CPS / Visualización de inputs con CPS |
| HUD | Module list, watermark, info / Lista de módulos, watermark, info |

### Misc (10)
| Module | Description / Descripción |
|--------|--------------------------|
| Timer | Game speed modification / Modificación de velocidad del juego |
| Blink | Packet buffering (real interception) / Buffering de packets (intercepción real) |
| AntiAFK | Anti-idle with configurable modes / Anti-inactividad con modos configurables |
| AutoEat | Automatic eating when hungry / Comer automáticamente cuando hay hambre |
| AutoRespawn | Instant respawn on death / Respawn instantáneo al morir |
| FreeCam | Free camera movement / Movimiento libre de cámara |
| FlagDetector | Anticheat detection monitor / Monitor de detecciones del anticheat |
| FastBreak | Faster block breaking / Romper bloques más rápido |
| FastPlace | Faster block placement / Colocar bloques más rápido |

### Technical Features / Características Técnicas
- **GCD Fix** — Rotations calibrated to mouse sensitivity / Rotaciones calibradas a la sensibilidad del mouse
- **Gaussian CPS** — Statistically human click patterns / Patrones de clicks estadísticamente humanos
- **Silent Rotations** — Server-side rotation via packets / Rotación server-side vía packets
- **Mixin Injection** — Direct hooks into NetworkManager, Block rendering / Hooks directos en NetworkManager, renderizado de bloques
- **Full Settings System** — Sliders, toggles, and modes for every module / Sliders, toggles y modos para cada módulo
- **Premium Dark GUI** — Full-screen ClickGUI with animations and keybind support / ClickGUI fullscreen con animaciones y keybinds

---

## Download & Install / Descargar e Instalar

### Requirements / Requisitos
- Minecraft 1.8.9
- [Forge 1.8.9](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html)
- Java 8

### Option 1: Download JAR / Descargar JAR
1. Go to / Ve a [**Releases**](../../releases)
2. Download / Descarga `SharkwClient-1.0.jar`
3. Place in / Ponlo en `.minecraft/mods/`
4. Launch with Forge 1.8.9 / Abre Minecraft con Forge 1.8.9
5. Press / Presiona **RIGHT SHIFT** to open GUI / para abrir el GUI

### Option 2: Build from Source / Compilar desde el código
```bash
git clone https://github.com/SaaSyEng/SharkwClient.git
cd SharkwClient

# First time setup / Primera vez (~15 min)
gradlew.bat setupDecompWorkspace

# Build / Compilar
gradlew.bat build
```

JAR output: `build/libs/SharkwClient-1.0.jar` → copy to / copiar a `.minecraft/mods/`

---

## Controls / Controles

| Key / Tecla | Action / Acción |
|-------------|-----------------|
| RIGHT SHIFT | Open/Close ClickGUI / Abrir/Cerrar ClickGUI |
| Right-click module / Click derecho | Set keybind / Asignar keybind |
| Mouse wheel / Rueda del mouse | Scroll module list / Scroll en la lista |
| ESC | Close GUI / Cerrar GUI |

---

## Build Requirements / Requisitos para Compilar

- **JDK 8** (Adoptium/Temurin)
- **Windows** (ForgeGradle 2.1 + Gradle 2.14)

---

## License / Licencia

Open source. Use it, modify it, learn from it. / Úsalo, modifícalo, aprende de él.

---

## Disclaimer / Aviso

This mod is intended for private servers with explicit permission. Use responsibly.

Este mod está pensado para servidores privados con permiso explícito. Úsalo con responsabilidad.
