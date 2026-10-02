package com.kuronami.minimapshapes;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Client API shared by the JourneyMap and Xaero add-ons. */
public final class ClientShapes {
    public static final String JOURNEYMAP = "journeymap";
    public static final String XAERO = "xaero";

    private static final Map<String, Host> HOSTS = new ConcurrentHashMap<>();

    private ClientShapes() {}

    public static void register(String id, Shape[] choices, Function<Shape, Boolean> apply) {
        if (HOSTS.putIfAbsent(id, new Host(choices.clone(), apply)) != null) {
            throw new IllegalStateException("Minimap shape host already registered: " + id);
        }
    }

    public static boolean available(String id) {
        Host host = HOSTS.get(id);
        return host != null && !host.faulted;
    }

    public static Shape[] choices(String id) {
        Host host = HOSTS.get(id);
        return host == null ? new Shape[0] : host.choices.clone();
    }

    public static void tick(String id, KeyMapping openKey) {
        Host host = HOSTS.get(id);
        if (host == null) return;
        Minecraft mc = Minecraft.getInstance();
        Shape current = ShapeSettings.get(id);
        if (!host.faulted && mc.level != null && host.lastApplied != current) refresh(id);
        while (openKey.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new ShapeScreen(id));
        }
    }

    public static void refresh(String id) {
        Host host = HOSTS.get(id);
        if (host == null || host.faulted) return;
        Shape selected = ShapeSettings.get(id);
        try {
            if (host.apply.apply(selected)) host.lastApplied = selected;
        } catch (LinkageError | RuntimeException exception) {
            host.faulted = true;
            Constants.LOG.error("Minimap shape integration failed for " + id, exception);
        }
    }

    public static boolean journeyMapAvailable() {
        Host host = HOSTS.get(JOURNEYMAP);
        return host != null && !host.faulted;
    }

    public static boolean xaeroAvailable() {
        Host host = HOSTS.get(XAERO);
        return host != null && !host.faulted;
    }

    private static final class Host {
        final Shape[] choices;
        final Function<Shape, Boolean> apply;
        volatile Shape lastApplied;
        volatile boolean faulted;

        Host(Shape[] choices, Function<Shape, Boolean> apply) {
            this.choices = choices;
            this.apply = apply;
        }
    }
}
