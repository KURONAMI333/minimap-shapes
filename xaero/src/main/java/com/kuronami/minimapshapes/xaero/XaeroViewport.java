package com.kuronami.minimapshapes.xaero;

import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeGeometry;
import net.minecraft.client.Minecraft;

/** Per-frame footprint shared by the texture, rim, compass and off-screen markers. */
public final class XaeroViewport {
    private static double aspect = 16.0 / 9.0;
    private XaeroViewport() {}

    public static void update() {
        var window = Minecraft.getInstance().getWindow();
        if (window.getWidth() > 0 && window.getHeight() > 0)
            aspect = Math.max(1.0, (double) window.getWidth() / window.getHeight());
    }

    public static double shortAxis() { return 1.0 / aspect; }
    public static double radius(Shape shape, double angle) {
        return ShapeGeometry.radius(shape, angle, aspect);
    }
}
