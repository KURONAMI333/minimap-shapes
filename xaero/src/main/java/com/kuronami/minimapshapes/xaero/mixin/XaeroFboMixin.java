package com.kuronami.minimapshapes.xaero.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.ShapeSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Generate the square's corner data before the custom viewport cuts it out. */
@Mixin(targets = "xaero.common.minimap.render.MinimapFBORenderer", remap = false)
public abstract class XaeroFboMixin {
    @ModifyVariable(method = "renderChunksToFBO", at = @At("HEAD"), argsOnly = true,
            index = 13, require = 1, remap = false)
    private int minimapshapes$fullTexture(int original) {
        return ClientShapes.xaeroAvailable() && ShapeSettings.get(ClientShapes.XAERO).isCustom() ? 0 : original;
    }
}
