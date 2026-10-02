# Minimap Shapes

This source tree builds three client-side mods for Minecraft 1.21.1:

| Mod | What it provides | Required mods |
| --- | --- | --- |
| Minimap Shapes Core | Shape geometry, previews, and separate saved preferences for each map | Fabric API on Fabric |
| Minimap Shapes for JourneyMap | Rounded square, hexagon, and octagon | Core and JourneyMap 6.0.9 for MC 1.21.1 |
| Minimap Shapes for Xaero's Minimap | Rounded square, hexagon, octagon, horizontal rectangle, and vertical rectangle | Core and Xaero's Minimap 26.5.0 |

Each mod has its own Fabric and NeoForge JAR. Install the JARs for **one loader only**. For example, a Fabric JourneyMap setup needs the Fabric Core JAR, the Fabric JourneyMap add-on JAR, JourneyMap, and Fabric API. The two map add-ons can coexist; each stores its selection separately. Core alone does not change a minimap.

In-game, press **O** for the JourneyMap shape screen or **K** for the Xaero shape screen. Choose a shape to apply it immediately. **Default** returns that map to its own shape setting. The selected outline stays fixed on screen while map content follows the host mod's heading and zoom settings. The rectangle options crop the short axis of Xaero's square map viewport; they do not stretch the terrain.

Selections are saved in `config/minimapshapes-journeymap.properties` and `config/minimapshapes-xaero.properties`. No manual configuration is needed.

## Build

Use Java 21 and run `./gradlew build --no-build-cache`. The six runtime JARs are in `core/{fabric,neoforge}/build/libs`, `journeymap/{fabric,neoforge}/build/libs`, and `xaero/{fabric,neoforge}/build/libs`; choose the JAR without `sources` or `javadoc` in its filename.

Shared client code lives in `common/src/main/java`, with map-specific render hooks in `journeymap/src/main/java` and `xaero/src/main/java`. The `core`, `journeymap`, and `xaero` directories each contain their Fabric and NeoForge entry points and metadata. The add-ons have mandatory dependencies on Core and their matching map mod, so a missing or unsupported host is reported by the loader instead of silently doing nothing.
