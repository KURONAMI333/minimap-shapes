package com.kuronami.minimapshapes.journeymap;

import com.kuronami.minimapshapes.Shape;

import journeymap.client.ui.UIManager;
import journeymap.client.ui.minimap.MiniMap;

/** Only loaded after a matching JourneyMap version was found. */
public final class JourneyMapBridge {
    private JourneyMapBridge() {}

    public static boolean apply(Shape selected) {
        MiniMap minimap = UIManager.INSTANCE.getMiniMap();
        if (minimap == null || minimap.getCurrentMinimapProperties() == null) return false;
        // DisplayVars reads the original profile setting and substitutes Circle only in memory.
        // reset() rebuilds the active frame without changing any JourneyMap profile on disk.
        minimap.reset();
        return true;
    }
}
