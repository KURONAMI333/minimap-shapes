package com.kuronami.minimapshapes.xaero;
import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
final class XaeroForgeClient {
    private static final KeyMapping OPEN = new KeyMapping("key.minimapshapes_xaero.open",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.minimapshapes_xaero");
    private XaeroForgeClient() {}
    static void init() {
        ClientShapes.register(ClientShapes.XAERO, new Shape[]{Shape.STANDARD, Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON,
                        Shape.HORIZONTAL_RECTANGLE, Shape.VERTICAL_RECTANGLE}, shape -> true);
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((RegisterKeyMappingsEvent event) -> event.register(OPEN));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ClientShapes.tick(ClientShapes.XAERO, OPEN);
        });
    }
}
