Build minimap shape add-ons with shared contour geometry, a selection screen, and a separate saved preference for each map.

Minimap Shapes Core is the library used by the JourneyMap and Xaero shape add-ons. Install it when an add-on requires it. Each add-on connects Core to its map's renderer and registers the shapes that renderer supports.

## Geometry

The shared shapes are a rounded square, hexagon, octagon, horizontal rectangle, and vertical rectangle. A Default selection delegates rendering to the map's native shape. Geometry is expressed in a centered unit square, so an integration can apply the same contour to a terrain mask, frame, and marker boundary.

A minimal radius query uses the public Java API:

```java
import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeGeometry;

double angle = Math.PI / 4;
double radius = ShapeGeometry.radius(Shape.HEXAGON, angle);
```

The rectangle overload accepts an aspect ratio and crops the short axis while keeping the map texture's scale. Integrations provide their own coordinate transformations and offscreen projection.

## Selection and saved preferences

Register a stable map ID, a list of supported shapes, and an apply callback through `ClientShapes.register`. The add-on registers its key binding and calls `ClientShapes.tick` from its client tick handler. Core opens the selector, saves the chosen enum value, and requests application when it changes.

Preferences are separate for each registered map. The JourneyMap and Xaero integrations can therefore share one Core installation while retaining independent selections. A new integration supplies its screen-title translation and connects the contour to its host's rendering, rotation, and marker rules.

## API scope

Core provides these shared building blocks. Map-specific rendering hooks, host dependencies, texture ownership, and version compatibility remain the integration's responsibility. The source includes API documentation and a small geometry consumer example.

<p><a href="https://www.patreon.com/KURONAMI333"><img src="https://raw.githubusercontent.com/KURONAMI333/music-disc-maker/6a0a895769575a1a58fd1fb6dfb15e259bcefccb/_docs/support/patreon.png" width="440" height="156" alt="Support my mods on Patreon"></a> <a href="https://x.com/kuronami333"><img src="https://raw.githubusercontent.com/KURONAMI333/music-disc-maker/6a0a895769575a1a58fd1fb6dfb15e259bcefccb/_docs/support/x.png" width="300" height="156" alt="Follow @kuronami333 on X"></a></p>

MIT licensed. Free to use, modify, and distribute under the terms of the MIT license.

Bugs and questions: comment on the CurseForge page, or DM [@kuronami333 on X](https://x.com/kuronami333).
