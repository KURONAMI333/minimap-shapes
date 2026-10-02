package com.kuronami.minimapshapes.xaero;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class XaeroFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientShapes.register(ClientShapes.XAERO,
                new Shape[]{Shape.STANDARD, Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON,
                        Shape.HORIZONTAL_RECTANGLE, Shape.VERTICAL_RECTANGLE}, shape -> true);
        KeyMapping open = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.minimapshapes_xaero.open", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K, "key.categories.minimapshapes_xaero"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientShapes.tick(ClientShapes.XAERO, open));
    }
}
