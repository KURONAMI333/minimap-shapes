// SPDX-License-Identifier: MIT
import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeGeometry;

/** A small consumer of the shared geometry, with no game launch. */
public final class GeometryConsumer {
    public static boolean inside(Shape shape, double x, double y, double screenAspect) {
        if (shape == Shape.STANDARD) {
            throw new IllegalArgumentException("Use the map's native boundary for STANDARD");
        }
        return Math.hypot(x, y) <= ShapeGeometry.radius(shape, Math.atan2(y, x), screenAspect);
    }

    public static void main(String[] args) {
        if (!inside(Shape.HEXAGON, 0, 0.9, 16.0 / 9.0)) throw new AssertionError();
        if (inside(Shape.HEXAGON, 0.95, 0, 16.0 / 9.0)) throw new AssertionError();
        if (!inside(Shape.HORIZONTAL_RECTANGLE, 0.9, 0.5, 16.0 / 9.0)) throw new AssertionError();
        if (inside(Shape.HORIZONTAL_RECTANGLE, 0, 0.7, 16.0 / 9.0)) throw new AssertionError();
        System.out.println("Geometry consumer passed");
    }
}
