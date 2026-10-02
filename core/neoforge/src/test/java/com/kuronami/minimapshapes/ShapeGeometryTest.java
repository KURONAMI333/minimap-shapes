package com.kuronami.minimapshapes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapeGeometryTest {
    private static final double EPSILON = 1e-9;

    @Test
    void rectangleCropsOnlyTheShortAxis() {
        double aspect = 16.0 / 9.0;
        assertEquals(1.0, ShapeGeometry.radius(Shape.HORIZONTAL_RECTANGLE, 0, aspect), EPSILON);
        assertEquals(9.0 / 16.0, ShapeGeometry.radius(Shape.HORIZONTAL_RECTANGLE, Math.PI / 2, aspect), EPSILON);
        assertEquals(9.0 / 16.0, ShapeGeometry.radius(Shape.VERTICAL_RECTANGLE, 0, aspect), EPSILON);
        assertEquals(1.0, ShapeGeometry.radius(Shape.VERTICAL_RECTANGLE, Math.PI / 2, aspect), EPSILON);
    }

    @Test
    void polygonContoursRemainInsideTheSquareViewport() {
        for (Shape shape : new Shape[]{Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON}) {
            for (int step = 0; step < 720; step++) {
                double angle = step * Math.PI / 360.0;
                double radius = ShapeGeometry.radius(shape, angle);
                assertTrue(radius > 0 && Double.isFinite(radius), shape + " invalid radius at " + angle);
                assertTrue(Math.abs(radius * Math.cos(angle)) <= 1.0000001, shape + " escaped X viewport");
                assertTrue(Math.abs(radius * Math.sin(angle)) <= 1.0000001, shape + " escaped Y viewport");
                assertEquals(0.0, ShapeGeometry.distanceToEdge(shape,
                        radius * Math.cos(angle), radius * Math.sin(angle)), 1e-8);
            }
        }
    }
}
