# Minimap Shapes Core API

Core supplies shape geometry, a selection screen, and one saved preference per map add-on. The JourneyMap and Xaero add-ons connect these functions to their map renderers. A new map integration supplies its own rendering and marker hooks.

The Java package is `com.kuronami.minimapshapes`. The mod ID is `minimapshapes_core`. Use the Core JAR for the same Minecraft version and loader as the add-on, and declare it as a required dependency. The current add-ons require the matching Core version.

## Geometry

`Shape` contains `STANDARD`, `ROUNDED_SQUARE`, `HEXAGON`, `OCTAGON`, `HORIZONTAL_RECTANGLE`, and `VERTICAL_RECTANGLE`. `STANDARD` means the host's native shape: bypass custom clipping and use the host's own mask, frame, and marker boundary.

`ShapeGeometry.radius(shape, angle, aspect)` returns the distance from the origin to the contour at `angle` in radians. Coordinates use a centered unit square, with each axis ranging from −1 to 1. The hexagon points upward; the octagon has horizontal and vertical edges. The rounded square uses a fourth-power superellipse.

Pass a finite screen aspect ratio of at least 1 for rectangles. The long axis remains 2 units wide and the short axis is cropped to `2 / aspect`. The two-argument overload uses 16:9. Scale the resulting viewport uniformly with the underlying map; changing texture scale separately on each axis stretches the terrain.

`distanceToEdge(shape, x, y)` is a radial difference between the contour and the point, using the default aspect. It is positive inside. It is not the shortest Euclidean distance to an edge. For a rectangle with a different aspect, calculate the radius with the three-argument overload.

[GeometryConsumer.java](examples/GeometryConsumer.java) is a runnable usage example. Compile or run it with the named development Core artifact and the Minecraft development classpath. It reads geometry and does not start Minecraft. Fabric distribution artifacts use intermediary Minecraft names; use Loom's development dependency handling when compiling a Fabric consumer.

## Registration and selection

Call `ClientShapes.register(id, choices, apply)` once per map integration. Use a stable lowercase ID matching `[a-z0-9_-]{1,32}`. Include `STANDARD` and only shapes your renderer supports. Registration clones the choices and can run during parallel loader initialization; preference loading is deferred until the client uses it. Registering the same ID twice throws `IllegalStateException`.

The `apply` callback receives the selected `Shape`. Return `true` after the host has accepted it. Return `false` while the host is not ready, allowing a later client tick to retry. The callback runs on selection changes and initial application in a loaded world. A runtime exception or linkage error is logged and disables that host's apply callback for the rest of the session. `available(id)` then returns false.

The add-on registers a `KeyMapping` through its loader and calls `ClientShapes.tick(id, openKey)` on each client tick. Core opens `ShapeScreen` when the key is pressed and no other screen is open, and tracks application of the selected shape. Give each integration a distinct key binding and translation category.

`ShapeSettings.get(id)` loads the preference lazily; the default is `STANDARD`. `set(id, shape)` updates the preference, saves it, and requests immediate application. Call these methods after the Minecraft client exists, from the client thread. Files are stored as `config/minimapshapes-<id>.properties`. The choice array and saved value should agree; Core does not filter a manually edited valid enum value against the registered choices.

Provide `minimapshapes.screen.title.<id>` in the add-on's language files. Shared shape labels and the Done button are supplied by Core and Minecraft. The JourneyMap and Xaero entry points are concrete registration/tick examples in this source tree.

## Renderer responsibilities

Use the same contour for the terrain mask, frame, marker inside/outside decisions, and offscreen projection. Transform marker coordinates into the contour's screen orientation before testing them, and transform projected results back when the host's drawing pose rotates. Preserve the host's zoom, atlas UVs, winding, fractional translation, direction labels, and viewport placement.

`ShapeTexture.make(shape, width, height, rim)` creates a `DynamicTexture` for a mask or plain rim using the default geometry. Its caller owns the returned texture and must close it when the host releases the frame. Create it on the appropriate rendering thread. This helper does not install a texture, manage the host's cache, or choose a rectangle aspect.

Core does not discover map mods or install render hooks automatically. The map-specific add-on declares its host dependency and maintains its hooks as that host changes. No server synchronization or waypoint storage is supplied by Core.

## License

Core and this API documentation are covered by [the MIT license](../core/LICENSE). The JourneyMap and Xaero add-ons retain the separate All Rights Reserved license in the repository root.
