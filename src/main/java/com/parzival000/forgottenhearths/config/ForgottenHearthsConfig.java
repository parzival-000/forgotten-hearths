package com.parzival000.forgottenhearths.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ForgottenHearthsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue GENERATE_HOMESTEADS = BUILDER
            .comment("Whether Forgotten Hearths homesteads generate in new chunks.")
            .define("worldgen.generateHomesteads", true);

    public static final ModConfigSpec.IntValue HOMESTEAD_SPACING = BUILDER
            .comment("Average structure grid spacing in chunks. Larger values make homesteads rarer.")
            .defineInRange("worldgen.homesteadSpacing", 52, 24, 160);

    public static final ModConfigSpec.IntValue REQUIRED_LOGS = BUILDER
            .comment("Logs consumed when a discovered hearth is rekindled.")
            .defineInRange("restoration.requiredLogs", 4, 1, 16);

    public static final ModConfigSpec.IntValue REQUIRED_BRICKS = BUILDER
            .comment("Bricks consumed when a discovered hearth is rekindled.")
            .defineInRange("restoration.requiredBricks", 4, 1, 16);

    public static final ModConfigSpec.BooleanValue COMFORT_ENABLED = BUILDER
            .comment("Whether restored hearths grant Comforted.")
            .define("comfort.enabled", true);

    public static final ModConfigSpec.DoubleValue COMFORT_RADIUS = BUILDER
            .comment("Radius in blocks around a restored hearth that grants Comforted.")
            .defineInRange("comfort.radius", 6.0, 2.0, 16.0);

    public static final ModConfigSpec.IntValue COMFORT_REFRESH_TICKS = BUILDER
            .comment("How often a restored hearth refreshes Comforted.")
            .defineInRange("comfort.refreshTicks", 40, 20, 200);

    public static final ModConfigSpec.IntValue COMFORT_HEAL_INTERVAL_TICKS = BUILDER
            .comment("Ticks between half-heart Comforted healing pulses when the player is well fed.")
            .defineInRange("comfort.healIntervalTicks", 100, 40, 600);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ForgottenHearthsConfig() {
    }
}
