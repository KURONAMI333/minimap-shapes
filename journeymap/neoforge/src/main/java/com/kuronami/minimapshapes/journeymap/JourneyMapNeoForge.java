package com.kuronami.minimapshapes.journeymap;

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

@Mod(value = "minimapshapes_journeymap", dist = Dist.CLIENT)
public final class JourneyMapNeoForge {
    private static final KeyMapping OPEN = new KeyMapping("key.minimapshapes_journeymap.open",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.minimapshapes_journeymap");

    public JourneyMapNeoForge(IEventBus modBus) {
        ClientShapes.register(ClientShapes.JOURNEYMAP,
                new Shape[]{Shape.STANDARD, Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON},
                JourneyMapBridge::apply);
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(OPEN));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ClientShapes.tick(ClientShapes.JOURNEYMAP, OPEN));
    }
}
