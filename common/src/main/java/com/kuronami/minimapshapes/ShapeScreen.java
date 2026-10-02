package com.kuronami.minimapshapes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class ShapeScreen extends Screen {
    private final String host;

    public ShapeScreen(String host) {
        super(Component.translatable("minimapshapes.screen.title." + host));
        this.host = host;
    }

    @Override
    protected void init() {
        Shape[] choices = ClientShapes.choices(host);
        Layout layout = layout(choices.length);
        for (int index = 0; index < choices.length; index++) {
            Shape shape = choices[index];
            int x = layout.x(index);
            int y = layout.y(index);
            addRenderableWidget(Button.builder(Component.translatable(
                    "minimapshapes.shape." + shape.name().toLowerCase(Locale.ROOT)), button -> {
                ShapeSettings.set(host, shape);
                rebuildWidgets();
            }).bounds(x + layout.preview + 8, y + 1,
                    layout.cellWidth - layout.preview - 10, layout.buttonHeight).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 80, layout.doneY(), 160, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        Shape[] choices = ClientShapes.choices(host);
        Layout layout = layout(choices.length);
        graphics.drawCenteredString(font, title, width / 2, layout.top - 20, 0xFFFFFF);
        for (int index = 0; index < choices.length; index++) {
            Shape shape = choices[index];
            int x = layout.x(index);
            int y = layout.y(index);
            drawPreview(graphics, shape, x + 2, y + 1, layout.preview);
            if (shape == ShapeSettings.get(host)) {
                graphics.renderOutline(x, y, layout.cellWidth, layout.buttonHeight + 2, 0xFFE1C869);
            }
        }
        if (!ClientShapes.available(host)) {
            graphics.drawCenteredString(font, Component.translatable("minimapshapes.screen.no_host"),
                    width / 2, layout.doneY() + 24, 0xFFDD7777);
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    private Layout layout(int choiceCount) {
        int columns = width >= 360 ? 2 : 1;
        int rows = Math.max(1, (choiceCount + columns - 1) / columns);
        int rowHeight = Math.max(20, Math.min(42, (height - 66) / rows));
        int cellWidth = Math.min(224, (width - 24) / columns);
        int preview = Math.min(36, rowHeight - 4);
        int top = Math.max(24, (height - rows * rowHeight - 30) / 2);
        return new Layout(columns, rows, rowHeight, cellWidth, preview,
                Math.min(30, rowHeight - 3), top, (width - columns * cellWidth) / 2);
    }

    private record Layout(int columns, int rows, int rowHeight, int cellWidth,
                          int preview, int buttonHeight, int top, int left) {
        int x(int index) {
            return left + (index % columns) * cellWidth;
        }
        int y(int index) { return top + (index / columns) * rowHeight; }
        int doneY() { return top + rows * rowHeight + 6; }
    }

    private static void drawPreview(GuiGraphics graphics, Shape shape, int left, int top, int size) {
        double half = size / 2.0;
        for (int y = 0; y < size; y++) {
            int start = size;
            int end = -1;
            for (int x = 0; x < size; x++) {
                double px = (x + 0.5 - half) / half;
                double py = (y + 0.5 - half) / half;
                if (ShapeGeometry.distanceToEdge(shape, px, py) >= 0) {
                    start = Math.min(start, x);
                    end = x;
                }
            }
            if (end >= start) {
                graphics.fill(left + start, top + y, left + end + 1, top + y + 1,
                        ((y / 5) & 1) == 0 ? 0xFF52725D : 0xFF456854);
            }
        }
        graphics.fill(left + size / 2 - 1, top + size / 2 - 1,
                left + size / 2 + 2, top + size / 2 + 2, 0xFFFFFFFF);
    }
}
