# SharkwClient

**Mod open-source de Minecraft 1.8.9 Forge** para mejora de PvP y auditoría de seguridad en servidores.

SharkwClient es un cliente con muchas funciones construido sobre Forge + SpongeMixin, diseñado para testear sistemas anticheat en servidores privados. Incluye 38 módulos configurables en las categorías de combate, movimiento, visual y utilidades.

---

## Características

### Combate (10 módulos)
| Módulo | Descripción |
|--------|-------------|
| KillAura | Aura silenciosa con rotaciones suaves y GCD fix |
| AimAssist | Corrección de apuntado suave e independiente del framerate |
| AutoClicker | Autoclicker con distribución gaussiana de CPS |
| Criticals | Críticos por packet en cada golpe |
| Velocity | Cancela o reduce el knockback (a nivel de packet) |
| Reach | Alcance de ataque extendido con raycasting |
| WTap | Reset de sprint automático para máximo knockback |
| Strafe | Automatización de strafe en combate |
| AutoArmor | Equipado automático de la mejor armadura |
| Backtrack | Posiciones retrasadas de entidades para más ventana de hit |

### Movimiento (11 módulos)
| Módulo | Descripción |
|--------|-------------|
| Speed | Modos Sprint / SlowHop / GroundStrafe |
| Fly | Modos Vanilla y Glide con anti-kick |
| Sprint | Auto-sprint con opción omnidireccional |
| Scaffold | Auto bridge con rotaciones silenciosas |
| NoFall | Prevención de daño por caída |
| Bhop | Bunny hop con boost de velocidad |
| Jesus | Caminar sobre el agua |
| NoSlow | Cancela la ralentización por uso de items |
| Step | Subir bloques sin saltar |
| Phase | Noclip a través de bloques |
| AirJump | Saltos múltiples en el aire |

### Visual (7 módulos)
| Módulo | Descripción |
|--------|-------------|
| ESP | Cajas de jugadores/mobs a través de paredes |
| Tracers | Líneas hacia entidades |
| Fullbright | Visibilidad máxima |
| XRay | Ver minerales a través de bloques |
| TargetHUD | Muestra la vida del objetivo |
| KeyStrokes | Visualización de inputs con CPS |
| HUD | Lista de módulos, watermark, info |

### Misc (10 módulos)
| Módulo | Descripción |
|--------|-------------|
| Timer | Modificación de velocidad del juego |
| Blink | Buffering de packets (intercepción real) |
| AntiAFK | Anti-inactividad con modos configurables |
| AutoEat | Comer automáticamente cuando hay hambre |
| AutoRespawn | Respawn instantáneo al morir |
| FreeCam | Movimiento libre de cámara |
| FlagDetector | Monitor de detecciones del anticheat |
| FastBreak | Romper bloques más rápido |
| FastPlace | Colocar bloques más rápido |

### Características Técnicas
- **GCD Fix** — Rotaciones calibradas a la sensibilidad del hardware del mouse
- **CPS Gaussiano** — Patrones de clicks estadísticamente humanos
- **Rotaciones Silenciosas** — Rotación server-side vía packets
- **Inyección Mixin** — Hooks directos en NetworkManager, renderizado de bloques y más
- **Sistema de Settings Completo** — Cada módulo tiene sliders, toggles y modos configurables
- **GUI Dark Premium** — ClickGUI fullscreen con toggles animados, sliders y soporte de keybinds

---

## Descargar e Instalar

### Requisitos
- Minecraft 1.8.9
- [Forge 1.8.9](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html) instalado
- Java 8

### Opción 1: Descargar el JAR compilado
1. Ve a la página de [**Releases**](../../releases)
2. Descarga el último `SharkwClient-1.0.jar`
3. Ponlo en tu carpeta `.minecraft/mods/`
4. Abre Minecraft con Forge 1.8.9
5. Presiona **RIGHT SHIFT** para abrir el GUI

### Opción 2: Compilar desde el código fuente
```bash
# Clonar el repositorio
git clone https://github.com/SaaSyEng/SharkwClient.git
cd SharkwClient

# Configurar workspace (solo la primera vez, ~15 min)
# Windows:
gradlew.bat setupDecompWorkspace

# Compilar
gradlew.bat build
```

El JAR compilado estará en `build/libs/SharkwClient-1.0.jar`. Cópialo a tu carpeta `.minecraft/mods/`.

### Inicio Rápido
1. Instala Forge 1.8.9 en tu launcher de Minecraft
2. Mete el JAR en `.minecraft/mods/`
3. Abre Minecraft con Forge 1.8.9
4. Presiona **RIGHT SHIFT** en el juego para abrir el ClickGUI
5. Haz click en los módulos para activar/desactivar, expande para ver settings
6. Click derecho en un módulo para asignar un keybind

---

## Controles

| Tecla | Acción |
|-------|--------|
| RIGHT SHIFT | Abrir/Cerrar ClickGUI |
| Click derecho en módulo | Asignar keybind |
| Rueda del mouse | Scroll en la lista de módulos |
| ESC | Cerrar GUI |

---

## Requisitos para Compilar

- **JDK 8** (se recomienda Adoptium/Temurin)
- **Windows** (ForgeGradle 2.1 + Gradle 2.14)

---

## Licencia

Este proyecto es open source. Úsalo, modifícalo, aprende de él.

---

## Aviso

Este mod está pensado para uso en servidores privados donde tengas permiso explícito para hacer pruebas. Úsalo con responsabilidad.
