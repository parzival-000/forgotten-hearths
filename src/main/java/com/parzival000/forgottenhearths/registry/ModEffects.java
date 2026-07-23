package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.effect.ComfortedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ForgottenHearths.MOD_ID);

    public static final DeferredHolder<MobEffect, ComfortedEffect> COMFORTED = EFFECTS.register(
            "comforted", ComfortedEffect::new
    );

    private ModEffects() {
    }
}
