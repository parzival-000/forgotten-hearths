package com.parzival000.forgottenhearths.effect;

import com.parzival000.forgottenhearths.config.ForgottenHearthsConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ComfortedEffect extends MobEffect {
    public ComfortedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD98945);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        int interval = ForgottenHearthsConfig.COMFORT_HEAL_INTERVAL_TICKS.getAsInt();
        if (entity.tickCount % interval == 0
                && entity instanceof Player player
                && player.getFoodData().getFoodLevel() >= 18
                && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0F);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }
}
