package com.kuronami.minimapshapes.journeymap.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeSettings;
import com.kuronami.minimapshapes.ShapeTexture;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.JMRenderTypes;
import journeymap.client.ui.theme.Theme;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The frame owns these final-sized replacements and closes them in clear(). */
@Mixin(targets = "journeymap.client.ui.theme.ThemeMinimapFrame", remap = false)
public abstract class JourneyMapFrameMixin {
    @Shadow private DynamicTexture textureCircleMask;
    @Shadow private DynamicTexture textureCircle;
    @Shadow private RenderType circleMaskRenderType;
    @Unique private DynamicTexture minimapshapes$originalCircleMask;
    @Unique private DynamicTexture minimapshapes$originalCircleRim;

    @Inject(method = "<init>", at = @At("TAIL"), require = 1, remap = false)
    private void minimapshapes$replace(Theme theme, Theme.Minimap.MinimapSpec spec,
                                       MiniMapProperties properties, int width, int height,
                                       CallbackInfo callback) {
        if (!ClientShapes.journeyMapAvailable()) return;
        Shape shape = ShapeSettings.get(ClientShapes.JOURNEYMAP);
        if (!shape.isCustom() || !(spec instanceof Theme.Minimap.MinimapCircle)) return;
        minimapshapes$originalCircleMask = textureCircleMask;
        minimapshapes$originalCircleRim = textureCircle;
        textureCircleMask = ShapeTexture.make(shape, width, height, false);
        textureCircle = ShapeTexture.make(shape, width, height, true);
        // JourneyMap's Fabric jar exposes this method with an intermediary-typed
        // DynamicTexture parameter. Resolve the one stable method at runtime so
        // both loaders compile against their own Minecraft mappings.
        try {
            circleMaskRenderType = (RenderType) JMRenderTypes.class
                    .getMethod("getMinimapCircleMask", AbstractTexture.class)
                    .invoke(null, textureCircleMask);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("JourneyMap circle mask render type is unavailable", exception);
        }
    }

    @Inject(method = "clear", at = @At("HEAD"), require = 1, remap = false)
    private void minimapshapes$closeOriginals(CallbackInfo callback) {
        // The frame normally closes these two cache-backed textures in clear().
        // Keep that lifetime after replacing its fields; never close them in
        // the constructor because another preview frame may still share them.
        if (minimapshapes$originalCircleMask != null) {
            minimapshapes$originalCircleMask.close();
            minimapshapes$originalCircleMask = null;
        }
        if (minimapshapes$originalCircleRim != null) {
            minimapshapes$originalCircleRim.close();
            minimapshapes$originalCircleRim = null;
        }
    }
}
