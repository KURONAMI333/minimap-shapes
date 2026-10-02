package com.kuronami.minimapshapes.journeymap.mixin;

import com.kuronami.minimapshapes.ClientShapes;
import com.kuronami.minimapshapes.Shape;
import com.kuronami.minimapshapes.ShapeGeometry;
import com.kuronami.minimapshapes.ShapeSettings;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.ui.minimap.DisplayVars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** Keeps JourneyMap's waypoint and offscreen markers on the same contour as its mask. */
@Mixin(targets = "journeymap.client.ui.minimap.MiniMap", remap = false)
public abstract class JourneyMapBoundaryMixin {
    @Shadow public abstract DisplayVars getDisplayVars();
    @Shadow public abstract MiniMapProperties getCurrentMinimapProperties();
    @Shadow private double lastRotation;

    @Inject(method = "isOnScreen", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void minimapshapes$isOnShape(Point2D.Double point, Point2D center,
                                          Rectangle2D.Double ignored,
                                          CallbackInfoReturnable<Boolean> callback) {
        Shape selected = ShapeSettings.get(ClientShapes.JOURNEYMAP);
        DisplayVars vars = getDisplayVars();
        if (!ClientShapes.journeyMapAvailable() || !selected.isCustom() || vars == null) return;
        double halfWidth = vars.minimapWidth / 2.0;
        double halfHeight = vars.minimapHeight / 2.0;
        if (halfWidth <= 0 || halfHeight <= 0) return;
        double radians = Math.toRadians(Double.isFinite(lastRotation) ? lastRotation : 0);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double dx = point.x - center.getX();
        double dy = point.y - center.getY();
        double x = (cosine * dx - sine * dy) / halfWidth;
        double y = (sine * dx + cosine * dy) / halfHeight;
        callback.setReturnValue(ShapeGeometry.distanceToEdge(selected, x, y) > 0);
    }

    @Inject(method = "getPointOnFrame", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void minimapshapes$pointOnShape(Point2D.Double point, Point2D center, double offset,
                                             CallbackInfoReturnable<Point2D.Double> callback) {
        Shape selected = ShapeSettings.get(ClientShapes.JOURNEYMAP);
        DisplayVars vars = getDisplayVars();
        if (!ClientShapes.journeyMapAvailable() || !selected.isCustom() || vars == null) return;
        // JourneyMap offsets the projection origin on both axes before tracing
        // the ray, so preserve that contract when replacing the circle radius.
        double originX = center.getX() + offset;
        double originY = center.getY() + offset;
        double radians = Math.toRadians(Double.isFinite(lastRotation) ? lastRotation : 0);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double dx = point.x - originX;
        double dy = point.y - originY;
        double rotatedX = cosine * dx - sine * dy;
        double rotatedY = sine * dx + cosine * dy;
        double angle = Math.atan2(rotatedY, rotatedX);
        double radius = ShapeGeometry.radius(selected, angle);
        double edgeX = vars.minimapWidth / 2.0 * radius * Math.cos(angle);
        double edgeY = vars.minimapHeight / 2.0 * radius * Math.sin(angle);
        callback.setReturnValue(new Point2D.Double(
                originX + cosine * edgeX + sine * edgeY,
                originY - sine * edgeX + cosine * edgeY));
    }

    @ModifyArg(method = "updateDisplayVars(Ljourneymap/client/ui/minimap/Shape;FFLjourneymap/client/ui/minimap/Position;ZZ)V",
            at = @At(value = "INVOKE", target =
                    "Ljourneymap/common/properties/config/EnumField;set(Ljava/lang/Enum;)Ljourneymap/common/properties/config/EnumField;",
                    ordinal = 0, remap = false),
            index = 0, require = 1, remap = false)
    private Enum<?> minimapshapes$preserveHostProfile(Enum<?> proposed) {
        if (ClientShapes.journeyMapAvailable() && ShapeSettings.get(ClientShapes.JOURNEYMAP).isCustom()
                && proposed == journeymap.client.ui.minimap.Shape.Circle) {
            return getCurrentMinimapProperties().shape.get();
        }
        return proposed;
    }
}
