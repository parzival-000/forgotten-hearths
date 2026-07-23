package com.parzival000.forgottenhearths;

import com.mojang.logging.LogUtils;
import com.parzival000.forgottenhearths.config.ForgottenHearthsConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ForgottenHearths.MOD_ID)
public final class ForgottenHearths {
    public static final String MOD_ID = "forgotten_hearths";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForgottenHearths(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ForgottenHearthsConfig.SPEC, "forgotten-hearths-server.toml");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

