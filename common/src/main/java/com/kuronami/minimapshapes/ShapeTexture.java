package com.kuronami.minimapshapes;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

/** Replacement alpha mask and plain rim for JourneyMap's circle theme slot. */
public final class ShapeTexture {
    private ShapeTexture() {}

    public static DynamicTexture make(Shape shape, int width, int height, boolean rim) {
        NativeImage image = new NativeImage(width, height, true);
        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;
        int samples = 4;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int covered = 0;
                for (int sy = 0; sy < samples; sy++) {
                    for (int sx = 0; sx < samples; sx++) {
                        double px = (x + (sx + 0.5) / samples - halfWidth) / halfWidth;
                        double py = (y + (sy + 0.5) / samples - halfHeight) / halfHeight;
                        double distance = ShapeGeometry.distanceToEdge(shape, px, py);
                        if (rim ? distance >= 0 && distance <= 2.0 / Math.min(halfWidth, halfHeight)
                                : distance >= 0) covered++;
                    }
                }
                int alpha = (covered * (rim ? 208 : 255) + 8) / 16;
                image.setPixelRGBA(x, y, alpha << 24 | 0xFFFFFF);
            }
        }
        return new DynamicTexture(image);
    }
}
