package me.settingdust.fixattributeracecondition.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Mixin(AttributeMap.class)
public abstract class MixinAttributeMap<E, K, V> {
    @Shadow
    @Final
    private Set<AttributeInstance> dirtyAttributes;
    @Shadow
    @Final
    private AttributeSupplier supplier;

    @Shadow
    protected abstract void onAttributeModified(AttributeInstance p_22158_);

    @Redirect(method = "onAttributeModified", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private synchronized boolean fixRaceCondition$syncAdd(Set<AttributeInstance> instance, E e) {
        return dirtyAttributes.add((AttributeInstance) e);
    }

    @Redirect(method = "getInstance", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private synchronized V fixRaceCondition$syncComputeIfAbsent(Map<Attribute, AttributeInstance> attributes, K k, Function<? super K, ? extends V> mappingFunction) {
        return (V) attributes.computeIfAbsent((Attribute) k, (Function<? super Attribute, ? extends AttributeInstance>) mappingFunction);
    }
}
