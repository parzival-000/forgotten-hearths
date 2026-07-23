package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.block.ForgottenHearthBlock;
import com.parzival000.forgottenhearths.block.HearthStage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ForgottenHearths.MOD_ID);

    public static final DeferredBlock<ForgottenHearthBlock> FORGOTTEN_HEARTH = BLOCKS.registerBlock(
            "forgotten_hearth",
            ForgottenHearthBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(Block.INDESTRUCTIBLE, 1200.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(ForgottenHearthBlock.STAGE) == HearthStage.RESTORED
                            ? 13
                            : state.getValue(ForgottenHearthBlock.STAGE).isWarm() ? 10 : 0)
    );


    private ModBlocks() {
    }
}

