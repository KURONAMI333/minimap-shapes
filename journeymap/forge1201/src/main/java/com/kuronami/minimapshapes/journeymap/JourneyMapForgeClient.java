package com.kuronami.minimapshapes.journeymap;
import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
final class JourneyMapForgeClient {
    private static final KeyMapping OPEN = new KeyMapping("key.minimapshapes_journeymap.open",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.minimapshapes_journeymap");
    private JourneyMapForgeClient() {}
    static void init() {
        ClientShapes.register(ClientShapes.JOURNEYMAP, new Shape[]{Shape.STANDARD, Shape.ROUNDED_SQUARE, Shape.HEXAGON, Shape.OCTAGON}, JourneyMapBridge::apply);
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((RegisterKeyMappingsEvent event) -> event.register(OPEN));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ClientShapes.tick(ClientShapes.JOURNEYMAP, OPEN);
        });
    }
}
