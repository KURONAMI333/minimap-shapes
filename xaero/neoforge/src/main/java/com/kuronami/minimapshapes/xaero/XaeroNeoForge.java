package com.kuronami.minimapshapes.xaero;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = "minimapshapes_xaero", dist = Dist.CLIENT)
public final class XaeroNeoForge {
    private static final KeyMapping OPEN = new KeyMapping("key.minimapshapes_xaero.open",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.minimapshapes_xaero");

    public XaeroNeoForge(IEventBus modBus) {
        ClientShapes.register(ClientShapes.XAERO,
                new Shape[]{Shape.STANDARD, Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON,
                        Shape.HORIZONTAL_RECTANGLE, Shape.VERTICAL_RECTANGLE}, shape -> true);
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(OPEN));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ClientShapes.tick(ClientShapes.XAERO, OPEN));
    }
}
