package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.block.entity.ForgottenHearthBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, ForgottenHearths.MOD_ID
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgottenHearthBlockEntity>> FORGOTTEN_HEARTH =
            BLOCK_ENTITIES.register(
                    "forgotten_hearth",
                    () -> new BlockEntityType<>(ForgottenHearthBlockEntity::new, ModBlocks.FORGOTTEN_HEARTH.get())
            );

    private ModBlockEntities() {
    }
}
