package com.kuronami.minimapshapes;

/** Map footprint in a unit square. The radius is measured from its center. */
public final class ShapeGeometry {
    private ShapeGeometry() {}

    public static double radius(Shape shape, double angle) {
        return radius(shape, angle, 16.0 / 9.0);
    }

    /** Rectangle aspect applies to the viewport, never to the map texture's scale. */
    public static double radius(Shape shape, double angle, double aspect) {
        if (shape == Shape.HORIZONTAL_RECTANGLE || shape == Shape.VERTICAL_RECTANGLE) {
            // Retain the square texture scale: the short axis is a viewport crop.
            double shortAxis = 1.0 / Math.max(1.0, aspect);
            double halfWidth = shape == Shape.HORIZONTAL_RECTANGLE ? 1.0 : shortAxis;
            double halfHeight = shape == Shape.VERTICAL_RECTANGLE ? 1.0 : shortAxis;
            return Math.min(halfWidth / Math.abs(Math.cos(angle)),
                    halfHeight / Math.abs(Math.sin(angle)));
        }
        if (shape == Shape.ROUNDED_SQUARE) {
            double cosine = Math.abs(Math.cos(angle));
            double sine = Math.abs(Math.sin(angle));
            return 1.0 / Math.pow(Math.pow(cosine, 4) + Math.pow(sine, 4), 0.25);
        }
        int sides = shape == Shape.HEXAGON ? 6 : shape == Shape.OCTAGON ? 8 : 0;
        if (sides == 0) return 1.0;

        // Top-facing hexagon; octagon has horizontal and vertical edges.
        double first = sides == 6 ? -Math.PI / 2 : -Math.PI / 8;
        double sector = Math.PI * 2 / sides;
        double delta = Math.IEEEremainder(angle - first, sector);
        double vertexRadius = sides == 6 ? 1.0 : 1.0 / Math.cos(Math.PI / 8);
        return vertexRadius * Math.cos(Math.PI / sides)
                / Math.cos(Math.PI / sides - Math.abs(delta));
    }

    public static double distanceToEdge(Shape shape, double x, double y) {
        double distance = Math.hypot(x, y);
        return radius(shape, Math.atan2(y, x)) - distance;
    }
}
