package com.kuronami.minimapshapes.xaero.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeSettings;
import com.kuronami.minimapshapes.xaero.XaeroViewport;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Xaero also uses this projection for compass letters; both follow the viewport boundary. */
@Mixin(targets = "xaero.hud.minimap.element.render.over.MinimapElementOverMapRendererHandler", remap = false)
public abstract class XaeroOverMapMixin {
    @Inject(method = "translatePosition", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void minimapshapes$project(PoseStack pose, int specW, int specH,
                                              int halfViewW, int halfViewH, double ps, double pc,
                                              double offX, double offY, double zoom, boolean circle,
                                              double[] partialTranslate, CallbackInfoReturnable<Boolean> callback) {
        Shape shape = ShapeSettings.get(ClientShapes.XAERO);
        if (!ClientShapes.xaeroAvailable() || !shape.isCustom() || !circle) return;
        // Preserve Xaero's rotation and zoom before replacing only the boundary.
        double x = (ps * offX - pc * offY) * zoom;
        double y = (pc * offX + ps * offY) * zoom;
        double angle = Math.atan2(y, x);
        double distance = Math.hypot(x, y);
        double boundary = XaeroViewport.radius(shape, angle);
        double markerEdge = Math.max(0, specW) * boundary;
        double viewEdge = Math.max(0, halfViewW) * boundary;
        boolean outside = distance > viewEdge;
        if (distance > markerEdge && distance > 0) {
            double factor = markerEdge / distance;
            x *= factor;
            y *= factor;
            outside = true;
        }
        // Keep the host's integer/fraction split and boolean out-of-bounds result together.
        long roundedX = Math.round(x);
        long roundedY = Math.round(y);
        partialTranslate[0] = x - roundedX;
        partialTranslate[1] = y - roundedY;
        pose.translate((float) roundedX, (float) roundedY, 0);
        callback.setReturnValue(outside);
    }
}
