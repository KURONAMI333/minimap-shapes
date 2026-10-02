package com.kuronami.minimapshapes.journeymap;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class JourneyMapFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientShapes.register(ClientShapes.JOURNEYMAP,
                new Shape[]{Shape.STANDARD, Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON},
                JourneyMapBridge::apply);
        KeyMapping open = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.minimapshapes_journeymap.open", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O, "key.categories.minimapshapes_journeymap"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientShapes.tick(ClientShapes.JOURNEYMAP, open));
    }
}
