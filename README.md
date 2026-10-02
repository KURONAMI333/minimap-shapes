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

Core is licensed under MIT so other map add-ons can use its shape API. The MIT scope is `common/src/main/java`, `core/`, `docs/CORE_API.md`, and `docs/examples/GeometryConsumer.java`; see `core/LICENSE`. The JourneyMap and Xaero add-ons in `journeymap/` and `xaero/` are All Rights Reserved under the root `LICENSE`.

## Build

Use Java 21 and run `./gradlew build --no-build-cache`. The six runtime JARs are in `core/{fabric,neoforge}/build/libs`, `journeymap/{fabric,neoforge}/build/libs`, and `xaero/{fabric,neoforge}/build/libs`; choose the JAR without `sources` or `javadoc` in its filename.

Shared client code lives in `common/src/main/java`, with map-specific render hooks in `journeymap/src/main/java` and `xaero/src/main/java`. The `core`, `journeymap`, and `xaero` directories each contain their Fabric and NeoForge entry points and metadata. The add-ons have mandatory dependencies on Core and their matching map mod, so a missing or unsupported host is reported by the loader instead of silently doing nothing.

## Development client preparation

After building, `python3 tools/prepare-dev-runtime.py prepare all` stages exact Core/add-on, host, and JEI JARs for six isolated scenarios: Fabric or NeoForge, each with JourneyMap only, Xaero only, or both. `python3 tools/prepare-dev-runtime.py verify all` checks their hashes and Gradle run directories without launching Minecraft. Each scenario uses its corresponding module's `runClient` task, whose own mod classes come from the development classpath. Fabric API 0.109.0+1.21.1 is already supplied by Loom's `modImplementation` runtime classpath, so the preparation tool does not duplicate it in `mods/`.

When a client run is permitted, re-prepare with `--owner <current-session-tag>` if the owner changed, then use the workspace's guarded `_tools/runclient_fresh.sh` with the task printed by the tool and `RUNCLIENT_NO_JEI=1`; the correct loader-specific JEI is already staged. Do not invoke `./gradlew runClient` directly. Preparation and static verification alone do not establish that the host mixins render correctly in-game.
