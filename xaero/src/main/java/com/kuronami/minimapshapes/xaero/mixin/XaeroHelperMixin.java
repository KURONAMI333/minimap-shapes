package com.kuronami.minimapshapes.xaero.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeSettings;
import com.kuronami.minimapshapes.xaero.XaeroShapeDraw;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Both FBO and safe rendering finish with the same texture and frame helpers. */
@Mixin(targets = "xaero.common.minimap.render.MinimapRendererHelper", remap = false)
public abstract class XaeroHelperMixin {
    // @Desc uses class literals: Fabric remaps PoseStack without leaving a Mojmap string target.
    // Inject after the host's shader choice to preserve its alpha-test/premultiplication contract.
    @Inject(target = @Desc(value = "drawTexturedElipseInsideRectangle", args = {
            PoseStack.class, double.class, int.class, float.class, float.class,
            int.class, int.class, float.class, float.class, float.class}),
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V",
                    shift = At.Shift.AFTER, remap = false), cancellable = true, require = 1, remap = false)
    private void minimapshapes$fill(PoseStack pose, double startAngle, int segments,
                                    float x, float y, int textureX, int textureY,
                                    float diameter, float textureHeight, float atlasSize,
                                    CallbackInfo callback) {
        Shape shape = ShapeSettings.get(ClientShapes.XAERO);
        if (!ClientShapes.xaeroAvailable() || !shape.isCustom()) return;
        XaeroShapeDraw.fill(shape, pose, startAngle, segments, x, y,
                textureX, textureY, diameter, textureHeight, atlasSize);
        callback.cancel();
    }

    @Inject(method = "drawTexturedElipseInsideRectangleFrame",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V",
                    shift = At.Shift.AFTER, remap = false), cancellable = true, require = 1, remap = false)
    private void minimapshapes$frame(PoseStack pose, boolean resetTexture, boolean reverseTexture,
                                     double startAngle, int start, int end, int segments,
                                     float thickness, float x, float y,
                                     int textureX, int textureY, float diameter,
                                     float textureRepeat, float textureHeight,
                                     int seamWidth, float atlasSize,
                                     CallbackInfo callback) {
        Shape shape = ShapeSettings.get(ClientShapes.XAERO);
        if (!ClientShapes.xaeroAvailable() || !shape.isCustom()) return;
        XaeroShapeDraw.frame(shape, pose, resetTexture, reverseTexture, startAngle, start, end, segments,
                thickness, x, y, textureX, textureY, diameter,
                textureRepeat, textureHeight, seamWidth, atlasSize);
        callback.cancel();
    }
}
