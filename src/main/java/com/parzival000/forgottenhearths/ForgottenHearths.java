package com.parzival000.forgottenhearths;

import com.mojang.logging.LogUtils;
import com.parzival000.forgottenhearths.config.ForgottenHearthsConfig;
import com.parzival000.forgottenhearths.gametest.ModGameTests;
import com.parzival000.forgottenhearths.gametest.PersistenceValidation;
import com.parzival000.forgottenhearths.gametest.WorldgenValidation;
import com.parzival000.forgottenhearths.registry.ModBlockEntities;
import com.parzival000.forgottenhearths.registry.ModBlocks;
import com.parzival000.forgottenhearths.registry.ModCreativeTab;
import com.parzival000.forgottenhearths.registry.ModEffects;
import com.parzival000.forgottenhearths.registry.ModItems;
import com.parzival000.forgottenhearths.registry.ModWorldgen;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ForgottenHearths.MOD_ID)
public final class ForgottenHearths {
    public static final String MOD_ID = "forgotten_hearths";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForgottenHearths(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModCreativeTab.TABS.register(modBus);
        ModWorldgen.STRUCTURE_PLACEMENTS.register(modBus);
        ModGameTests.TEST_FUNCTIONS.register(modBus);
        NeoForge.EVENT_BUS.addListener(PersistenceValidation::onServerStarted);
        NeoForge.EVENT_BUS.addListener(WorldgenValidation::onServerStarted);
        container.registerConfig(ModConfig.Type.SERVER, ForgottenHearthsConfig.SPEC, "forgotten-hearths-server.toml");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

