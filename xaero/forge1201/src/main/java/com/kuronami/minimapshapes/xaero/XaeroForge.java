package com.kuronami.minimapshapes.xaero;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
@Mod("minimapshapes_xaero")
public final class XaeroForge {
    public XaeroForge() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> XaeroForgeClient::init);
    }
}
