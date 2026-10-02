package com.kuronami.minimapshapes.xaero.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.ShapeSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Safe mode has a separate terrain/radar generation path with the same square envelope. */
@Mixin(targets = "xaero.common.minimap.render.MinimapSafeModeRenderer", remap = false)
public abstract class XaeroSafeModeMixin {
    @ModifyVariable(method = "updateMapFrameSafeMode", at = @At("HEAD"), argsOnly = true,
            index = 10, require = 1, remap = false)
    private int minimapshapes$fullTexture(int original) {
        return ClientShapes.xaeroAvailable() && ShapeSettings.get(ClientShapes.XAERO).isCustom() ? 0 : original;
    }
}
