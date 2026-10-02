package com.kuronami.minimapshapes.journeymap.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.ShapeSettings;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The active map gets the circle rendering pipeline, without editing any profile setting. */
@Mixin(targets = "journeymap.client.ui.minimap.DisplayVars", remap = false)
public abstract class JourneyMapDisplayVarsMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE",
            target = "Ljourneymap/common/properties/config/EnumField;get()Ljava/lang/Enum;",
            ordinal = 2, remap = false), require = 1, remap = false)
    private Enum<?> minimapshapes$circleForCustomShape(Enum<?> original) {
        if (ClientShapes.journeyMapAvailable() && ShapeSettings.get(ClientShapes.JOURNEYMAP).isCustom()) {
            return journeymap.client.ui.minimap.Shape.Circle;
        }
        return original;
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "FIELD",
            target = "Ljourneymap/client/ui/theme/Theme$Minimap$MinimapCircle;rotates:Z",
            opcode = org.objectweb.asm.Opcodes.GETFIELD, remap = false),
            require = 1, remap = false)
    private boolean minimapshapes$fixedFrameForCustomShape(boolean original) {
        return ClientShapes.journeyMapAvailable()
                && ShapeSettings.get(ClientShapes.JOURNEYMAP).isCustom() ? false : original;
    }
}
