package com.kuronami.minimapshapes;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

/** Separate client preference for each installed minimap add-on. */
public final class ShapeSettings {
    private static final String KEY = "shape";
    private static final Map<String, Shape> SELECTED = new ConcurrentHashMap<>();

    private ShapeSettings() {}

    private static Path path(String host) {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("minimapshapes-" + host + ".properties");
    }

    public static Shape get(String host) {
        validateHost(host);
        return SELECTED.computeIfAbsent(host, ShapeSettings::load);
    }

    public static void set(String host, Shape shape) {
        if (get(host) == shape) return;
        SELECTED.put(host, shape);
        save(host, shape);
        ClientShapes.refresh(host);
    }

    private static void validateHost(String host) {
        if (host == null || !host.matches("[a-z0-9_-]{1,32}")) {
            throw new IllegalArgumentException("Invalid minimap shape host id: " + host);
        }
    }

    private static Shape load(String host) {
        Path path = path(host);
        if (!Files.isRegularFile(path)) return Shape.STANDARD;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            return Shape.valueOf(properties.getProperty(KEY, Shape.STANDARD.name()));
        } catch (IOException | IllegalArgumentException exception) {
            Constants.LOG.warn("Could not load minimap shape setting for " + host, exception);
            return Shape.STANDARD;
        }
    }

    private static void save(String host, Shape shape) {
        Properties properties = new Properties();
        properties.setProperty(KEY, shape.name());
        try {
            Files.createDirectories(path(host).getParent());
            try (OutputStream output = Files.newOutputStream(path(host))) {
                properties.store(output, "Minimap Shapes " + host + " client preferences");
            }
        } catch (IOException exception) {
            Constants.LOG.error("Could not save minimap shape settings", exception);
        }
    }
}
