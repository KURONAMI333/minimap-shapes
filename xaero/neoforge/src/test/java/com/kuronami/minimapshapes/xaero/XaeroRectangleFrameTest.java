package com.kuronami.minimapshapes.xaero;

import com.kuronami.minimapshapes.Shape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 数学的な矩形枠の検査。Minecraftの描画合格を意味しない。 */
public class XaeroRectangleFrameTest {
    private static final double EPSILON = 1e-8;
    private static int checkedVertices;

    @Test
    void everyEdgeHasConstantOuterCoordinateAndNormalThickness() {
        for (Shape shape : new Shape[]{Shape.HORIZONTAL_RECTANGLE, Shape.VERTICAL_RECTANGLE}) {
            for (double aspect : new double[]{1, 4.0 / 3, 16.0 / 9, 21.0 / 9, 32.0 / 9}) {
                for (double half : new double[]{4, 31.5, 64, 128}) {
                    for (double border : new double[]{0, 0.5, 1, 3, 5, 11}) {
                        double width = shape == Shape.HORIZONTAL_RECTANGLE ? half : half / aspect;
                        double height = shape == Shape.VERTICAL_RECTANGLE ? half : half / aspect;
                        for (int sign : new int[]{-1, 1}) {
                            for (double fraction : new double[]{-1, -0.91, -0.6, -0.17, 0, 0.23, 0.58, 0.94, 1}) {
                                // 入力は辺上の点から決める。実装の半径式で期待値を作らない。
                                checkPoint(shape, aspect, half, border, sign * width, fraction * height,
                                        true, width, height);
                                checkPoint(shape, aspect, half, border, fraction * width, sign * height,
                                        false, width, height);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void checkPoint(Shape shape, double aspect, double half, double border,
                                   double innerX, double innerY, boolean verticalEdge,
                                   double width, double height) {
        double theta = Math.atan2(innerX, -innerY);
        var inner = XaeroRectangleFrame.vertex(shape, half, 0, theta, 1 / aspect);
        var outer = XaeroRectangleFrame.vertex(shape, half, border, theta, 1 / aspect);
        assertEquals(innerX, inner.x(), EPSILON, "inner X footprint must not stretch");
        assertEquals(innerY, inner.y(), EPSILON, "inner Y footprint must not stretch");
        if (verticalEdge) {
            assertEquals(width + border, Math.abs(outer.x()), EPSILON, "vertical outer edge must be straight");
            assertEquals(border, Math.abs(outer.x()) - Math.abs(inner.x()), EPSILON, "X normal thickness");
        } else {
            assertEquals(height + border, Math.abs(outer.y()), EPSILON, "horizontal outer edge must be straight");
            assertEquals(border, Math.abs(outer.y()) - Math.abs(inner.y()), EPSILON, "Y normal thickness");
        }
        assertTrue(Math.abs(outer.x()) <= width + border + EPSILON, "X boundary leak");
        assertTrue(Math.abs(outer.y()) <= height + border + EPSILON, "Y boundary leak");
        // 正規化した辺上の位置も保ち、同一辺の隣接サンプルを結んだ線分が欠けない。
        assertEquals(innerX / width, outer.x() / (width + border), EPSILON);
        assertEquals(innerY / height, outer.y() / (height + border), EPSILON);
        checkedVertices++;
    }

    @Test
    void offCenterRegressionWouldFailForThePreviousRadialBorder() {
        double half = 64, height = 36, border = 3;
        double x = 48, y = -height;
        double theta = Math.atan2(x, -y);
        double oldOuterY = -(Math.hypot(x, y) + border) * Math.cos(theta);
        assertTrue(Math.abs(Math.abs(oldOuterY) - (height + border)) > 1,
                "off-center witness must expose the previous bowed edge");
        var fixed = XaeroRectangleFrame.vertex(Shape.HORIZONTAL_RECTANGLE, half, border, theta, 9.0 / 16);
        assertEquals(-39, fixed.y(), EPSILON);
    }

    public static void main(String[] args) {
        var test = new XaeroRectangleFrameTest();
        test.everyEdgeHasConstantOuterCoordinateAndNormalThickness();
        test.offCenterRegressionWouldFailForThePreviousRadialBorder();
        System.out.println("GEOMETRY_PASS checkedVertices=" + checkedVertices + " renderingVerified=false");
    }
}
