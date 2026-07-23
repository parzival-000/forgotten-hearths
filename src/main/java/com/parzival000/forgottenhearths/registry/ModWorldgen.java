package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.worldgen.ConfigurableRandomSpreadPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS = DeferredRegister.create(
            Registries.STRUCTURE_PLACEMENT, ForgottenHearths.MOD_ID
    );

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<ConfigurableRandomSpreadPlacement>>
            CONFIGURABLE_RANDOM_SPREAD = STRUCTURE_PLACEMENTS.register(
                    "configurable_random_spread", () -> () -> ConfigurableRandomSpreadPlacement.CODEC
            );

    private ModWorldgen() {
    }
}
