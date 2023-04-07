package me.settingdust.fixattributeracecondition.mixin;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(FontSet.class)
public abstract class MixinFontSet {
    @Shadow
    @Final
    private Int2ObjectMap<GlyphInfo> glyphInfos;

    @Shadow
    @Nullable
    protected abstract GlyphInfo getGlyphInfoForSpace(int codePoint);

    @Shadow
    protected abstract RawGlyph getRaw(int codePoint);

    @Inject(method = "getGlyphInfo", at = @At("HEAD"), cancellable = true)
    private synchronized void fixRaceCondition$syncGetGlyphInfo(int codePoint, CallbackInfoReturnable<GlyphInfo> cir) {
        cir.setReturnValue(glyphInfos.computeIfAbsent(codePoint, (point) -> {
            final var glyphInfo = getGlyphInfoForSpace(point);
            return glyphInfo == null ? getRaw(point) : glyphInfo;
        }));
    }
}
