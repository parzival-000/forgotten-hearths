package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.block.ForgottenHearthBlock;
import com.parzival000.forgottenhearths.block.HearthStage;
import com.parzival000.forgottenhearths.block.RestorationMarkerBlock;
import com.parzival000.forgottenhearths.block.SmallDecorativeBlock;
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

    public static final DeferredBlock<RestorationMarkerBlock> RESTORATION_MARKER = BLOCKS.registerBlock(
            "restoration_marker",
            RestorationMarkerBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(Block.INDESTRUCTIBLE, 1200.0F)
                    .sound(SoundType.DEEPSLATE_TILES)
                    .noOcclusion()
    );

    public static final DeferredBlock<SmallDecorativeBlock> HEARTH_KETTLE = BLOCKS.registerBlock(
            "hearth_kettle",
            properties -> new SmallDecorativeBlock(properties, Block.box(2, 0, 3, 15, 14, 13)),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5F).sound(SoundType.COPPER).noOcclusion()
    );

    public static final DeferredBlock<SmallDecorativeBlock> MENDED_CROCK = BLOCKS.registerBlock(
            "mended_crock",
            properties -> new SmallDecorativeBlock(properties, Block.box(3, 0, 3, 13, 11, 13)),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(0.8F).sound(SoundType.DECORATED_POT).noOcclusion()
    );

    public static final DeferredBlock<SmallDecorativeBlock> PATCHWORK_QUILT = BLOCKS.registerBlock(
            "patchwork_quilt",
            properties -> new SmallDecorativeBlock(properties, Block.box(2, 0, 3, 14, 3, 13)),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.4F).sound(SoundType.WOOL).noOcclusion()
    );

    private ModBlocks() {
    }
}
