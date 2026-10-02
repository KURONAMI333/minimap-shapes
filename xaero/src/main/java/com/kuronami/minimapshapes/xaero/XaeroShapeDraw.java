package com.kuronami.minimapshapes.xaero;

import com.kuronami.minimapshapes.Shape;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/** Geometry for Xaero's final viewport. The caller retains the host shader and blend state. */
public final class XaeroShapeDraw {
    private static final double TAU = Math.PI * 2;
    private XaeroShapeDraw() {}

    public static void fill(Shape shape, PoseStack pose, double startAngle, int originalSegments,
                            float x, float y, int textureX, int textureY,
                            float diameter, float textureHeight, float atlasSize) {
        if (diameter <= 0 || atlasSize <= 0) return;
        int segments = Math.max(64, originalSegments);
        float half = diameter / 2;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX);
        List<Double> angles = samples(shape, startAngle, startAngle + TAU, segments);
        for (int i = 1; i < angles.size(); i++) {
            // Xaero uses current, previous, center winding, including its flipped texture V.
            vertex(buffer, matrix, x, y, textureX, textureY, half, textureHeight,
                    atlasSize, shape, angles.get(i));
            vertex(buffer, matrix, x, y, textureX, textureY, half, textureHeight,
                    atlasSize, shape, angles.get(i - 1));
            buffer.addVertex(matrix, x + half, y + half, 0)
                    .setUv((textureX + half) / atlasSize,
                            (textureY + textureHeight / 2) / atlasSize);
        }
        BufferUploader.drawWithShader(buffer.build());
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y,
                               int textureX, int textureY, float half, float textureHeight,
                               float atlasSize, Shape shape, double theta) {
        double radial = XaeroViewport.radius(shape, theta - Math.PI / 2);
        float dx = (float) (half * radial * Math.sin(theta));
        float dy = (float) (-half * radial * Math.cos(theta));
        buffer.addVertex(matrix, x + half + dx, y + half + dy, 0)
                .setUv((textureX + half + dx) / atlasSize,
                        (textureY + textureHeight * (0.5f - dy / (2 * half))) / atlasSize);
    }

    public static void frame(Shape shape, PoseStack pose, boolean resetTexture, boolean reverseTexture,
                             double startAngle, int start, int end, int segments, float thickness,
                             float x, float y, int textureX, int textureY,
                             float diameter, float textureRepeat, float textureHeight,
                             int seamWidth, float atlasSize) {
        if (diameter <= 0 || atlasSize <= 0 || segments <= 0) return;
        start = Math.max(0, Math.min(start, segments));
        end = Math.max(start, Math.min(end, segments));
        if (end == start) return;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);
        float half = diameter / 2;
        float segmentLength = (float) (TAU * (half + thickness) / segments);
        int textureStart = resetTexture ? (reverseTexture ? end : start) : 0;
        float seamThreshold = reverseTexture ? seamWidth + segmentLength : seamWidth;
        double a0 = startAngle + TAU * start / segments;
        double a1 = startAngle + TAU * end / segments;
        List<Double> angles = samples(shape, a0, a1, end - start);
        for (int i = 1; i < angles.size(); i++) {
            double a = angles.get(i - 1);
            double b = angles.get(i);
            float uA = frameU((float) ((a - startAngle) / TAU * segments), textureStart,
                    segmentLength, textureX, textureRepeat, seamWidth, seamThreshold, atlasSize);
            float uB = frameU((float) ((b - startAngle) / TAU * segments), textureStart,
                    segmentLength, textureX, textureRepeat, seamWidth, seamThreshold, atlasSize);
            frameVertex(buffer, matrix, x, y, half, 0, shape, a, uA, textureY + textureHeight, atlasSize);
            frameVertex(buffer, matrix, x, y, half, 0, shape, b, uB, textureY + textureHeight, atlasSize);
            frameVertex(buffer, matrix, x, y, half, thickness, shape, b, uB, textureY, atlasSize);
            frameVertex(buffer, matrix, x, y, half, thickness, shape, a, uA, textureY, atlasSize);
        }
        BufferUploader.drawWithShader(buffer.build());
    }

    private static float frameU(float segment, int start, float segmentLength, int textureX,
                                float repeat, int seamWidth, float seamThreshold, float atlasSize) {
        float travel = segmentLength * Math.abs(segment - start);
        float base = textureX;
        if (travel >= seamThreshold) {
            base += seamWidth;
            travel -= seamThreshold;
            if (repeat > 0 && travel >= repeat) travel %= repeat;
        }
        return (base + travel) / atlasSize;
    }

    private static void frameVertex(BufferBuilder buffer, Matrix4f matrix,
                                    float x, float y, float half, float border, Shape shape,
                                    double theta, float u, float v, float atlasSize) {
        double radius = half * XaeroViewport.radius(shape, theta - Math.PI / 2) + border;
        buffer.addVertex(matrix, x + half + (float) (radius * Math.sin(theta)),
                        y + half - (float) (radius * Math.cos(theta)), 0)
                .setUv(u, v / atlasSize);
    }

    /** Insert exact corners so an arbitrary Xaero circle seam cannot round polygon vertices. */
    private static List<Double> samples(Shape shape, double start, double end, int segments) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i <= segments; i++) values.add(start + (end - start) * i / segments);
        List<Double> corners = new ArrayList<>();
        if (shape == Shape.HEXAGON || shape == Shape.OCTAGON) {
            int count = shape == Shape.HEXAGON ? 6 : 8;
            double first = count == 6 ? 0 : Math.PI * 3 / 8;
            for (int i = 0; i < count; i++) corners.add(first + TAU * i / count);
        } else if (shape == Shape.HORIZONTAL_RECTANGLE || shape == Shape.VERTICAL_RECTANGLE) {
            double width = shape == Shape.HORIZONTAL_RECTANGLE ? 1 : XaeroViewport.shortAxis();
            double height = shape == Shape.VERTICAL_RECTANGLE ? 1 : XaeroViewport.shortAxis();
            for (int sx : new int[]{-1, 1}) for (int sy : new int[]{-1, 1})
                corners.add(Math.atan2(sx * width, -sy * height));
        }
        for (double corner : corners) {
            double angle = corner + Math.ceil((start - corner) / TAU) * TAU;
            if (angle > start + 1.0e-9 && angle < end - 1.0e-9) values.add(angle);
        }
        values.sort(Double::compare);
        return values;
    }
}
