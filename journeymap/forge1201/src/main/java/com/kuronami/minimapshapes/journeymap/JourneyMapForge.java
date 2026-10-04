package com.kuronami.minimapshapes.journeymap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
@Mod("minimapshapes_journeymap")
public final class JourneyMapForge {
    public JourneyMapForge() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> JourneyMapForgeClient::init);
    }
}
