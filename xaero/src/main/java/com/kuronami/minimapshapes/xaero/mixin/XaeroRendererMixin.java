package com.kuronami.minimapshapes.xaero.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.ShapeSettings;
import com.kuronami.minimapshapes.xaero.XaeroViewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** SHAPE is local 22 in Xaero 26.5.0 and 26.6.0's renderMinimap (identical bytecode), both loader jars. */
@Mixin(targets = "xaero.common.minimap.render.MinimapRenderer", remap = false)
public abstract class XaeroRendererMixin {
    @ModifyVariable(method = "renderMinimap", at = @At("STORE"), index = 22,
            require = 1, remap = false)
    private int minimapshapes$useCirclePipeline(int original) {
        if (!ClientShapes.xaeroAvailable() || !ShapeSettings.get(ClientShapes.XAERO).isCustom()) return original;
        XaeroViewport.update();
        return 1;
    }
}
