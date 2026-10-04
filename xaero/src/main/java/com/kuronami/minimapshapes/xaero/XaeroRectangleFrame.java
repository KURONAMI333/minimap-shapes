package com.kuronami.minimapshapes.xaero;

import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeGeometry;

/** 矩形の枠だけを各辺の法線方向へ広げる。地図の切抜きとUVは変更しない。 */
final class XaeroRectangleFrame {
    private XaeroRectangleFrame() {}

    record Point(double x, double y) {}

    /**
     * 内縁と同じ角を保ち、外縁を幅・高さそれぞれborderだけ広げる。
     * 半径へborderを加えると辺中央だけ膨らむため、矩形では軸ごとに広げる。
     * 角は隣接する2辺のオフセットの交点になり、サンプル間にも弓形や欠けを作らない。
     */
    static Point vertex(Shape shape, double half, double border, double theta, double shortAxis) {
        double halfWidth = half * (shape == Shape.HORIZONTAL_RECTANGLE ? 1.0 : shortAxis);
        double halfHeight = half * (shape == Shape.VERTICAL_RECTANGLE ? 1.0 : shortAxis);
        double radius = half * ShapeGeometry.radius(shape, theta - Math.PI / 2, 1.0 / shortAxis);
        double innerX = radius * Math.sin(theta);
        double innerY = -radius * Math.cos(theta);
        return new Point(innerX * (halfWidth + border) / halfWidth,
                innerY * (halfHeight + border) / halfHeight);
    }
}
