package com.parzival000.forgottenhearths.advancement;

import com.parzival000.forgottenhearths.ForgottenHearths;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {
    public static final String DISCOVERED = "echoes_under_the_moss";
    public static final String RELIC = "traces_of_a_home";
    public static final String REKINDLED = "warmth_returns";
    public static final String RESTORED = "a_house_remembered";

    public static void grant(ServerPlayer player, String path) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(ForgottenHearths.id(path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "remembered");
        }
    }

    private ModAdvancements() {
    }
}
